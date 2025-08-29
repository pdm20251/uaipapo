package com.example.uaipapo.ui.feature.auth.signin

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.uaipapo.R
import com.example.uaipapo.ui.feature.auth.signin.SignInViewModel.SignInState


@Composable
fun OTPScreen(navController: NavController, phoneNumber: String) {

    val viewModel: SignInViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()
    var otpCode by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = uiState.value) {

        when (uiState.value) {
            is SignInState.Verified -> {
                navController.navigate("home")
            }

            is SignInState.Error -> {
                Toast.makeText(context, "Verification Failed", Toast.LENGTH_SHORT).show()
            }

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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White)
            )

            Text(text = "We sent a verification code to $phoneNumber", fontWeight = FontWeight.Bold)

            OutlinedTextField(value = otpCode,
                onValueChange = {
                    if (it.length == 6) {
                        otpCode = it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Verification Code") },
                placeholder = { Text(text = "654321")}
            )

            Spacer(modifier = Modifier.size(16.dp))

            if (uiState.value == SignInState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.signIn(phoneNumber) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = otpCode.isNotEmpty() && (uiState.value == SignInState.Nothing || uiState.value == SignInState.Error)
                ) {
                    Text(text = "Next")
                }

                TextButton(onClick = { }) {
                    Text(text = "Resend code")
                }

                Spacer(modifier = Modifier.size(4.dp))

                TextButton(onClick = { navController.navigate("signin") }) {
                    Text(text = "Wrong number?", fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOTPScreen() {
    OTPScreen(navController = rememberNavController(), "+5534999999999")
}