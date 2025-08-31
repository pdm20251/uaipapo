package com.example.uaipapo.model

import java.util.Date // Necessário para o novo campo timestamp

/**
 * Classe de dados que representa uma mensagem no aplicativo.
 *
 * @param id Identificador único da mensagem.
 * @param senderId ID do usuário que enviou a mensagem.
 * @param senderName Nome de exibição do remetente.
 * @param senderPhotoUrl URL da imagem de perfil do remetente.
 * @param message Conteúdo da mensagem de texto. Pode ser nulo se for uma mensagem de mídia.
 * @param timestamp Data e hora em que a mensagem foi enviada/criada.
 * @param imageUrl URL de uma imagem anexa. Usado se mediaType for "image". (Pode ser depreciado em favor de mediaUrl)
 * @param mediaUrl URL genérica para qualquer tipo de mídia anexada (imagem, documento, áudio, vídeo).
 * @param mediaType Tipo da mídia anexada (ex: "image", "document", "audio", "video").
 * @param fileName Nome original do arquivo para mídias como documentos, áudios.
 * @param fileSize Tamanho do arquivo em bytes (opcional, mas útil).
 * @param channelId O ID do canal ao qual esta mensagem pertence. (Re-adicionado para consistência com o ViewModel)
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String? = null,
    val message: String? = null, // Mensagem de texto, pode ser nula para mídia
    val timestamp: Date = Date(), // Usar java.util.Date para melhor integração com Firestore
    
    // Campos específicos para imagem (podem ser mantidos para compatibilidade ou migrados para mediaUrl)
    val imageUrl: String? = null, 

    // Campos genéricos para mídia
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "image", "document", "audio", "video"
    val fileName: String? = null,
    val fileSize: Long? = null, // Tamanho em bytes

    // Adicionando de volta para clareza e consistência com o que o ViewModel espera ao enviar
    val channelId: String = ""
)
