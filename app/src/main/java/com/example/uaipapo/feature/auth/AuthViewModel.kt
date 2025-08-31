package com.example.uaipapo.feature.auth


import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    init {
        // Observar mudanças no estado de autenticação
        firebaseAuth.addAuthStateListener { auth ->
            _isUserLoggedIn.value = auth.currentUser != null
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}