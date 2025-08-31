package com.example.uaipapo.feature.auth.signin

import android.app.Activity
import android.util.Log
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.uaipapo.R
import com.example.uaipapo.ui.theme.BrightRed
import com.example.uaipapo.ui.theme.DarkRed
import com.example.uaipapo.ui.theme.LighGray
import com.example.uaipapo.ui.theme.White

@Composable
fun SignInScreen(navController: NavController) {

    val viewModel: SignInViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()
    var phonenumber by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = uiState.value) {

        when (uiState.value) {
            is SignInState.CodeSent -> {
                navController.navigate("otp/${phonenumber.replace("(", "")
                    .replace(")", "")
                    .replace("-", "")
                    .replace(" ", "")}")
            }

            is SignInState.Error -> {
                Toast.makeText(context, "Sign In failed", Toast.LENGTH_SHORT).show()
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

            Spacer(modifier = Modifier.size(30.dp))

            OutlinedTextField(value = "+$phonenumber",
                onValueChange = { newValue ->
                    // Ensure the fixed prefix is always present
                    if (newValue.startsWith("+")) {
                        phonenumber = newValue.substringAfter("+")
                    } else {
                        // If the user somehow removes the prefix, reset
                        phonenumber = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                label = { Text(text = "Phone Number") },
                placeholder = { Text(text = "+55 (34) 99999-9999") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = White,
                    focusedContainerColor = White,
                    errorContainerColor = White,
                    focusedLabelColor = BrightRed,
                    focusedIndicatorColor = BrightRed,
                    cursorColor = BrightRed,
                    focusedTextColor = BrightRed
                ),
            )

            Spacer(modifier = Modifier.size(16.dp))

            if (uiState.value == SignInState.Loading) {
                CircularProgressIndicator(color = BrightRed)
            } else {
                Button(
                    onClick = {
                        val currentActivity = context as? Activity

                        if(currentActivity != null) {
                            viewModel.sendOtp(
                                currentActivity,
                                "+${phonenumber.replace("(", "")
                                    .replace(")", "")
                                    .replace("-", "")
                                    .replace(" ", "")}",
                                false
                            )
                        } else {
                            Log.e("SignInScreen", "Current activity is null.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.6f),
                    enabled = phonenumber.length >= 11 && (uiState.value == SignInState.Nothing || uiState.value == SignInState.Error),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightRed, disabledContainerColor = LighGray)
                ) {
                    Text(text = "Next")
                }

                TextButton(onClick = { navController.navigate("signup") }) {
                    Text(text = "Don't have an account? Sign Up", color = BrightRed)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSignInScreen() {
    SignInScreen(navController = rememberNavController())
}