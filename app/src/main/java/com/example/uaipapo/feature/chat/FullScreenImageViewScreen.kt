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
import androidx.compose.runtime.LaunchedEffect // Mantido para o log de fixedImageUrl
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.uaipapo.R
// kotlinx.coroutines.Dispatchers // Removido se não mais necessário
// kotlinx.coroutines.withContext // Removido se não mais necessário
// okhttp3.OkHttpClient // Removido
// okhttp3.Request as OkHttpRequest // Removido

// Função utilitária para codificar as barras no caminho do objeto da URL do Firebase (MANTIDA)
fun encodeSlashesInObjectPath(url: String?): String? {
    if (url.isNullOrBlank()) {
        return url
    }
    try {
        val objectPathMarker = "/o/"
        val queryMarker = "?"

        val objectPathStartIndex = url.indexOf(objectPathMarker)
        if (objectPathStartIndex == -1) {
            Log.w("UrlEncode", "Marker '$objectPathMarker' not found in URL: $url")
            return url
        }

        val actualObjectPathStartIndex = objectPathStartIndex + objectPathMarker.length
        val queryStartIndex = url.indexOf(queryMarker, startIndex = actualObjectPathStartIndex)
        val basePath = url.substring(0, actualObjectPathStartIndex)
        val objectPath: String
        val queryParams: String

        if (queryStartIndex == -1) {
            objectPath = url.substring(actualObjectPathStartIndex)
            queryParams = ""
        } else {
            objectPath = url.substring(actualObjectPathStartIndex, queryStartIndex)
            queryParams = url.substring(queryStartIndex)
        }

        val encodedObjectPath = objectPath.replace("/", "%2F")
        val newUrl = basePath + encodedObjectPath + queryParams
        Log.d("UrlEncode", "Original URL: $url")
        Log.d("UrlEncode", "Encoded URL : $newUrl")
        return newUrl
    } catch (e: Exception) {
        Log.e("UrlEncode", "Error encoding slashes in URL: $url", e)
        return url
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageViewScreen(navController: NavController, imageUrl: String?) {
    val fixedImageUrl by remember(imageUrl) {
        derivedStateOf {
            encodeSlashesInObjectPath(imageUrl)
        }
    }

    LaunchedEffect(fixedImageUrl) {
        Log.d("FullScreenImage", "Effective Image URL for Coil: $fixedImageUrl")
    }

    // --- Bloco OkHttpTest REMOVIDO ---

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
            if (!fixedImageUrl.isNullOrBlank()) {
                val context = LocalContext.current
                val imageRequest = ImageRequest.Builder(context)
                    .data(fixedImageUrl)
                    .listener(onError = { _, result ->
                        Log.e("FullScreenImageCoil", "Coil Error: ${result.throwable.localizedMessage}", result.throwable)
                    }, onSuccess = { _, result ->
                        Log.d("FullScreenImageCoil", "Coil Success. DataSource: ${result.dataSource}")
                    })
                    .build()

                SubcomposeAsyncImage(
                    model = imageRequest,
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
                Log.w("FullScreenImage", "Image URL (original or fixed) is null or blank when trying to display.")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                     Icon(
                        painter = painterResource(id = R.drawable.ic_broken_image_generic),
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
