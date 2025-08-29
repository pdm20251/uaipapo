package com.example.uaipapo.feature.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.uaipapo.R
import com.example.uaipapo.model.Message
//import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(@ApplicationContext val context: Context) : ViewModel() {

    /**
     * Declaração de um StateFlow mutável privado que armazena a lista de mensagens.
     * O uso de uma lista vazia como valor inicial previne erros de estado nulo.
     */
    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    /**
     * Expõe a lista de mensagens como um StateFlow de leitura, seguindo a convenção.
     */
    val message = _messages.asStateFlow()

    // Instância do Firebase Realtime Database.
    private val db = Firebase.database

    /**
     * Envia uma mensagem de texto para um canal específico.
     * @param channelID O ID do canal para onde a mensagem será enviada.
     * @param messageText O conteúdo da mensagem de texto.
     */
    fun sendMessage(channelID: String, messageText: String?) {
        // Cria uma nova instância da classe Message com os dados do remetente.
        val message = Message(
            db.reference.push().key ?: UUID.randomUUID().toString(),
            Firebase.auth.currentUser?.uid ?: "",
            messageText,
            System.currentTimeMillis(),
            Firebase.auth.currentUser?.displayName ?: "",
            null,
            null
        )

        // Salva a mensagem no Firebase Realtime Database.
        db.reference.child("messages").child(channelID).push().setValue(message)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // Se a mensagem for enviada com sucesso, envia uma notificação push.
                    { /* TODO */ }
                }
            }
    }

    /**
     * Inicia a escuta por novas mensagens em um canal específico.
     * @param channelID O ID do canal a ser monitorado.
     */
    fun listenForMessages(channelID: String) {
        // Define um listener para monitorar o nó de mensagens no Firebase.
        db.getReference("messages").child(channelID).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Quando novos dados são recebidos, converte-os em uma lista de objetos Message.
                    val list = mutableListOf<Message>()
                    snapshot.children.forEach { data ->
                        val message = data.getValue(Message::class.java)
                        message?.let {
                            list.add(it)
                        }
                    }
                    // Atualiza o StateFlow com a nova lista, o que notifica a UI.
                    _messages.value = list
                }

                override fun onCancelled(error: DatabaseError) {
                    // Tratamento de erro quando o listener é cancelado.
                }
            })
        // Adiciona o usuário a um tópico de notificação e o registra no canal.
        registerUserIdtoChannel(channelID)
    }

    /**
     * Obtém os IDs de todos os usuários em um canal.
     * @param channelID O ID do canal.
     * @param callback A função de retorno a ser executada com a lista de IDs de usuários.
     */
    fun getAllUserEmails(channelID: String, callback: (List<String>) -> Unit) {
        // Referencia o nó de usuários do canal.
        val ref = db.reference.child("channels").child(channelID).child("users")
        // Adiciona um listener para obter os dados uma única vez.
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userIds = snapshot.children.map { it.value.toString() }
                callback.invoke(userIds)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.invoke(emptyList())
            }
        })
    }

    /**
     * Registra o ID do usuário atual em um canal, se ele ainda não estiver lá.
     * @param channelID O ID do canal.
     */
    fun registerUserIdtoChannel(channelID: String) {
        val currentUser = Firebase.auth.currentUser
        val ref = db.reference.child("channels").child(channelID).child("users")
        ref.child(currentUser?.uid ?: "").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Adiciona o usuário apenas se ele não estiver registrado.
                    if (!snapshot.exists()) {
                        ref.child(currentUser?.uid ?: "").setValue(currentUser?.email)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
        )
    }


    /**
     * Obtém um token de acesso para autenticação com as APIs do Google.
     * @return O token de acesso.

    private fun getAccessToken(): String {
        // Abre o arquivo de credenciais do serviço.
        val inputStream = context.resources.openRawResource(R.raw.chatter_key)
        // Cria as credenciais com escopo para o Firebase Cloud Messaging.
        val googleCreds = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        // Retorna o token de acesso.
        return googleCreds.refreshAccessToken().tokenValue
    }*/
}