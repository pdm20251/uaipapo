package com.example.uaipapo.feature.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class UserProfile(
    val uid: String? = null,
    val name: String? = null,
    val email: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val auth = Firebase.auth
    private val database = Firebase.database.reference.child("users")

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // Usa o ID do usuário para obter dados do Realtime Database.
            database.child(firebaseUser.uid).get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java)
                _currentUser.value = UserProfile(
                    uid = firebaseUser.uid,
                    name = name,
                    email = firebaseUser.email
                )
            }.addOnFailureListener {
                // Tratar erro de busca
            }
        }
    }

    fun updateUserProfile(newName: String, onComplete: (Boolean) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // Atualiza o nome de exibição no Firebase Auth.
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
            firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Atualiza o nome no Firebase Realtime Database.
                        database.child(firebaseUser.uid).child("name").setValue(newName)
                            .addOnCompleteListener { dbTask ->
                                onComplete(dbTask.isSuccessful)
                            }
                    } else {
                        onComplete(false)
                    }
                }
        } else {
            onComplete(false)
        }
    }
}