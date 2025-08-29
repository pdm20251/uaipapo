package com.example.uaipapo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Define o tema do aplicativo, aplicando cores e tipografia do Material Design.
 *
 * @param darkTheme Determina se o tema escuro deve ser aplicado. O valor padrão usa a configuração
 * do sistema.
 * @param dynamicColor Ativa as cores dinâmicas para dispositivos Android 12+ (S). O valor padrão
 * está ativado.
 * @param content O conteúdo da UI para o qual o tema será aplicado.
 */
@Composable
fun UaipapoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Define o esquema de cores com base nas opções de cor dinâmica e tema escuro.
    val colorScheme = when {
        // Usa cores dinâmicas se o dispositivo for Android 12+ e a opção estiver ativada.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Usa o esquema de cores padrão para o tema escuro.
        darkTheme -> darkColorScheme() // Assumindo uma função para o tema escuro padrão
        // Usa o esquema de cores padrão para o tema claro.
        else -> lightColorScheme() // Assumindo uma função para o tema claro padrão
    }

    // Aplica o tema Material Design com o esquema de cores e tipografia definidos.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assumindo uma variável de tipografia
        content = content
    )
}