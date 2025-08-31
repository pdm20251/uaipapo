package com.example.uaipapo.feature.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Adicionado para ImageRequest.Builder
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest // Adicionado para ImageRequest.Builder
import com.example.uaipapo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageViewScreen(navController: NavController, imageUrl: String?) {
    // Log da URL recebida (este já existia e é bom manter)
    LaunchedEffect(imageUrl) {
        Log.d("FullScreenImage", "Image URL received by Screen: $imageUrl")
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { /* Sem título */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                // Log da URL um pouco antes de ser usada pelo Coil
                Log.d("FullScreenImage", "Image URL being passed to Coil: $imageUrl")

                val context = LocalContext.current
                val imageRequest = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .listener(onError = { _, result ->
                        Log.e("FullScreenImageCoil", "Coil Error: ${result.throwable.localizedMessage}", result.throwable)
                    }, onSuccess = { _, result ->
                        Log.d("FullScreenImageCoil", "Coil Success. DataSource: ${result.dataSource}")
                    })
                    .build()

                SubcomposeAsyncImage(
                    model = imageRequest, // Usando o ImageRequest construído
                    contentDescription = "Imagem em tela cheia",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color.White
                        )
                    },
                    error = { errorState ->
                        // Este log já existia e é útil para o erro do SubcomposeAsyncImage em si
                        Log.e("FullScreenImage", "SubcomposeAsyncImage Error: ${errorState.result.throwable.localizedMessage}", errorState.result.throwable)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_broken_image_generic),
                                contentDescription = "Erro ao carregar imagem",
                                tint = Color.Gray,
                                modifier = Modifier.size(100.dp)
                            )
                            Text("Erro ao carregar imagem", color = Color.Gray)
                        }
                    }
                )
            } else {
                Log.w("FullScreenImage", "Image URL is null or blank when trying to display.")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                     Icon(
                        painter = painterResource(id = R.drawable.ic_broken_image_generic), // Substitua por seu drawable
                        contentDescription = "URL da imagem inválida",
                        tint = Color.Gray,
                        modifier = Modifier.size(100.dp)
                    )
                    Text("URL da imagem inválida", color = Color.Gray)
                }
            }
        }
    }
}
