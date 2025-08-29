package com.example.uaipapo.feature.auth.signin

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel responsável pela lógica de autenticação do usuário.
 * Gerencia a comunicação com o Firebase e o estado da UI de login.
 */
@HiltViewModel
class SignInViewModel @Inject constructor() : ViewModel() {

    // MutableStateFlow que armazena o estado atual da tela de login.
    private val _state = MutableStateFlow<SignInState>(SignInState.Nothing)

    // Expõe o estado como um StateFlow de leitura para a UI.
    val state = _state.asStateFlow()

    /**
     * Inicia o processo de login usando email e senha.
     * Atualiza o estado da UI para refletir o progresso da operação.
     * @param email O email fornecido pelo usuário.
     * @param password A senha fornecida pelo usuário.
     */
    fun signIn(email: String, password: String) {
        // Define o estado como "carregando" para mostrar um indicador na UI.
        _state.value = SignInState.Loading

        // Tenta autenticar o usuário com o Firebase.
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Verifica o resultado da autenticação.
                if (task.isSuccessful && task.result?.user != null) {
                    // Autenticação bem-sucedida.
                    _state.value = SignInState.Success
                } else {
                    // Autenticação falhou.
                    _state.value = SignInState.Error
                }
            }
    }
}

/**
 * Classe selada que define os estados possíveis da tela de login,
 * garantindo que a UI reaja a cada um de forma controlada.
 */
sealed class SignInState {
    // Estado inicial.
    object Nothing : SignInState()
    // Estado de carregamento.
    object Loading : SignInState()
    // Estado de sucesso.
    object Success : SignInState()
    // Estado de erro.
    object Error : SignInState()
}