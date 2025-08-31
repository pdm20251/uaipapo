package com.example.uaipapo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.uaipapo.feature.auth.signin.OTPScreen
import com.example.uaipapo.feature.auth.signin.SignInScreen
import com.example.uaipapo.feature.auth.signin.AuthViewModel
import com.example.uaipapo.feature.auth.signin.NameScreen
import com.example.uaipapo.feature.home.HomeScreen
import com.example.uaipapo.feature.chat.ChatScreen
import com.example.uaipapo.feature.profile.EditProfileScreen

@Composable
fun MainApp(authViewModel: AuthViewModel) {
    Surface(modifier = Modifier.fillMaxSize()) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                SignInScreen(navController, authViewModel)
            }

            composable("otp/{phoneNumber}", arguments = listOf(
                navArgument("phoneNumber") {
                    type = NavType.StringType
                }
            )){
                OTPScreen(navController, authViewModel, phoneNumber = it.arguments?.getString("phoneNumber") ?: "")
            }

            composable("name/{phoneNumber}", arguments = listOf(
                navArgument("phoneNumber") {
                    type = NavType.StringType
                }
            )){
                NameScreen(navController, authViewModel, phoneNumber = it.arguments?.getString("phoneNumber") ?: "")
            }

            composable("home") {
                HomeScreen(navController, authViewModel)
            }

            composable("chat/{channelId}&{channelName}", arguments = listOf(
                navArgument("channelId") {
                    type = NavType.StringType
                },
                navArgument("channelName") {
                    type = NavType.StringType
                }
            )) {
                val channelId = it.arguments?.getString("channelId") ?: ""
                val channelName = it.arguments?.getString("channelName") ?: ""
                ChatScreen(navController, channelId,channelName)
            }

            composable("edit_profile") {
                EditProfileScreen(navController, authViewModel)
            }
        }
    }
}