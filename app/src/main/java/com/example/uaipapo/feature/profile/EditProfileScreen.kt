package com.example.uaipapo.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.uaipapo.feature.auth.signin.AuthViewModel
import com.example.uaipapo.feature.profile.ProfileViewModel
import com.example.uaipapo.ui.theme.DarkGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val viewModel: ProfileViewModel = hiltViewModel()

    val user by viewModel.currentUser.collectAsState()
    val nameState = remember { mutableStateOf(TextFieldValue(user?.name ?: "")) }
    val isSaving = remember { mutableStateOf(false) }
    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }

    // Launcher para selecionar uma imagem da galeria
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri.value = uri
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Imagem de perfil (selecionada ou padrão)
            val currentProfileImage = selectedImageUri.value ?: user?.profileImageUrl

            if (currentProfileImage != null && currentProfileImage != "null") { // Verifica se há uma imagem válida
                AsyncImage(
                    model = currentProfileImage,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            } else {
                // Se não houver foto, exibe o ícone com a borda
                Card(
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.Gray), // Borda cinza
                    modifier = Modifier
                        .size(120.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    colors = CardDefaults.cardColors(containerColor = DarkGrey) // Cor de fundo para o Card
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit, // Ícone de adicionar foto
                            contentDescription = "Adicionar foto de perfil",
                            tint = Color.Gray,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Inicia nome do usuário como o definido na tela de cadastro
            val nameState = remember { mutableStateOf(TextFieldValue(user?.name ?: "")) }
            LaunchedEffect(user) {
                nameState.value = TextFieldValue(user?.name ?: "")
            }

            TextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                label = { Text("Nome de Usuário", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = DarkGrey,
                    unfocusedContainerColor = DarkGrey,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isSaving.value = true
                    viewModel.updateUserProfile(nameState.value.text, selectedImageUri.value) { success ->
                        isSaving.value = false
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving.value) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Salvar", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = Color.White)
            }
        }
    }
}