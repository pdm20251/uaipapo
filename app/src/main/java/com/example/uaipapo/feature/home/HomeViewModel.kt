package com.example.uaipapo.feature.home

import androidx.lifecycle.ViewModel
import com.example.uaipapo.model.Channel
import com.google.firebase.Firebase
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel que gerencia a lógica de negócio e o estado da tela principal (`Home`).
 * Lida com a busca e a criação de canais de chat, usando o Firebase Realtime Database.
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    // Instância do Firebase Realtime Database para interagir com os dados.
    private val firebaseDatabase = Firebase.database

    // MutableStateFlow interno que armazena a lista de canais.
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())

    // Expõe a lista de canais como um StateFlow de leitura para a UI.
    val channels = _channels.asStateFlow()

    /**
     * O bloco `init` é executado na inicialização do ViewModel.
     * Ele chama a função para buscar os canais assim que o ViewModel é criado.
     */
    init {
        getChannels()
    }

    /**
     * Busca todos os canais do Firebase Realtime Database.
     * Atualiza o StateFlow com a lista de canais encontrados.
     */
    private fun getChannels() {
        // Obtém uma referência ao nó "channel" e busca os dados uma única vez.
        firebaseDatabase.getReference("channel").get().addOnSuccessListener {
            val list = mutableListOf<Channel>()
            // Itera sobre os dados recebidos para criar uma lista de objetos Channel.
            it.children.forEach { data ->
                val channel = Channel(data.key!!, data.value.toString())
                list.add(channel)
            }
            // Atualiza o valor do StateFlow, o que notifica a UI sobre a mudança.
            _channels.value = list
        }
    }

    /**
     * Adiciona um novo canal ao Firebase Realtime Database.
     * @param name O nome do novo canal.
     */
    fun addChannel(name: String) {
        // Gera uma chave única para o novo canal.
        val key = firebaseDatabase.getReference("channel").push().key
        // Salva o nome do canal usando a chave gerada.
        firebaseDatabase.getReference("channel").child(key!!).setValue(name).addOnSuccessListener {
            // Após o sucesso, chama `getChannels()` novamente para atualizar a lista na UI.
            getChannels()
        }
    }
}