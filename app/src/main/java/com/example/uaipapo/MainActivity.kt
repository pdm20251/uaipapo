package com.example.uaipapo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.uaipapo.feature.auth.signin.AuthViewModel
import com.example.uaipapo.ui.theme.UaipapoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = AuthViewModel()

        enableEdgeToEdge()
        setContent {
            UaipapoTheme {
                MainApp(authViewModel)
            }
        }
    }
}