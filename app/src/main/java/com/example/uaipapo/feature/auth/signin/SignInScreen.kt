package com.example.uaipapo.feature.auth.signin

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.uaipapo.R

/**
 * Tela de login, usando Jetpack Compose para a UI.
 * Gerencia a entrada do usuário e o estado da autenticação.
 */
@Composable
fun SignInScreen(navController: NavController) {

    // Instancia e gerencia o ViewModel para a lógica de negócio.
    val viewModel: SignInViewModel = hiltViewModel()

    // Coleta o estado da UI do ViewModel.
    val uiState = viewModel.state.collectAsState()

    // Estado do campo de email.
    var email by remember { mutableStateOf("") }

    // Estado do campo de senha.
    var password by remember { mutableStateOf("") }

    // Obtém o contexto atual da UI.
    val context = LocalContext.current

    /**
     * Efeito colateral para reagir às mudanças de estado.
     * Navega para a tela principal ou exibe um erro.
     */
    LaunchedEffect(key1 = uiState.value) {
        when (uiState.value) {
            is SignInState.Success -> navController.navigate("home")
            is SignInState.Error -> Toast.makeText(context, "Não conseguimos acessar sua conta!", Toast.LENGTH_SHORT).show()
            else -> {}
        }
    }

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
            // Componente de imagem.
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White)
            )

            // Campo de texto para email.
            OutlinedTextField(value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Email") })

            // Campo de texto para senha, ocultando o texto.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Senha") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.size(16.dp))

            // Exibe indicador de progresso ou botões, dependendo do estado.
            if (uiState.value == SignInState.Loading) {
                CircularProgressIndicator()
            } else {
                // Botão de login.
                Button(
                    onClick = { viewModel.signIn(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotEmpty() && password.isNotEmpty() && (uiState.value == SignInState.Nothing || uiState.value == SignInState.Error)
                ) {
                    Text(text = "Sign In")
                }

                // Botão para navegar para a tela de cadastro.
                TextButton(onClick = { navController.navigate("signup") }) {
                    Text(text = "Cadastrse-se")
                }
            }
        }
    }
}

/**
 * Pré-visualização da tela de login.
 */
@Preview(showBackground = true)
@Composable
fun PreviewSignInScreen() {
    SignInScreen(navController = rememberNavController())
}