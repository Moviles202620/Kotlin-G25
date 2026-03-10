package com.example.goatly.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goatly.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun StudentLoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.Background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(30.dp))

                // Logo "Goatly •"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Goatly", fontSize = 34.sp, fontWeight = FontWeight.W800, color = AppColors.DarkText)
                    Spacer(Modifier.width(6.dp))
                    Box(modifier = Modifier.size(10.dp).background(AppColors.PrimaryYellow, CircleShape))
                }

                Spacer(Modifier.height(26.dp))

                // Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.Surface,
                    shadowElevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Iniciar sesión", fontSize = 30.sp, fontWeight = FontWeight.W800)
                        Spacer(Modifier.height(6.dp))
                        Text("Acceso para estudiantes", fontSize = 16.sp, color = AppColors.GreyText)
                        Spacer(Modifier.height(22.dp))

                        Text("Correo institucional", fontSize = 16.sp, fontWeight = FontWeight.W700)
                        Spacer(Modifier.height(10.dp))
                        GoatlyTextField(value = email, onValueChange = { email = it }, placeholder = "nombre@uniandes.edu.co", keyboardType = KeyboardType.Email)

                        Spacer(Modifier.height(18.dp))
                        Text("Contraseña", fontSize = 16.sp, fontWeight = FontWeight.W700)
                        Spacer(Modifier.height(10.dp))
                        GoatlyTextField(value = password, onValueChange = { password = it }, placeholder = "••••••••", isPassword = true)

                        Spacer(Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val ok = authViewModel.login(
                                    email = email.ifBlank { "nombre@uniandes.edu.co" },
                                    password = password.ifBlank { "1234" }
                                )
                                if (ok) onLoginSuccess()
                                else scope.launch { snackbarHostState.showSnackbar("Credenciales inválidas (debe ser @uniandes.edu.co)") }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.DarkText, contentColor = AppColors.PrimaryYellow),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("Ingresar", fontSize = 20.sp, fontWeight = FontWeight.W800)
                        }

                        Spacer(Modifier.height(18.dp))
                        HorizontalDivider(color = AppColors.Border)
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("¿Olvidaste tu contraseña?", fontSize = 18.sp, fontWeight = FontWeight.W700, color = AppColors.DarkText.copy(alpha = 0.85f))
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Ir a registro
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿No tienes cuenta? ", color = AppColors.GreyText)
                    TextButton(onClick = onGoToRegister, contentPadding = PaddingValues(0.dp)) {
                        Text("Regístrate", color = AppColors.PrimaryYellow, fontWeight = FontWeight.W700)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    listOf("Privacidad", "Términos", "Ayuda").forEach {
                        Text(it, color = Color(0xFF9AA4B2))
                    }
                }
            }
        }
    }
}

@Composable
fun GoatlyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color(0xFF9AA4B2)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.PrimaryYellow,
            unfocusedBorderColor = AppColors.Border,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp)
    )
}
