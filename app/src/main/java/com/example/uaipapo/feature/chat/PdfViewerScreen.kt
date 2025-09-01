package com.example.uaipapo.feature.chat

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.github.barteksc.pdfviewer.PDFView // Import para PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

// A função encodeSlashesInObjectPath FOI REMOVIDA DESTE ARQUIVO.
// Ela será usada a partir de FullScreenImageViewScreen.kt (ou de um arquivo utilitário comum se o movermos).

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(navController: NavController, pdfUrl: String?) {
    val context = LocalContext.current

    // Agora deve usar a encodeSlashesInObjectPath do mesmo pacote (de FullScreenImageViewScreen.kt)
    val fixedPdfUrl by remember(pdfUrl) {
        derivedStateOf {
            encodeSlashesInObjectPath(pdfUrl)
        }
    }

    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(fixedPdfUrl) {
        if (fixedPdfUrl.isNullOrBlank()) {
            errorMessage = "URL do PDF inválida."
            pdfFile = null
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null
        pdfFile = null

        Log.d("PdfViewerScreen", "Attempting to download PDF from: $fixedPdfUrl")

        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(fixedPdfUrl!!).build() // Este .build() deve resolver agora
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val inputStream: InputStream? = response.body?.byteStream()
                    if (inputStream != null) {
                        val fileName = "temp_pdf_${UUID.randomUUID()}.pdf"
                        val tempFile = File(context.cacheDir, fileName)
                        tempFile.createNewFile()
                        
                        val outputStream = FileOutputStream(tempFile)
                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        pdfFile = tempFile
                        Log.d("PdfViewerScreen", "PDF downloaded to: ${tempFile.absolutePath}")
                    } else {
                        errorMessage = "Falha ao obter o conteúdo do PDF."
                        Log.e("PdfViewerScreen", "PDF download failed: InputStream is null.")
                    }
                } else {
                    errorMessage = "Falha no download do PDF: ${response.code} ${response.message}"
                    Log.e("PdfViewerScreen", "PDF download failed: ${response.code} ${response.message} - Body: ${response.body?.string()}")
                }
                response.close()
            } catch (e: Exception) {
                errorMessage = "Erro ao baixar PDF: ${e.localizedMessage}"
                Log.e("PdfViewerScreen", "Exception during PDF download", e)
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visualizador de PDF") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (pdfFile != null && pdfFile!!.exists()) {
                AndroidView(
                    factory = { ctx ->
                        PDFView(ctx, null).apply { // Este PDFView ainda depende da resolução da dependência
                            // Configurações iniciais
                        }
                    },
                    update = { view ->
                        Log.d("PdfViewerScreen", "Updating PDFView with file: ${pdfFile!!.absolutePath}")
                        view.fromFile(pdfFile)
                            .password(null)
                            .defaultPage(0)
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .onLoad { nbPages ->
                                Log.d("PdfViewerScreen", "PDF loaded. Total pages: $nbPages")
                            }
                            .onError { throwable ->
                                Log.e("PdfViewerScreen", "Error loading PDF into PDFView", throwable)
                                errorMessage = "Erro ao carregar PDF: ${throwable.localizedMessage}"
                            }
                            .load()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!fixedPdfUrl.isNullOrBlank()){
                 Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Text("Não foi possível carregar o PDF.")
                    Text("Verifique sua conexão ou tente novamente mais tarde.")
                }
            }
             else {
                Text("Nenhum PDF para exibir.", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
