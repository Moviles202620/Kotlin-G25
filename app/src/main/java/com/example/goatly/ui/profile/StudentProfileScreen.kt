package com.example.goatly.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.goatly.ui.applications.ApplicationsViewModel
import com.example.goatly.ui.applications.ApplicationsViewModel.UiState
import com.example.goatly.ui.theme.AppColors

@Composable
fun StudentProfileScreen(
    profileViewModel: ProfileViewModel,
    appsViewModel: ApplicationsViewModel = viewModel(),
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        appsViewModel.refresh()
    }

    val appsUiState by appsViewModel.uiState.collectAsState()
    val user by profileViewModel.user.collectAsState()
    val isOffline by profileViewModel.isOffline.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val carnetUploadSuccess by profileViewModel.carnetUploadSuccess.collectAsState()
    val carnetDeleteSuccess by profileViewModel.carnetDeleteSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val error by profileViewModel.error.collectAsState()

    // Sprint 4: Caching — Coil — image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { profileViewModel.uploadCarnet(context, it) }
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            profileViewModel.clearError()
        }
    }

    LaunchedEffect(carnetUploadSuccess) {
        if (carnetUploadSuccess) {
            snackbarHostState.showSnackbar("Carnet subido correctamente")
            profileViewModel.clearCarnetSuccess()
        }
    }

    LaunchedEffect(carnetDeleteSuccess) {
        if (carnetDeleteSuccess) {
            snackbarHostState.showSnackbar("Carnet eliminado")
            profileViewModel.clearCarnetDeleteSuccess()
        }
    }

    val userName = user?.name?.takeIf { it.isNotBlank() }
    val userEmail = user?.email?.takeIf { it.isNotBlank() }
    val userMajor = user?.department?.takeIf { it.isNotBlank() }
    val userLanguage = user?.language
    val initials = userName?.split(" ")?.take(2)?.joinToString("") { it.first().uppercase() } ?: "EU"
    val carnetUrl = user?.profilePicture

    val stats = when (val s = appsUiState) {
        is UiState.Success -> s.response.stats
        is UiState.SuccessOffline -> s.stats
        else -> null
    }
    val accepted = stats?.accepted ?: 0
    val pending = stats?.pending ?: 0
    val total = stats?.total ?: 0

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.Surface
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (isOffline) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F0E8))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFF9A7B3A), modifier = Modifier.size(16.dp))
                        Text("Sin conexión — datos guardados", fontSize = 13.sp, color = Color(0xFF9A7B3A), fontWeight = FontWeight.W600)
                    }
                }
            }
            item { Spacer(Modifier.height(18.dp)) }

            // ============================================================
            // Sprint 4: Caching — Coil image cache — Avatar / Carnet
            // ============================================================
            item {
                var showMenu by remember { mutableStateOf(false) }
                var showFullImage by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(AppColors.PrimaryYellow)
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (carnetUrl != null) {
                            // Coil loads and caches the image automatically in memory and disk
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(carnetUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Carnet universitario",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(initials, fontSize = 26.sp, fontWeight = FontWeight.W900, color = Color.White)
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                                .background(AppColors.DarkText, CircleShape)
                                .border(2.dp, AppColors.Surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Opciones de foto",
                                tint = AppColors.PrimaryYellow,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(90.dp),
                            color = AppColors.PrimaryYellow,
                            strokeWidth = 3.dp
                        )
                    }

                    // Dropdown menu con opciones
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (carnetUrl != null) {
                            DropdownMenuItem(
                                text = { Text("Ver imagen") },
                                onClick = {
                                    showMenu = false
                                    showFullImage = true
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(if (carnetUrl != null) "Cambiar imagen" else "Subir carnet") },
                            onClick = {
                                showMenu = false
                                imagePicker.launch("image/*")
                            }
                        )
                        if (carnetUrl != null) {
                            DropdownMenuItem(
                                text = { Text("Eliminar imagen", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    profileViewModel.deleteCarnet()
                                }
                            )
                        }
                    }
                }

                // Full screen image dialog
                if (showFullImage && carnetUrl != null) {
                    AlertDialog(
                        onDismissRequest = { showFullImage = false },
                        confirmButton = {
                            TextButton(onClick = { showFullImage = false }) {
                                Text("Cerrar")
                            }
                        },
                        text = {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(carnetUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Carnet completo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    if (carnetUrl != null) "Toca para ver opciones" else "Toca para subir tu carnet",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 12.sp,
                    color = AppColors.GreyText,
                    textAlign = TextAlign.Center
                )
            }
            // Sprint 4: Caching — END Avatar/Carnet

            item { Spacer(Modifier.height(12.dp)) }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(userName ?: "Estudiante Uniandes", fontSize = 22.sp, fontWeight = FontWeight.W900)
                    Spacer(Modifier.height(4.dp))
                    Text(userMajor ?: "Carrera", color = AppColors.GreyText, fontSize = 15.sp)
                    Spacer(Modifier.height(2.dp))
                    Text("Universidad de los Andes", color = AppColors.GreyText, fontSize = 15.sp)
                }
            }
            item { Spacer(Modifier.height(14.dp)) }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileSoftButton("Editar perfil", filled = true, modifier = Modifier.weight(1f)) { onEditProfile() }
                    ProfileSoftButton("Configuración", filled = false, modifier = Modifier.weight(1f)) { onSettings() }
                }
            }

            item { Spacer(Modifier.height(14.dp)) }
            item { HorizontalDivider(color = AppColors.Border) }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileStatCard("ACEPTADAS", "$accepted", AppColors.PrimaryYellow, Modifier.weight(1f))
                    ProfileStatCard("PENDIENTES", "$pending", AppColors.DarkText, Modifier.weight(1f))
                    ProfileStatCard("TOTAL", "$total", AppColors.DarkText, Modifier.weight(1f))
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp), color = AppColors.Surface
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("INFORMACIÓN", fontSize = 12.sp, letterSpacing = 1.4.sp, color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800)
                        HorizontalDivider(color = AppColors.Border)
                        ProfileInfoRow("Correo", userEmail ?: "—")
                        HorizontalDivider(color = AppColors.Border)
                        ProfileInfoRow("Departamento", userMajor ?: "—")
                        HorizontalDivider(color = AppColors.Border)
                        ProfileInfoRow("Universidad", "Universidad de los Andes")
                        HorizontalDivider(color = AppColors.Border)
                        ProfileInfoRow("Idioma", if (userLanguage == "en") "English" else "Español")
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = AppColors.GreyText, modifier = Modifier.size(18.dp))
                    }
                    Text("Cerrar sesión", color = AppColors.GreyText, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ProfileSoftButton(text: String, filled: Boolean, modifier: Modifier = Modifier, onTap: () -> Unit) {
    OutlinedButton(
        onClick = onTap, modifier = modifier.height(44.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (filled) AppColors.PrimaryYellow.copy(alpha = 0.2f) else Color.White,
            contentColor = if (filled) AppColors.PrimaryYellow else AppColors.GreyText
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (filled) Color.Transparent else AppColors.Border),
        shape = RoundedCornerShape(12.dp)
    ) { Text(text, fontWeight = FontWeight.W800) }
}

@Composable
private fun ProfileStatCard(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.border(1.dp, AppColors.Border, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), color = AppColors.Surface) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 12.sp, color = AppColors.GreyText, fontWeight = FontWeight.W800)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.W900, color = valueColor)
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = AppColors.GreyText, fontSize = 15.sp)
        Text(value, fontWeight = FontWeight.W700, fontSize = 15.sp)
    }
}