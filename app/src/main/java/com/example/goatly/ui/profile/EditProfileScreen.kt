package com.example.goatly.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goatly.ui.auth.GoatlyTextField
import com.example.goatly.ui.theme.AppColors

private val VALID_DEPARTMENTS = listOf(
    "Ingeniería",
    "Ciencias Sociales",
    "Ciencias Básicas",
    "Administrativo",
    "Artes",
    "Deporte"
)

private val LANGUAGE_OPTIONS = listOf("es" to "Español", "en" to "English")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Leer el snapshot actual del usuario para inicializar los campos inmediatamente.
    // Esto evita el problema de LaunchedEffect que llega un frame tarde.
    val initialUser = remember { profileViewModel.user.value }

    var name by remember { mutableStateOf(initialUser?.name ?: "") }
    var department by remember {
        mutableStateOf(
            initialUser?.department
                ?.takeIf { VALID_DEPARTMENTS.contains(it) }
                ?: VALID_DEPARTMENTS.first()
        )
    }
    var language by remember { mutableStateOf(initialUser?.language ?: "es") }
    var deptExpanded by remember { mutableStateOf(false) }
    var langExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

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
                title = { Text("Editar perfil", fontWeight = FontWeight.W800) },
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

                    // Nombre
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nombre completo", fontSize = 16.sp, fontWeight = FontWeight.W700)
                        GoatlyTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = if (it.isBlank()) "Campo requerido" else null
                            },
                            placeholder = "Tu nombre completo"
                        )
                        nameError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
                    }

                    // Departamento
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Departamento", fontSize = 16.sp, fontWeight = FontWeight.W700)
                        ExposedDropdownMenuBox(
                            expanded = deptExpanded,
                            onExpandedChange = { deptExpanded = !deptExpanded }
                        ) {
                            OutlinedTextField(
                                value = department,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        if (deptExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.PrimaryYellow,
                                    unfocusedBorderColor = AppColors.Border,
                                    focusedContainerColor = AppColors.Surface,
                                    unfocusedContainerColor = AppColors.Surface
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = deptExpanded,
                                onDismissRequest = { deptExpanded = false }
                            ) {
                                VALID_DEPARTMENTS.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { Text(dept) },
                                        onClick = {
                                            department = dept
                                            deptExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Idioma
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Idioma", fontSize = 16.sp, fontWeight = FontWeight.W700)
                        ExposedDropdownMenuBox(
                            expanded = langExpanded,
                            onExpandedChange = { langExpanded = !langExpanded }
                        ) {
                            OutlinedTextField(
                                value = LANGUAGE_OPTIONS.firstOrNull { it.first == language }?.second ?: language,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        if (langExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.PrimaryYellow,
                                    unfocusedBorderColor = AppColors.Border,
                                    focusedContainerColor = AppColors.Surface,
                                    unfocusedContainerColor = AppColors.Surface
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = langExpanded,
                                onDismissRequest = { langExpanded = false }
                            ) {
                                LANGUAGE_OPTIONS.forEach { (code, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            language = code
                                            langExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    nameError = if (name.isBlank()) "Campo requerido" else null
                    if (nameError == null) {
                        profileViewModel.updateProfile(name, department, language, onBack)
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
                    Text("Guardar cambios", fontSize = 18.sp, fontWeight = FontWeight.W800)
                }
            }
        }
    }
}
