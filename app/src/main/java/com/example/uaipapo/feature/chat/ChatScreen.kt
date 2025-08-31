package com.example.uaipapo.feature.chat

import java.net.URLEncoder // RE-ADICIONADO
import java.io.UnsupportedEncodingException // RE-ADICIONADO
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.uaipapo.R
import com.example.uaipapo.model.Message
import com.example.uaipapo.ui.theme.DarkGrey
import com.example.uaipapo.ui.theme.Purple
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import android.util.Log

object ScreenRoutes {
    const val FULL_SCREEN_IMAGE_ROUTE_PREFIX = "full_screen_image"
    // REVERTIDO para usar URLEncoder
    fun fullScreenImageRoute(imageUrl: String): String {
        return try {
            "$FULL_SCREEN_IMAGE_ROUTE_PREFIX/${URLEncoder.encode(imageUrl, "UTF-8")}"
        } catch (e: UnsupportedEncodingException) {
            Log.e("ScreenRoutes", "Error encoding URL: $imageUrl", e)
            "$FULL_SCREEN_IMAGE_ROUTE_PREFIX/encoding_error"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, channelId: String, channelName: String) {
    val viewModel: ChatViewModel = hiltViewModel()
    val isUploading = remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: android.net.Uri? ->
            uri?.let {
                isUploading.value = true
                viewModel.sendMediaMessage(it, channelId, "image") {
                    isUploading.value = false
                }
            }
        }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: android.net.Uri? ->
            uri?.let {
                isUploading.value = true
                viewModel.sendMediaMessage(it, channelId, "document") {
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
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LaunchedEffect(key1 = channelId) {
                    viewModel.listenForMessages(channelId)
                }

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
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkGrey,
                            unfocusedContainerColor = DarkGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        )
                    )
                }

                val messages = viewModel.filteredMessages.collectAsState()
                ChatMessages(
                    navController = navController,
                    channelName = channelName,
                    messages = messages.value,
                    onSendMessage = { message ->
                        viewModel.sendMessage(channelId, message)
                    },
                    onAttachPhotoClicked = {
                        imagePickerLauncher.launch("image/*")
                    },
                    onAttachDocumentClicked = {
                        documentPickerLauncher.launch("*/*")
                    },
                    searchText = searchText,
                    isUploading = isUploading.value
                )
            }
        })
}

@Composable
fun ChatMessages(
    navController: NavController,
    channelName: String,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onAttachPhotoClicked: () -> Unit,
    onAttachDocumentClicked: () -> Unit,
    isUploading: Boolean,
    searchText: String
) {
    val hideKeyboardController = LocalSoftwareKeyboardController.current
    val msg = remember { mutableStateOf("") }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                ChatBubble(
                    navController = navController,
                    message = message,
                    searchText = searchText
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGrey)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(onClick = { showAttachmentMenu = true }) {
                    Image(
                        painter = painterResource(id = R.drawable.attach),
                        contentDescription = "Anexar mídia"
                    )
                }
                DropdownMenu(
                    expanded = showAttachmentMenu,
                    onDismissRequest = { showAttachmentMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Imagem") },
                        onClick = {
                            showAttachmentMenu = false
                            onAttachPhotoClicked()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Documento") },
                        onClick = {
                            showAttachmentMenu = false
                            onAttachDocumentClicked()
                        }
                    )
                }
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
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkGrey,
                    unfocusedContainerColor = DarkGrey,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray
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

@Composable
fun ChatBubble(
    navController: NavController,
    message: Message,
    searchText: String
) {
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
    val bubbleColor = if (isCurrentUser) Purple else DarkGrey
    val senderDisplayName = if (message.senderName.isNullOrBlank() || message.senderName.contains("@")) {
        message.senderName?.substringBefore("@") ?: "Usuário"
    } else {
        message.senderName
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        val alignment = if (!isCurrentUser) Alignment.CenterStart else Alignment.CenterEnd
        Column(
            modifier = Modifier
                .align(alignment)
                .then(if(isCurrentUser) Modifier.padding(start = 48.dp) else Modifier.padding(end=0.dp)),
            horizontalAlignment = if (!isCurrentUser) Alignment.Start else Alignment.End
        ) {
            if (!isCurrentUser) {
                Text(
                    text = senderDisplayName,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 56.dp, bottom = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom
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
                            color = bubbleColor, shape = RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                                bottomStart = if (isCurrentUser && !message.senderPhotoUrl.isNullOrEmpty()) 0.dp else 8.dp,
                                bottomEnd = if (!isCurrentUser && !message.senderPhotoUrl.isNullOrEmpty()) 0.dp else 8.dp
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .widthIn(min = 40.dp)
                ) {
                    when {
                        message.mediaType == "image" && !message.mediaUrl.isNullOrBlank() -> {
                            AsyncImage(
                                model = message.mediaUrl,
                                contentDescription = "Imagem anexada",
                                modifier = Modifier
                                    .sizeIn(maxWidth = 240.dp, maxHeight = 240.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        message.mediaUrl?.let { navController.navigate(ScreenRoutes.fullScreenImageRoute(it)) }
                                    },
                                contentScale = ContentScale.Fit
                            )
                        }
                        message.imageUrl != null && message.mediaType == null -> {
                            AsyncImage(
                                model = message.imageUrl,
                                contentDescription = "Imagem anexada",
                                modifier = Modifier
                                    .sizeIn(maxWidth = 240.dp, maxHeight = 240.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        message.imageUrl?.let { navController.navigate(ScreenRoutes.fullScreenImageRoute(it)) }
                                    },
                                contentScale = ContentScale.Fit
                            )
                        }
                        message.mediaType == "document" && !message.fileName.isNullOrBlank() -> {
                            val fileName = message.fileName ?: ""
                            val annotatedString = buildAnnotatedString {
                                if (searchText.isNotBlank() && fileName.contains(searchText, ignoreCase = true)) {
                                    var lastIndex = 0
                                    val matches = searchText.toRegex(RegexOption.IGNORE_CASE).findAll(fileName)
                                    for (match in matches) {
                                        append(fileName.substring(lastIndex, match.range.first))
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, background = Color.Yellow, color = DarkGrey)) {
                                            append(match.value)
                                        }
                                        lastIndex = match.range.last + 1
                                    }
                                    append(fileName.substring(lastIndex))
                                } else {
                                    append(fileName)
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    Log.d("ChatBubble", "Documento clicado: ${message.mediaUrl ?: message.fileName}")
                                }
                            ) {
                                Text(text = annotatedString, color = Color.White)
                            }
                        }
                        !message.message.isNullOrBlank() -> {
                            val messageText = message.message!!
                            val annotatedString = buildAnnotatedString {
                                if (searchText.isNotBlank() && messageText.contains(searchText, ignoreCase = true)) {
                                    var lastIndex = 0
                                    val matches = searchText.toRegex(RegexOption.IGNORE_CASE).findAll(messageText)
                                    for (match in matches) {
                                        append(messageText.substring(lastIndex, match.range.first))
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, background = Color.Yellow, color = DarkGrey )) {
                                            append(match.value)
                                        }
                                        lastIndex = match.range.last + 1
                                    }
                                    append(messageText.substring(lastIndex))
                                } else {
                                    append(messageText)
                                }
                            }
                            Text(text = annotatedString, color = Color.White)
                        }
                        else -> {
                            Text(text = "Conteúdo multimídia", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
