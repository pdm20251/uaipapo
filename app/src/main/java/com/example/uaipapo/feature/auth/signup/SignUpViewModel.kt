//package com.example.uaipapo.feature.auth.signup
//
//import androidx.lifecycle.ViewModel
//import com.google.firebase.auth.FirebaseAuth
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import javax.inject.Inject
//
///**
// * ViewModel responsável pela lógica de cadastro de novos usuários.
// * Gerencia o estado da UI de cadastro e a comunicação com o Firebase.
// */
//@HiltViewModel
//class SignUpViewModel @Inject constructor() : ViewModel() {
//
//    // Armazena o estado interno da tela de cadastro.
//    private val _state = MutableStateFlow<SignUpState>(SignUpState.Nothing)
//
//    // Expõe o estado como um fluxo de leitura para a UI.
//    val state = _state.asStateFlow()
//
//    /**
//     * Inicia o processo de cadastro de um novo usuário com nome, email e senha.
//     * Atualiza o estado para refletir o progresso da operação.
//     * @param name O nome do usuário.
//     * @param email O email do usuário.
//     * @param password A senha do usuário.
//     */
//    fun signUp(name: String, email: String, password: String) {
//        // Altera o estado para "carregando" para indicar que a operação está em andamento.
//        _state.value = SignUpState.Loading
//
//        // Cria um novo usuário no Firebase Authentication.
//        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//
//                    // Se o usuário foi criado com sucesso, atualiza o perfil com o nome.
//                    task.result.user?.let {
//                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
//                            .setDisplayName(name)
//                            .build()
//                        it.updateProfile(profileUpdates)?.addOnCompleteListener {
//
//                            // Define o estado como "sucesso" após o perfil ser atualizado.
//                            _state.value = SignUpState.Success
//                        }
//                        return@addOnCompleteListener
//                    }
//
//                    // Em caso de falha na atualização do perfil, define o estado como erro.
//                    _state.value = SignUpState.Error
//
//                } else {
//
//                    // Se a criação do usuário falhou (ex: email já existe), define o estado como erro.
//                    _state.value = SignUpState.Error
//                }
//            }
//    }
//}
//
///**
// * Classe selada que define os estados possíveis da tela de cadastro.
// * Garante que a UI reaja de forma apropriada a cada estado.
// */
//sealed class SignUpState {
//
//    // Estado inicial.
//    object Nothing : SignUpState()
//
//    // Estado de carregamento.
//    object Loading : SignUpState()
//
//    // Estado de sucesso.
//    object Success : SignUpState()
//
//    // Estado de erro.
//    object Error : SignUpState()
//}