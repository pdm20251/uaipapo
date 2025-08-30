package com.example.uaipapo.feature.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.uaipapo.R
import com.example.uaipapo.model.Message
//import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(@ApplicationContext val context: Context) : ViewModel() {

    /**
     * Declaração de um StateFlow mutável privado que armazena a lista de mensagens.
     * O uso de uma lista vazia como valor inicial previne erros de estado nulo.
     */
    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    /**
     * Expõe a lista de mensagens como um StateFlow de leitura, seguindo a convenção.
     */
    val message = _messages.asStateFlow()

    // Lista de mensagens filtradas que a UI irá exibir
    private val _filteredMessages = MutableStateFlow<List<Message>>(emptyList())
    val filteredMessages: StateFlow<List<Message>> = _filteredMessages

    // Instância do Firebase Realtime Database.
    private val db = Firebase.database
    private val storage = Firebase.storage.reference

    fun sendImageMessage(imageUri: Uri, channelID: String, onComplete: () -> Unit) {
        val currentUser = Firebase.auth.currentUser ?: return
        val imageRef = storage.child("chat_images/${channelID}/${UUID.randomUUID()}.jpg")

        // Inicia o upload da imagem para o Firebase Storage
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Obtém a URL de download da imagem
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Cria uma mensagem de imagem com a URL
                    val message = Message(
                        db.reference.push().key ?: UUID.randomUUID().toString(),
                        currentUser.uid,
                        null, // Mensagem de texto é nula para imagens
                        System.currentTimeMillis(),
                        currentUser.displayName ?: "Anônimo",
                        downloadUrl.toString(), // A URL da imagem da mensagem
                        currentUser.photoUrl.toString() // URL da foto de perfil
                    )
                    // Envia a mensagem para o Firebase Realtime Database
                    db.reference.child("messages").child(channelID).push().setValue(message)
                        .addOnCompleteListener {
                            onComplete()
                        }
                }
            }
            .addOnFailureListener { e ->
                onComplete()
                // Tratar falha no upload
            }
    }

    /**
     * Envia uma mensagem de texto ou imagem para um canal específico.
     * @param channelID O ID do canal para onde a mensagem será enviada.
     * @param messageText O conteúdo da mensagem de texto.
     * @param image A URL da imagem, se a mensagem for uma imagem.
     */
    fun sendMessage(channelID: String, messageText: String) {
        val currentUser = Firebase.auth.currentUser ?: return

        val message = Message(
            db.reference.push().key ?: UUID.randomUUID().toString(),
            currentUser.uid,
            messageText,
            System.currentTimeMillis(),
            currentUser.displayName ?: "Anônimo",
            null,
            currentUser.photoUrl.toString()
        )

        // Salva a mensagem do Realtime Firabse Database
        db.reference.child("messages").child(channelID).push().setValue(message)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //postNotificationToUsers(channelID, message.senderName, messageText)
                }
            }
    }
    /**
     * Inicia a escuta por novas mensagens em um canal específico.
     * @param channelID O ID do canal a ser monitorado.
     */
    fun listenForMessages(channelID: String) {
        // Define um listener para monitorar o nó de mensagens no Firebase.
        db.getReference("messages").child(channelID).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Quando novos dados são recebidos, converte-os em uma lista de objetos Message.
                    val list = mutableListOf<Message>()
                    snapshot.children.forEach { data ->
                        val message = data.getValue(Message::class.java)
                        message?.let {
                            list.add(it)
                        }
                    }
                    // Atualiza o StateFlow com a nova lista, o que notifica a UI.
                    _messages.value = list
                    searchMessages("")
                }

                override fun onCancelled(error: DatabaseError) {
                    // Tratamento de erro quando o listener é cancelado.
                }
            })
        // Adiciona o usuário a um tópico de notificação e o registra no canal.
        registerUserIdtoChannel(channelID)
    }

    /**
     * Obtém os IDs de todos os usuários em um canal.
     * @param channelID O ID do canal.
     * @param callback A função de retorno a ser executada com a lista de IDs de usuários.
     */
    fun getAllUserEmails(channelID: String, callback: (List<String>) -> Unit) {
        // Referencia o nó de usuários do canal.
        val ref = db.reference.child("channels").child(channelID).child("users")
        // Adiciona um listener para obter os dados uma única vez.
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userIds = snapshot.children.map { it.value.toString() }
                callback.invoke(userIds)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.invoke(emptyList())
            }
        })
    }

    /**
     * Registra o ID do usuário atual em um canal, se ele ainda não estiver lá.
     * @param channelID O ID do canal.
     */
    fun registerUserIdtoChannel(channelID: String) {
        val currentUser = Firebase.auth.currentUser
        val ref = db.reference.child("channels").child(channelID).child("users")
        ref.child(currentUser?.uid ?: "").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Adiciona o usuário apenas se ele não estiver registrado.
                    if (!snapshot.exists()) {
                        ref.child(currentUser?.uid ?: "").setValue(currentUser?.email)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
        )
    }

    /**
     * Filtra as mensagens com base no texto de busca.
     * @param query O termo de busca.
     */
    fun searchMessages(query: String) {
        if (query.isBlank()) {
            _filteredMessages.value = _messages.value
        } else {
            val filteredList = _messages.value.filter {
                it.message?.contains(query, ignoreCase = true) == true
            }
            _filteredMessages.value = filteredList
        }
    }

}

