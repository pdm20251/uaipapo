package com.example.uaipapo.model

/**
 * Classe de dados que representa uma mensagem no aplicativo.
 * Todos os campos têm valores padrão.
 *
 * @param id identificador único da mensagem.
 * @param senderId ID do usuário que enviou a mensagem.
 * @param message conteúdo da mensagem de texto. Nulo se a mensagem for apenas uma imagem.
 * @param createdAt timestamp de criação da mensagem (ms).
 * @param senderName Nome de exibição do remetente.
 * @param senderImage URL da imagem de perfil do remetente. Pode ser nulo.
 * @param imageUrl URL de uma imagem anexa à mensagem. Pode ser nulo.
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val message: String? = "",
    val createdAt: Long = System.currentTimeMillis(),
    val senderName: String = "",
    val senderImage: String? = null,
    val imageUrl: String? = null
)