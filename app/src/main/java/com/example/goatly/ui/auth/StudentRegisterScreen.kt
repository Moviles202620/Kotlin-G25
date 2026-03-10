package com.example.goatly.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goatly.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
            )
        },
        containerColor = AppColors.Background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Goatly", fontSize = 34.sp, fontWeight = FontWeight.W800, color = AppColors.DarkText)
                Spacer(Modifier.width(6.dp))
                Box(modifier = Modifier.size(10.dp).background(AppColors.PrimaryYellow, CircleShape))
            }

            Spacer(Modifier.height(22.dp))

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = AppColors.Surface, shadowElevation = 6.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Crear cuenta", fontSize = 30.sp, fontWeight = FontWeight.W800)
                    Spacer(Modifier.height(6.dp))
                    Text("Únete a Goatly como estudiante", fontSize = 16.sp, color = AppColors.GreyText)
                    Spacer(Modifier.height(22.dp))

                    Text("Nombre completo", fontSize = 16.sp, fontWeight = FontWeight.W700)
                    Spacer(Modifier.height(10.dp))
                    GoatlyTextField(value = name, onValueChange = { name = it }, placeholder = "Tu nombre completo")

                    Spacer(Modifier.height(16.dp))
                    Text("Correo institucional", fontSize = 16.sp, fontWeight = FontWeight.W700)
                    Spacer(Modifier.height(10.dp))
                    GoatlyTextField(value = email, onValueChange = { email = it }, placeholder = "nombre@uniandes.edu.co", keyboardType = KeyboardType.Email)

                    Spacer(Modifier.height(16.dp))
                    Text("Carrera", fontSize = 16.sp, fontWeight = FontWeight.W700)
                    Spacer(Modifier.height(10.dp))
                    GoatlyTextField(value = major, onValueChange = { major = it }, placeholder = "Ejem: Ingeniería de Sistemas")

                    Spacer(Modifier.height(16.dp))
                    Text("Contraseña", fontSize = 16.sp, fontWeight = FontWeight.W700)
                    Spacer(Modifier.height(10.dp))
                    GoatlyTextField(value = password, onValueChange = { password = it }, placeholder = "Mínimo 4 caracteres", isPassword = true)

                    Spacer(Modifier.height(22.dp))

                    Button(
                        onClick = {
                            val e = email.trim().lowercase()
                            if (name.isBlank() || major.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Completa todos los campos.") }
                            } else if (!e.endsWith("@uniandes.edu.co")) {
                                scope.launch { snackbarHostState.showSnackbar("Debes usar tu correo @uniandes.edu.co") }
                            } else if (password.length < 4) {
                                scope.launch { snackbarHostState.showSnackbar("La contraseña debe tener mínimo 4 caracteres.") }
                            } else {
                                onRegisterSuccess()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.DarkText, contentColor = AppColors.PrimaryYellow),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Crear cuenta", fontSize = 20.sp, fontWeight = FontWeight.W800)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tienes cuenta? ", color = AppColors.GreyText)
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                    Text("Inicia sesión", color = AppColors.PrimaryYellow, fontWeight = FontWeight.W700)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
