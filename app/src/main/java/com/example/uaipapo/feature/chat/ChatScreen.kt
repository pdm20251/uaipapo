package com.example.uaipapo.feature.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.uaipapo.R
import com.example.uaipapo.feature.home.ChannelItem
import com.example.uaipapo.model.Message
import com.example.uaipapo.ui.theme.DarkGrey
import com.example.uaipapo.ui.theme.Purple
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


/**
 * Tela de chat que exibe as mensagens de um canal específico e permite ao usuário enviar novas mensagens.
 *
 * @param navController O controlador de navegação para gerenciar a navegação entre telas.
 * @param channelId O ID do canal de chat atual.
 * @param channelName O nome do canal de chat atual.
 */
@Composable
fun ChatScreen(navController: NavController, channelId: String, channelName: String) {
    // Scaffold fornece a estrutura básica de layout para a tela.
    Scaffold(
        containerColor = Color.Black
    ) {
        // Obtém a instância do ViewModel usando Hilt para gerenciamento do ciclo de vida.
        val viewModel: ChatViewModel = hiltViewModel()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // Um `LaunchedEffect` é usado para iniciar a escuta de mensagens do canal assim que a tela é composta.
            LaunchedEffect(key1 = true) {
                viewModel.listenForMessages(channelId)
            }
            // Coleta o estado das mensagens a partir do ViewModel.
            val messages = viewModel.message.collectAsState()

            // Chama a função composable `ChatMessages` para exibir o conteúdo principal do chat.
            ChatMessages(
                messages = messages.value,
                onSendMessage = { message ->
                    viewModel.sendMessage(channelId, message)
                },
                channelName = channelName
            )
        }
    }
}


/**
 * Componente principal do chat que contém a lista de mensagens e a caixa de entrada de texto.
 *
 * @param channelName O nome do canal.
 * @param messages A lista de mensagens a ser exibida.
 * @param onSendMessage Uma função de callback para enviar uma nova mensagem.
 */
@Composable
fun ChatMessages(
    channelName: String,
    messages: List<Message>,
    onSendMessage: (String) -> Unit
) {
    // Controlador de teclado para ocultá-lo quando a ação `Done` é acionada.
    val hideKeyboardController = LocalSoftwareKeyboardController.current

    // Estado mutável para armazenar o texto da mensagem.
    val msg = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // `LazyColumn` para exibir a lista de mensagens de forma performática.
        LazyColumn(modifier = Modifier.weight(1f)) {
            // Exibe o cabeçalho do canal.
            item {
                ChannelItem(channelName = channelName, Modifier, onClick = {})
            }
            // Itera sobre a lista de mensagens para exibir cada uma como um `ChatBubble`.
            items(messages) { message ->
                ChatBubble(message = message)
            }
        }
        // Caixa de entrada de texto para o usuário digitar e enviar mensagens.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGrey)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = msg.value,
                onValueChange = { msg.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Escreva sua Mensagem") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    hideKeyboardController?.hide()
                }),
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = DarkGrey,
                    unfocusedContainerColor = DarkGrey,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color.White,
                    unfocusedPlaceholderColor = Color.White
                )
            )
            // Botão de envio de mensagem.
            IconButton(onClick = {
                onSendMessage(msg.value)
                msg.value = ""
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "send",
                    tint = Color.White
                )
            }
        }
    }
}


/**
 * Componente de UI para exibir uma única mensagem de chat, em forma de bolha.
 *
 * @param message O objeto `Message` a ser exibido.
 */
@Composable
fun ChatBubble(message: Message) {
    // Determina se a mensagem foi enviada pelo usuário atual.
    val isCurrentUser = message.senderId == com.google.firebase.Firebase.auth.currentUser?.uid
    // Define a cor da bolha com base no remetente.
    val bubbleColor = if (isCurrentUser) {
        Purple
    } else {
        DarkGrey
    }

    // Extrai o nome de usuário (parte do email antes do "@").
    val senderName = message.senderName?.substringBefore("@") ?: "Usuário"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        // Alinha a bolha de mensagem para o lado certo da tela.
        val alignment = if (!isCurrentUser) Alignment.CenterStart else Alignment.CenterEnd
        Column(
            modifier = Modifier
                .padding(8.dp)
                .align(alignment),
            horizontalAlignment = if (!isCurrentUser) Alignment.Start else Alignment.End
        ) {
            // Pequena caixa de texto com o nome do usuário.
            Text(
                text = senderName,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exibe um avatar para mensagens que não são do usuário atual.
                if (!isCurrentUser) {
                    Image(
                        painter = painterResource(id = R.drawable.friend),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                // O contêiner da bolha de mensagem.
                Box(
                    modifier = Modifier
                        .background(
                            color = bubbleColor, shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    // Se a mensagem contiver uma URL de imagem, exibe a imagem.
                    if (message.imageUrl != null) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Caso contrário, exibe o texto da mensagem.
                        Text(text = message.message?.trim() ?: "", color = Color.White)
                    }
                }
            }
        }
    }
}