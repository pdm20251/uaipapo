package com.example.uaipapo.model

/**
 * Classe de dados que representa um canal de chat.
 *
 * @param id O identificador único do canal.
 * Recebe um valor padrão para facilitar a criação de novos canais
 * e a desserialização de dados do Firestore.
 * @param name O nome do canal, obrigatório ao criar uma instância.
 * @param createdAt O timestamp de criação do canal, em milissegundos.
 * O valor padrão é o horário atual do sistema.
 */
data class Channel(
    val id: String = "",
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)