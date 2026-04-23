package com.example.goatly.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var majorError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()

    val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27BF]")
    val nameRegex = Regex("^[A-Za-zÁÉÍÓÚÜáéíóúüÑñ ]+$")

    fun validateEmail(value: String): String? {
        val e = value.trim().lowercase()
        return when {
            value.isBlank() -> null
            emojiRegex.containsMatchIn(value) -> "El correo no puede contener emojis"
            !e.contains("@") -> "Ingresa un correo válido"
            e.startsWith("@") -> "Correo inválido, no cumple con el formato"
            !e.endsWith("@uniandes.edu.co") -> "Debe ser un correo @uniandes.edu.co"
            else -> null
        }
    }

    // Sprint 3: Feature Password Strength Indicator — START
    // Real-time password rules checklist. Each rule is a label + a lambda that checks the password.
    // Displayed as a green/red checklist below the password field.
    val passwordRules = listOf(
        "Al menos 8 caracteres"                    to { p: String -> p.length >= 8 },
        "Una letra mayúscula"                      to { p: String -> p.any { it.isUpperCase() } },
        "Una letra minúscula"                      to { p: String -> p.any { it.isLowerCase() } },
        "Un número"                                to { p: String -> p.any { it.isDigit() } },
        "Un carácter especial (!@#\$%^&*)"         to { p: String ->
            p.any { "!@#\$%^&*()-_=+[]{}|;:',.<>?/`~".contains(it) }
        },
        "Sin espacios"                             to { p: String -> !p.contains(' ') },
        "Sin emojis"                               to { p: String -> !emojiRegex.containsMatchIn(p) }
    )
    // passwordStrong is true only when ALL rules pass — used to enable the register button
    val passwordStrong = passwordRules.all { (_, check) -> check(password) }
    // Sprint 3: Feature Password Strength Indicator — END

    val careers = listOf(
        "No Aplica",
        "Administración",
        "Economía",
        "Gobierno y Asuntos Públicos",
        "Biología",
        "Física",
        "Geociencias",
        "Matemáticas",
        "Microbiología",
        "Química",
        "Medicina",
        "Arquitectura",
        "Diseño",
        "Arte",
        "Historia del Arte",
        "Literatura",
        "Música",
        "Narrativas Digitales",
        "Ingeniería Ambiental",
        "Ingeniería de Alimentos",
        "Ingeniería Biomédica",
        "Ingeniería Civil",
        "Ingeniería Eléctrica",
        "Ingeniería Electrónica",
        "Ingeniería Industrial",
        "Ingeniería Mecánica",
        "Ingeniería Química",
        "Ingeniería de Sistemas y Computación",
        "Derecho",
        "Antropología",
        "Ciencia Política",
        "Estudios Globales",
        "Filosofía",
        "Historia",
        "Lenguas y Cultura",
        "Psicología",
        "Licenciatura en Artes",
        "Licenciatura en Biología",
        "Licenciatura en Educación Infantil",
        "Licenciatura en Español y Filología",
        "Licenciatura en Filosofía",
        "Licenciatura en Física",
        "Licenciatura en Historia",
        "Licenciatura en Matemáticas",
        "Licenciatura en Química",
    )

    var majorExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Goatly", fontSize = 34.sp, fontWeight = FontWeight.W800, color = AppColors.DarkText)
                Spacer(Modifier.width(6.dp))
                Box(modifier = Modifier.size(10.dp).background(AppColors.PrimaryYellow, CircleShape))
            }

            Spacer(Modifier.height(22.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = AppColors.Surface,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Crear cuenta", fontSize = 30.sp, fontWeight = FontWeight.W800)
                    Spacer(Modifier.height(6.dp))
                    Text("Únete a Goatly", fontSize = 16.sp, color = AppColors.GreyText)
                    Spacer(Modifier.height(22.dp))

                    // ── Tipo de cuenta ──────────────────────────────────────
                    Text("Tipo de cuenta", fontSize = 16.sp, fontWeight = FontWeight.W700)
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("student" to "Estudiante", "staff" to "Funcionario").forEach { (value, label) ->
                            val selected = selectedRole == value
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = value }
                                    .border(
                                        width = 2.dp,
                                        color = if (selected) AppColors.DarkText else Color.LightGray,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) AppColors.DarkText else AppColors.Surface
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
                                    Text(
                                        label,
                                        fontWeight = FontWeight.W700,
                                        color = if (selected) AppColors.PrimaryYellow else AppColors.GreyText
                                    )
                                }
                            }
                        }
                    }
                    if (selectedRole == "staff") {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ten en cuenta que si deseas usar la app como funcionario, debes usar la aplicación correspondiente.",
                            fontSize = 12.sp,
                            color = AppColors.GreyText
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Nombre ──────────────────────────────────────────────
                    Text("Nombre completo", fontSize = 16.sp, fontWeight = FontWeight.W700)
                    Spacer(Modifier.height(10.dp))
                    GoatlyTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = when {
                                it.isBlank() -> null
                                emojiRegex.containsMatchIn(it) -> "El nombre no puede contener emojis"
                                !nameRegex.matches(it) -> "Solo letras y espacios"
                                else -> null
                            }
                        },
                        placeholder = "Tu nombre completo"
                    )
                    nameError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

                    Spacer(Modifier.height(16.dp))

                    // ── Correo ──────────────────────────────────────────────
                    Text("Correo institucional", fontSize = 16.sp, fontWeight = FontWeight.W700)
                    Spacer(Modifier.height(10.dp))
                    GoatlyTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = validateEmail(it)
                        },
                        placeholder = "nombre@uniandes.edu.co",
                        keyboardType = KeyboardType.Email
                    )
                    emailError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

                    Spacer(Modifier.height(16.dp))

                    // ── Carrera ─────────────────────────────────────────────
                    Text("Carrera", fontSize = 16.sp, fontWeight = FontWeight.W700)
                    Spacer(Modifier.height(10.dp))
                    ExposedDropdownMenuBox(
                        expanded = majorExpanded,
                        onExpandedChange = { majorExpanded = !majorExpanded },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        OutlinedTextField(
                            value = major,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Seleccionar", color = AppColors.GreyText) },
                            trailingIcon = {
                                Icon(
                                    if (majorExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.DarkText,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = AppColors.Surface,
                                unfocusedContainerColor = AppColors.Surface,
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = majorExpanded,
                            onDismissRequest = { majorExpanded = false }
                        ) {
                            careers.forEach { career ->
                                DropdownMenuItem(
                                    text = { Text(career, fontSize = 14.sp) },
                                    onClick = {
                                        major = career
                                        majorError = null
                                        majorExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    majorError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

                    Spacer(Modifier.height(16.dp))

                    // ── Contraseña ──────────────────────────────────────────
                    // Sprint 3: Feature Password Strength Indicator — UI
                    Text("Contraseña", fontSize = 16.sp, fontWeight = FontWeight.W700)
                    Spacer(Modifier.height(10.dp))
                    GoatlyTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Crea una contraseña segura",
                        isPassword = true
                    )

                    // Sprint 3: Feature Password Strength Indicator — Checklist
                    // Shows only when the user starts typing. Each rule turns green when met.
                    if (password.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.Background
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    "Tu contraseña debe tener:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.W600,
                                    color = AppColors.GreyText
                                )
                                Spacer(Modifier.height(2.dp))
                                passwordRules.forEach { (label, check) ->
                                    val passed = check(password)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (passed) "✓" else "✗",
                                            color = if (passed) AppColors.Success else AppColors.Danger,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.W700
                                        )
                                        Text(
                                            text = label,
                                            color = if (passed) AppColors.Success else AppColors.Danger,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Sprint 3: Feature Password Strength Indicator — END UI

                    Spacer(Modifier.height(22.dp))

                    // Sprint 3: Feature Password Strength Indicator — Button validation
                    // Register button only fires when ALL password rules pass (passwordStrong = true)
                    Button(
                        onClick = {
                            nameError = when {
                                name.isBlank() -> "Campo requerido"
                                emojiRegex.containsMatchIn(name) -> "El nombre no puede contener emojis"
                                !nameRegex.matches(name) -> "Solo letras y espacios"
                                else -> null
                            }
                            emailError = if (email.isBlank()) "Campo requerido" else validateEmail(email)
                            majorError = if (major.isBlank()) "Selecciona una carrera" else null

                            if (nameError == null && emailError == null && majorError == null && passwordStrong) {
                                viewModel.register(name, email, password, major, selectedRole, onRegisterSuccess)
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
                            Text("Crear cuenta", fontSize = 20.sp, fontWeight = FontWeight.W800)
                        }
                    }
                    // Sprint 3: Feature Password Strength Indicator — END Button validation
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