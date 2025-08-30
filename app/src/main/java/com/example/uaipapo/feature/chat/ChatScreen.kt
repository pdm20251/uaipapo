package com.example.uaipapo.feature.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, channelId: String, channelName: String) {
    val viewModel: ChatViewModel = hiltViewModel()

    // Estado para controlar a exibição do indicador de progresso
    val isUploading = remember { mutableStateOf(false) }

    // Estados para a barra de busca
    var showSearchBar by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // Launcher para selecionar uma imagem da galeria
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                isUploading.value = true
                viewModel.sendImageMessage(it, channelId) {
                    isUploading.value = false
                }
            }
        }
    )

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text(text = channelName, color = Color.White) },
                actions = {
                    // Botão para mostrar/ocultar a barra de busca
                    IconButton(onClick = {
                        showSearchBar = !showSearchBar
                        if (!showSearchBar) {
                            searchText = ""
                            viewModel.searchMessages("")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Buscar",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        content = { paddingValues -> // O nome do parâmetro deve ser usado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Correção aqui: use o padding fornecido
            ) {
                LaunchedEffect(key1 = true) {
                    viewModel.listenForMessages(channelId)
                }

                // A barra de busca só é exibida se showSearchBar for verdadeiro
                if (showSearchBar) {
                    TextField(
                        value = searchText,
                        onValueChange = { newValue ->
                            searchText = newValue
                            viewModel.searchMessages(newValue)
                        },
                        placeholder = { Text("Buscar mensagens...", color = Color.White) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = TextFieldDefaults.colors().copy(
                            focusedContainerColor = Color.Cyan,
                            unfocusedContainerColor = Color.Cyan,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                val messages = viewModel.filteredMessages.collectAsState() // Use filteredMessages
                ChatMessages(
                    messages = messages.value,
                    onSendMessage = { message ->
                        viewModel.sendMessage(channelId, message)
                    },
                    onAttachImageClicked = {
                        imagePickerLauncher.launch("image/*")
                    },
                    searchText = searchText,
                    channelName = channelName, // Este parâmetro não é mais usado por ChatMessages
                    isUploading = isUploading.value
                )
            }
        })
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
    channelName: String, // Este parâmetro pode ser removido, pois a TopBar já exibe o nome
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onAttachImageClicked: () -> Unit,
    isUploading: Boolean, // Estado de carregamento da imagem anexa
    searchText: String
) {
    val hideKeyboardController = LocalSoftwareKeyboardController.current
    val msg = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            // Removido o item com ChannelItem para evitar a duplicação do nome do canal
            items(messages) { message ->
                ChatBubble(message = message, searchText = searchText)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGrey)
                .padding(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttachImageClicked) {
                Image(
                    painter = painterResource(id = R.drawable.attach),
                    contentDescription = "Anexar imagem"
                )
            }

            TextField(
                value = msg.value,
                onValueChange = { msg.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Escreva...") },
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
            IconButton(onClick = {
                if (msg.value.isNotBlank()) {
                    onSendMessage(msg.value)
                    msg.value = ""
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "send",
                    tint = Color.White
                )
            }
        }

        if (isUploading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Purple
            )
        }
    }
}

/**
 * Componente de UI para exibir uma única mensagem de chat, em forma de bolha.
 *
 * @param message O objeto `Message` a ser exibido.
 */
@Composable
fun ChatBubble(message: Message, searchText: String) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
    val bubbleColor = if (isCurrentUser) Purple else DarkGrey
    val senderName = message.senderName?.substringBefore("@") ?: "Usuário"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        val alignment = if (!isCurrentUser) Alignment.CenterStart else Alignment.CenterEnd
        Column(
            modifier = Modifier
                .padding(8.dp)
                .align(alignment),
            horizontalAlignment = if (!isCurrentUser) Alignment.Start else Alignment.End
        ) {
            Text(
                text = senderName,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isCurrentUser) {
                    AsyncImage(
                        model = message.senderPhotoUrl,
                        contentDescription = "Foto de perfil do remetente",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.friend)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = bubbleColor, shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    if (message.imageUrl != null) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        if (searchText.isNotBlank() && message.message?.contains(searchText, ignoreCase = true) == true) {
                            val annotatedString = androidx.compose.ui.text.buildAnnotatedString {
                                val messageText = message.message ?: ""
                                var lastIndex = 0
                                val matches = searchText.toRegex(RegexOption.IGNORE_CASE).findAll(messageText)

                                for (match in matches) {
                                    append(messageText.substring(lastIndex, match.range.first))
                                    withStyle(style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        background = Color.Yellow
                                    )) {
                                        append(match.value)
                                    }
                                    lastIndex = match.range.last + 1
                                }
                                append(messageText.substring(lastIndex))
                            }
                            Text(text = annotatedString, color = Color.White)
                        } else {
                            Text(text = message.message?.trim() ?: "", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}