package com.example.uaipapo

import android.Manifest // Import adicionado
import android.content.pm.PackageManager // Import adicionado
import android.os.Build // Import adicionado
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts // Import adicionado
import androidx.core.content.ContextCompat // Import adicionado
// Seus outros imports (Scaffold, Text, UaipapoTheme, etc.) devem permanecer
import com.example.uaipapo.ui.theme.UaipapoTheme
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log // Import para Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ActivityResultLauncher para solicitar a permissão
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permissão concedida. Você pode querer fazer algo aqui, se necessário.
                Log.d("MainActivity", "Permissão para notificações concedida!")
            } else {
                // Permissão negada. Você pode querer informar ao usuário
                // que algumas funcionalidades podem não estar disponíveis.
                // Ex: Exibir um Toast ou Snackbar.
                Log.d("MainActivity", "Permissão para notificações negada.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        askNotificationPermission() // Solicita a permissão

        setContent {
            UaipapoTheme {
                MainApp() // Supondo que MainApp é seu Composable principal
            }
        }
    }

    private fun askNotificationPermission() {
        // Isso só é necessário para API level 33+ (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permissão já concedida
                Log.d("MainActivity", "Permissão POST_NOTIFICATIONS já concedida.")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Opcional: Mostrar uma UI explicando por que você precisa da permissão.
                // Isso é mostrado se o usuário negou a permissão anteriormente.
                // Você pode exibir um diálogo aqui antes de chamar requestPermissionLauncher.launch()
                Log.d("MainActivity", "Mostrando rationale para permissão POST_NOTIFICATIONS.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Solicitar a permissão diretamente
                Log.d("MainActivity", "Solicitando permissão POST_NOTIFICATIONS.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

