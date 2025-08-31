package com.example.uaipapo.feature.chat

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Se MessageRepository for usado e injetado, descomente
// import com.example.uaipapo.data.MessageRepository
import com.example.uaipapo.model.Message
import com.google.firebase.auth.FirebaseAuth // Import para injeção
import com.google.firebase.database.DataSnapshot // Import específico
import com.google.firebase.database.DatabaseError // Import específico
import com.google.firebase.database.FirebaseDatabase // Import para injeção
import com.google.firebase.database.ValueEventListener // Import específico
import com.google.firebase.storage.FirebaseStorage // Import para injeção
// KTX imports ainda são úteis, especialmente para o Hilt Module.
// Não são estritamente necessários aqui se tudo for injetado e usado.
// import com.google.firebase.auth.ktx.auth
// import com.google.firebase.database.ktx.database
// import com.google.firebase.storage.ktx.storage
// import com.google.firebase.ktx.Firebase

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth, // Injetado
    private val firebaseDatabase: FirebaseDatabase, // Injetado
    private val firebaseStorage: FirebaseStorage // Injetado
    // Se você tem MessageRepository injetado e configurado no FirebaseModule:
    // private val messageRepository: MessageRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val message = _messages.asStateFlow() 

    private val _filteredMessages = MutableStateFlow<List<Message>>(emptyList())
    val filteredMessages: StateFlow<List<Message>> = _filteredMessages

    fun listenForMessages(channelID: String) {
        // Usa a instância injetada de FirebaseDatabase
        firebaseDatabase.getReference("messages").child(channelID).orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Message>()
                    snapshot.children.forEach { data ->
                        val msg = data.getValue(Message::class.java)
                        msg?.let { list.add(it) }
                    }
                    _messages.value = list
                    searchMessages("") 
                }
                override fun onCancelled(error: DatabaseError) { /*Tratar erro*/ }
            })
        registerUserIdtoChannel(channelID)
    }

    fun sendMessage(channelID: String, messageText: String) {
        // Usa a instância injetada de FirebaseAuth e FirebaseDatabase
        val currentUser = firebaseAuth.currentUser ?: return
        val msg = Message(
            id = firebaseDatabase.reference.push().key ?: UUID.randomUUID().toString(),
            senderId = currentUser.uid,
            message = messageText,
            timestamp = Date(),
            senderName = currentUser.displayName ?: "Anônimo",
            senderPhotoUrl = currentUser.photoUrl?.toString(),
            channelId = channelID
        )
        firebaseDatabase.reference.child("messages").child(channelID).push().setValue(msg)
    }

    @SuppressLint("Range")
    private fun getFileMetadata(uri: Uri): Pair<String?, Long?> {
        var fileName: String? = null
        var fileSize: Long? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = cursor.getString(displayNameIndex)
                }
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }
        return Pair(fileName, fileSize)
    }

    fun sendMediaMessage(uri: Uri, channelId: String, mediaType: String, onUploadComplete: () -> Unit) {
        // Usa instâncias injetadas
        val currentUser = firebaseAuth.currentUser ?: run {
            onUploadComplete()
            return
        }
        val (fileName, fileSize) = getFileMetadata(uri)
        
        val storageDir = when (mediaType) {
            "image" -> "chat_images"
            "document" -> "chat_documents"
            "audio" -> "chat_audios"
            "video" -> "chat_videos"
            else -> "chat_files"
        }
        val storagePath = "${storageDir}/${channelId}/${UUID.randomUUID()}_${fileName ?: mediaType}"
        // Usa a instância injetada de FirebaseStorage
        val mediaRef = firebaseStorage.reference.child(storagePath)

        viewModelScope.launch {
            try {
                mediaRef.putFile(uri).await()
                val mediaUrlResult = mediaRef.downloadUrl.await().toString()
                
                val msg = Message(
                    // Usa a instância injetada de FirebaseDatabase
                    id = firebaseDatabase.reference.push().key ?: UUID.randomUUID().toString(),
                    senderId = currentUser.uid,
                    senderName = currentUser.displayName ?: "Anônimo",
                    senderPhotoUrl = currentUser.photoUrl?.toString(),
                    timestamp = Date(),
                    channelId = channelId,
                    mediaUrl = mediaUrlResult,
                    mediaType = mediaType,
                    fileName = fileName,
                    fileSize = fileSize,
                    imageUrl = if (mediaType == "image") mediaUrlResult else null 
                )
                // Usa a instância injetada de FirebaseDatabase
                firebaseDatabase.reference.child("messages").child(channelId).push().setValue(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onUploadComplete()
            }
        }
    }

    fun sendImageMessage(imageUri: Uri, channelID: String, onComplete: () -> Unit) {
        sendMediaMessage(imageUri, channelID, "image", onComplete)
    }

    fun registerUserIdtoChannel(channelID: String) {
        // Usa instâncias injetadas
        val currentUser = firebaseAuth.currentUser
        val ref = firebaseDatabase.reference.child("channels").child(channelID).child("users")
        ref.child(currentUser?.uid ?: "").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        ref.child(currentUser?.uid ?: "").setValue(currentUser?.email)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
        )
    }

    fun searchMessages(query: String) {
        if (query.isBlank()) {
            _filteredMessages.value = _messages.value
        } else {
            val filteredList = _messages.value.filter {
                (it.message?.contains(query, ignoreCase = true) == true) ||
                (it.fileName?.contains(query, ignoreCase = true) == true)
            }
            _filteredMessages.value = filteredList
        }
    }
}
