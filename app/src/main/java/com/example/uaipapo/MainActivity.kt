package com.example.uaipapo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.codewithfk.chatter.ui.theme.ChatterTheme
import com.example.uaipapo.ui.theme.uaiPapoTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Firebase.firestore

        setContent {
            uaiPapoTheme {
                MainApp()
            }
        }
    }
}