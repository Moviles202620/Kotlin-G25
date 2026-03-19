package com.example.goatly.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goatly.ui.auth.GoatlyTextField
import com.example.goatly.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            profileViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cambiar contraseña", fontWeight = FontWeight.W800) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
            )
        },
        containerColor = AppColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = AppColors.Surface,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Contraseña actual
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Contraseña actual", fontSize = 16.sp, fontWeight = FontWeight.W700)
                        GoatlyTextField(
                            value = currentPassword,
                            onValueChange = {
                                currentPassword = it
                                currentPasswordError = if (it.isBlank()) "Campo requerido" else null
                            },
                            placeholder = "••••••••",
                            isPassword = true
                        )
                        currentPasswordError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
                    }

                    // Nueva contraseña
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nueva contraseña", fontSize = 16.sp, fontWeight = FontWeight.W700)
                        GoatlyTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                newPasswordError = when {
                                    it.isBlank() -> "Campo requerido"
                                    it.length < 6 -> "Mínimo 6 caracteres"
                                    else -> null
                                }
                                if (confirmPassword.isNotBlank()) {
                                    confirmPasswordError = if (it != confirmPassword) "Las contraseñas no coinciden" else null
                                }
                            },
                            placeholder = "Mínimo 6 caracteres",
                            isPassword = true
                        )
                        newPasswordError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
                    }

                    // Confirmar contraseña
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Confirmar nueva contraseña", fontSize = 16.sp, fontWeight = FontWeight.W700)
                        GoatlyTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                confirmPasswordError = when {
                                    it.isBlank() -> "Campo requerido"
                                    it != newPassword -> "Las contraseñas no coinciden"
                                    else -> null
                                }
                            },
                            placeholder = "Repite tu nueva contraseña",
                            isPassword = true
                        )
                        confirmPasswordError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
                    }
                }
            }

            Button(
                onClick = {
                    currentPasswordError = if (currentPassword.isBlank()) "Campo requerido" else null
                    newPasswordError = when {
                        newPassword.isBlank() -> "Campo requerido"
                        newPassword.length < 6 -> "Mínimo 6 caracteres"
                        else -> null
                    }
                    confirmPasswordError = when {
                        confirmPassword.isBlank() -> "Campo requerido"
                        confirmPassword != newPassword -> "Las contraseñas no coinciden"
                        else -> null
                    }
                    if (currentPasswordError == null && newPasswordError == null && confirmPasswordError == null) {
                        profileViewModel.changePassword(currentPassword, newPassword, confirmPassword, onBack)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.DarkText,
                    contentColor = AppColors.PrimaryYellow
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = AppColors.PrimaryYellow, modifier = Modifier.size(24.dp))
                } else {
                    Text("Actualizar contraseña", fontSize = 18.sp, fontWeight = FontWeight.W800)
                }
            }
        }
    }
}
