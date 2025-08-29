package com.example.uaipapo.feature.auth.signup

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.uaipapo.R
import com.example.uaipapo.feature.auth.signin.SignInState // Nota: O `SignInState` aqui parece ser um erro de importação.

/**
 * Tela de cadastro (`Sign Up`) que utiliza Jetpack Compose para a UI.
 * Gerencia a entrada do usuário e o estado da criação de uma nova conta.
 * @param navController O controlador de navegação para gerenciar a transição entre telas.
 */
@Composable
fun SignUpScreen(navController: NavController) {
    // Instancia o ViewModel para o gerenciamento da lógica de negócio.
    val viewModel: SignUpViewModel = hiltViewModel()

    // Observa o estado da UI a partir do ViewModel.
    val uiState = viewModel.state.collectAsState()

    // Estados dos campos de entrada, usando 'remember' para persistir durante a recomposição.
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    // Obtém o contexto atual para exibir mensagens.
    val context = LocalContext.current

    /**
     * Efeito colateral que reage a mudanças no estado da UI.
     * Trata a navegação e a exibição de mensagens após o cadastro.
     */
    LaunchedEffect(key1 = uiState.value) {
        when (uiState.value) {
            is SignUpState.Success -> {
                // Navega para a tela principal em caso de sucesso.
                navController.navigate("home")
            }
            is SignUpState.Error -> {
                // Exibe uma mensagem de erro em caso de falha.
                Toast.makeText(context, "Falha ao entrar", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    // A estrutura base da tela, com preenchimento e cor de fundo.
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagem do logo.
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White)
            )

            // Campo de texto para o nome completo.
            OutlinedTextField(value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Nome Completo") })

            // Campo de texto para o email.
            OutlinedTextField(value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Email") })

            // Campo de texto para a senha, com visualização oculta.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Senha") },
                visualTransformation = PasswordVisualTransformation()
            )

            // Campo de texto para confirmar a senha.
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Confirme sua senha") },
                visualTransformation = PasswordVisualTransformation(),

                // 'isError' exibe um estado de erro se as senhas não coincidirem.
                isError = password.isNotEmpty() && confirm.isNotEmpty() && password != confirm
            )

            // Espaçador para separar os campos dos botões.
            Spacer(modifier = Modifier.size(16.dp))

            // Exibe um indicador de progresso ou os botões de ação, dependendo do estado.
            if (uiState.value == SignUpState.Loading) {
                CircularProgressIndicator()
            } else {
                // Botão de cadastro.
                Button(
                    onClick = {
                        // Chama a função de cadastro no ViewModel.
                        viewModel.signUp(name, email, password)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    // Habilita o botão apenas se todos os campos estiverem preenchidos e as senhas forem iguais.
                    enabled = name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirm.isNotEmpty() && password == confirm
                ) {
                    Text(text = "Cadastre-se")
                }

                // Botão de texto para voltar para a tela de login.
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(text = "Já tem uma conta? Faça Login")
                }
            }
        }
    }
}