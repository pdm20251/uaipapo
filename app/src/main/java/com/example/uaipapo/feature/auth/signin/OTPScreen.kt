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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardType.Companion.Number
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.uaipapo.R
import com.example.uaipapo.ui.theme.BrightRed
import com.example.uaipapo.ui.theme.LighGray
import com.example.uaipapo.ui.theme.White


@Composable
fun OTPScreen(navController: NavController, viewModel: SignInViewModel, phoneNumber: String) {

    val uiState = viewModel.state.collectAsState()
    var otpCode by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = uiState.value) {

        when (uiState.value) {
            is SignInState.Success -> {
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

            Spacer(Modifier.size(16.dp))

            Text(text = "Verification code sent to +$phoneNumber", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

            OutlinedTextField(value = otpCode,
                onValueChange = {
                    if (it.length <= 6) {
                        otpCode = it
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f),
                label = { Text(text = "Verification Code") },
                placeholder = { Text(text = "Ex.: 654321")},
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = White,
                    focusedContainerColor = White,
                    errorContainerColor = White,
                    focusedLabelColor = BrightRed,
                    focusedIndicatorColor = BrightRed,
                    cursorColor = BrightRed,
                    focusedTextColor = BrightRed
                )
            )

            Spacer(modifier = Modifier.size(16.dp))

            if (uiState.value == SignInState.Loading) {
                CircularProgressIndicator(color = BrightRed)
            } else {
                Button(
                    onClick = { viewModel.verifyOtpAndSignIn(otpCode) },
                    modifier = Modifier.fillMaxWidth(0.6f),
                    enabled = otpCode.length == 6 && (uiState.value == SignInState.Nothing || uiState.value == SignInState.Error || uiState.value == SignInState.CodeSent),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightRed, disabledContainerColor = LighGray)
                ) {
                    Text(text = "Next")
                }

                TextButton(onClick = { }) {
                    Text(text = "Resend code in s", color = BrightRed)
                }

                TextButton(onClick = { navController.navigate("signin") }) {
                    Text(text = "Wrong number?", fontStyle = FontStyle.Italic, color = BrightRed)
                }
            }
        }
    }
}