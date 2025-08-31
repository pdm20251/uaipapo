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
import com.example.uaipapo.feature.auth.signin.SignInScreen
import com.example.uaipapo.feature.auth.signup.SignUpScreen
import com.example.uaipapo.feature.home.HomeScreen
import com.example.uaipapo.feature.chat.ChatScreen
import com.example.uaipapo.feature.profile.EditProfileScreen
import com.example.uaipapo.feature.chat.FullScreenImageViewScreen
import com.example.uaipapo.feature.chat.ScreenRoutes
import java.net.URLDecoder // RE-ADICIONADO

@Composable
fun MainApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "login") {

            composable("login") {
                SignInScreen(navController)
            }
            composable("signup") {
                SignUpScreen(navController)
            }
            composable("home") {
                HomeScreen(navController)
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
                EditProfileScreen(navController)
            }

            // ROTA PARA IMAGEM EM TELA CHEIA REVERTIDA:
            composable(
                route = "${ScreenRoutes.FULL_SCREEN_IMAGE_ROUTE_PREFIX}/{imageUrl}",
                arguments = listOf(navArgument("imageUrl") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedImageUrl = backStackEntry.arguments?.getString("imageUrl")
                // REVERTIDO para usar URLDecoder.decode
                val imageUrl = encodedImageUrl?.let { URLDecoder.decode(it, "UTF-8") }
                FullScreenImageViewScreen(navController = navController, imageUrl = imageUrl)
            }
        }
    }
}
