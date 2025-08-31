package com.example.uaipapo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color // Import para Color, se você decidir usar enableLights
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UaiPapoApplication : Application() {

    companion object {
        const val CHANNEL_ID_PRIVATE_MESSAGES = "private_messages_channel"
        const val CHANNEL_ID_GROUP_MESSAGES = "group_messages_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)

            // Canal para Mensagens Privadas
            val privateMessagesChannel = NotificationChannel(
                CHANNEL_ID_PRIVATE_MESSAGES,
                "Mensagens Privadas",
                NotificationManager.IMPORTANCE_HIGH // Alta prioridade para mensagens diretas
            ).apply {
                description = "Notificações para mensagens recebidas em chats privados."
                // Da pra configurar outras propriedades do canal aqui, como:
                // enableLights(true)
                // lightColor = Color.RED
                // enableVibration(true) //Vibração geralmente é habilitada por padrão para IMPORTANCE_HIGH
                // vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            }
            notificationManager.createNotificationChannel(privateMessagesChannel)

            // Canal para Mensagens de Grupo
            val groupMessagesChannel = NotificationChannel(
                CHANNEL_ID_GROUP_MESSAGES,
                "Mensagens de Grupo",
                NotificationManager.IMPORTANCE_DEFAULT // Prioridade padrão para grupos (menos intrusivo)
            ).apply {
                description = "Notificações para mensagens recebidas em grupos."
                // Configurações opcionais:
                // enableLights(false) // Talvez sem luz para grupos?
                // enableVibration(true) // Ou um padrão de vibração diferente
                // vibrationPattern = longArrayOf(100, 200, 100, 200)
            }
            notificationManager.createNotificationChannel(groupMessagesChannel)
        }
    }
}
