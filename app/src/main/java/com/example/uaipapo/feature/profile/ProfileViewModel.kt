package com.example.uaipapo.feature.profile
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class UserProfile(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val auth = Firebase.auth
    private val database = Firebase.database.reference.child("users")
    private val storage = Firebase.storage.reference

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            database.child(firebaseUser.uid).get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java)
                _currentUser.value = UserProfile(
                    uid = firebaseUser.uid,
                    name = name,
                    email = firebaseUser.email,
                    profileImageUrl = firebaseUser.photoUrl.toString()
                )
            }.addOnFailureListener {
                // Tratar erro de busca
            }
        }
    }
    // Função que carrega a imagem e atualiza o perfil do usuário
    fun updateUserProfile(newName: String, newImageUri: Uri?, onComplete: (Boolean) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // Usa o nome atual do usuário se o campo 'newName' estiver vazio.
            val finalName = if (newName.isNullOrBlank()) {
                firebaseUser.displayName
            } else {
                newName
            }

            // Se uma nova imagem foi selecionada, faça o upload para o Firebase Storage.
            if (newImageUri != null) {
                val imageRef = storage.child("profile_pictures/${firebaseUser.uid}.jpg")
                imageRef.putFile(newImageUri)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            updateFirebaseUser(finalName, uri.toString(), onComplete)
                        }
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            } else {
                // Se não houver nova imagem, atualize apenas o nome.
                updateFirebaseUser(finalName, firebaseUser.photoUrl.toString(), onComplete)
            }
        } else {
            onComplete(false)
        }
    }

    // Função interna para atualizar o perfil no Firebase Auth e Database
    private fun updateFirebaseUser(newName: String?, imageUrl: String?, onComplete: (Boolean) -> Unit) {
        val firebaseUser = auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            // Atualiza o nome de exibição apenas se 'newName' não for nulo.
            if (newName != null) {
                displayName = newName
            }
            photoUri = if (imageUrl != "null") Uri.parse(imageUrl) else null
        }
        firebaseUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Atualiza o nome no Firebase Realtime Database apenas se 'newName' não for nulo.
                    if (newName != null) {
                        database.child(firebaseUser.uid).child("name").setValue(newName)
                            .addOnCompleteListener { dbTask ->
                                onComplete(dbTask.isSuccessful)
                            }
                    } else {
                        onComplete(true) // Considera a operação como bem-sucedida se apenas a imagem foi atualizada.
                    }
                } else {
                    onComplete(false)
                }
            }
        }
    }