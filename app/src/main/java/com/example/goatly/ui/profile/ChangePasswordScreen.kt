package com.example.goatly.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goatly.ui.auth.GoatlyTextField
import com.example.goatly.ui.theme.AppColors

private val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27BF]")
private val passwordRegex = Regex("^\\S+$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val isOffline by profileViewModel.isOffline.collectAsState()
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (isOffline) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F0E8))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFF9A7B3A), modifier = Modifier.size(16.dp))
                    Text("Sin conexión — cambio de contraseña no se guardará", fontSize = 13.sp, color = Color(0xFF9A7B3A), fontWeight = FontWeight.W600)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                                currentPasswordError = when {
                                    it.isBlank() -> "Campo requerido"
                                    emojiRegex.containsMatchIn(it) -> "No se permiten emojis"
                                    !passwordRegex.matches(it) -> "No se permiten espacios"
                                    else -> null
                                }
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
                                    emojiRegex.containsMatchIn(it) -> "No se permiten emojis"
                                    !passwordRegex.matches(it) -> "No se permiten espacios"
                                    it.length < 6 -> "Mínimo 6 caracteres"
                                    else -> null
                                }
                                if (confirmPassword.isNotBlank()) {
                                    confirmPasswordError = when {
                                        it != confirmPassword -> "Las contraseñas no coinciden"
                                        else -> null
                                    }
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
                                    emojiRegex.containsMatchIn(it) -> "No se permiten emojis"
                                    !passwordRegex.matches(it) -> "No se permiten espacios"
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
                    currentPasswordError = when {
                        currentPassword.isBlank() -> "Campo requerido"
                        emojiRegex.containsMatchIn(currentPassword) -> "No se permiten emojis"
                        !passwordRegex.matches(currentPassword) -> "No se permiten espacios"
                        else -> null
                    }
                    newPasswordError = when {
                        newPassword.isBlank() -> "Campo requerido"
                        emojiRegex.containsMatchIn(newPassword) -> "No se permiten emojis"
                        !passwordRegex.matches(newPassword) -> "No se permiten espacios"
                        newPassword.length < 6 -> "Mínimo 6 caracteres"
                        else -> null
                    }
                    confirmPasswordError = when {
                        confirmPassword.isBlank() -> "Campo requerido"
                        emojiRegex.containsMatchIn(confirmPassword) -> "No se permiten emojis"
                        !passwordRegex.matches(confirmPassword) -> "No se permiten espacios"
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
}
