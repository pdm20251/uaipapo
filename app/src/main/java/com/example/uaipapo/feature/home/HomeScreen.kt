package com.example.uaipapo.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.uaipapo.feature.auth.AuthViewModel
import com.example.uaipapo.model.Channel
import com.example.uaipapo.ui.theme.DarkGrey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Tela principal (`Home`) que exibe a lista de canais de chat e permite criar novos.
 *
 * @param navController O controlador de navegação para gerenciar a transição para a tela de chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController,
               authViewModel: AuthViewModel = hiltViewModel()
) {
    // Instancia e gerencia o ViewModel usando Hilt.
    val viewModel = hiltViewModel<HomeViewModel>()

    // Coleta o estado dos canais a partir do ViewModel.
    val channels = viewModel.channels.collectAsState()

    // Variável de estado para controlar a visibilidade do modal de adicionar canal.
    val addChannel = remember { mutableStateOf(false) }

    // Estado do ModalBottomSheet.
    val sheetState = rememberModalBottomSheetState()

    // Variável de estado para o texto de busca.
    val searchText = remember { mutableStateOf("") }

    // Filtra a lista de canais com base no texto de busca.
    val filteredChannels: List<Channel> = remember(channels.value, searchText.value) {
        if (searchText.value.isBlank()) {
            channels.value
        } else {
            channels.value.filter {
                it.name?.contains(searchText.value, ignoreCase = true) ?: false
            }
        }
    }

    // Coleta o estado de login do ViewModel
    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    // Efeito que é acionado quando o estado de login muda.
    LaunchedEffect(key1 = isUserLoggedIn) {
        if (!isUserLoggedIn) {
            // Se o usuário não estiver logado, navegue para a tela de login.
            navController.navigate("login") {
                // Limpa a pilha de navegação para que o usuário não possa voltar
                // para a tela inicial após o logout.
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }
    // O `Scaffold` fornece uma estrutura de layout para a tela.
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "️UaiPapo",
                        color = Color.Gray,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black),
                    )
                },
                actions = {
                    //ABotão de edição de perfil
                    IconButton(onClick = {
                        navController.navigate("edit_profile")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Editar Perfil",
                            tint = Color.White
                        )
                    }
                    // Botão de logout que chama a função do ViewModel
                    Button(onClick = { authViewModel.signOut() }) {
                        Text(text = "Sair")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGrey)
            )
        },
        floatingActionButton = {
            // Botão flutuante para adicionar um novo canal.
            Box(modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Blue)
                .clickable {
                    addChannel.value = true
                }) {
                Text(
                    text = "Adicionar Canal", modifier = Modifier.padding(16.dp), color = Color.White
                )
            }
        },
        containerColor = Color.Black
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            // `LazyColumn` para exibir uma lista de itens.
            LazyColumn {
                item {
                    // Título da tela.
                    Text(
                        text = "🗯️ Suas conversas",
                        color = Color.Gray,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                item {
                    // Campo de texto de busca.
                    TextField(value = searchText.value,
                        onValueChange = { searchText.value = it },
                        placeholder = { Text(text = "Procurar...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .clip(
                                RoundedCornerShape(40.dp)
                            ),
                        textStyle = TextStyle(color = Color.LightGray),
                        colors = TextFieldDefaults.colors().copy(
                            focusedContainerColor = DarkGrey,
                            unfocusedContainerColor = DarkGrey,
                            focusedTextColor = Color.Gray,
                            unfocusedTextColor = Color.Gray,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                            focusedIndicatorColor = Color.Gray
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search, contentDescription = null
                            )
                        })
                }

                // Itera sobre a lista de canais filtrados para criar um `ChannelItem` para cada um.
                items(filteredChannels) { channel ->
                    Column {
                        ChannelItem(
                            channelName = channel.name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                            onClick = {
                                // Navega para a tela de chat com o ID e o nome do canal.
                                navController.navigate("chat/${channel.id}&${channel.name}")
                            })
                    }
                }
            }
        }
    }

    // Exibe o modal de adicionar canal se a variável de estado for verdadeira.
    if (addChannel.value) {
        ModalBottomSheet(
            onDismissRequest = { addChannel.value = false },
            sheetState = sheetState
        ) {
            AddChannelDialog {
                viewModel.addChannel(it)
                addChannel.value = false
            }
        }
    }
}

/**
 * Componente de UI para exibir um item de canal na lista.
 *
 * @param channelName O nome do canal.
 * @param modifier Modificador para aplicar estilos ao componente.
 * @param onClick A função a ser executada ao clicar no item.
 */
@Composable
fun ChannelItem(
    channelName: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .padding(bottom = 16.dp)
                .clickable { onClick() },

            verticalAlignment = Alignment.CenterVertically
        ) {
            // Caixa circular que exibe a primeira letra do nome do canal.
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Yellow.copy(alpha = 0.3f))
            ) {
                Text(
                    text = channelName[0].uppercase(),
                    color = Color.White,
                    style = TextStyle(fontSize = 36.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Nome do canal.
            Text(text = channelName, modifier = Modifier.padding(8.dp), color = Color.White)
        }
    }
}

/**
 * Diálogo em formato de folha modal para adicionar um novo canal.
 *
 * @param onAddChannel A função de callback a ser chamada com o nome do novo canal.
 */
@Composable
fun AddChannelDialog(onAddChannel: (String) -> Unit) {
    // Variável de estado para o nome do canal digitado.
    val channelName = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Adicionar Canal")
        Spacer(modifier = Modifier.padding(8.dp))
        // Campo de texto para o nome do canal.
        TextField(
            value = channelName.value,
            onValueChange = { channelName.value = it },
            label = { Text(text = "Nome do Canal") },
            singleLine = true
        )
        Spacer(modifier = Modifier.padding(8.dp))
        // Botão para adicionar o canal.
        Button(
            onClick = { onAddChannel(channelName.value) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Adicionar")
        }
    }
}
