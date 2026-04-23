package com.example.goatly.ui.home

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goatly.ui.theme.AppColors
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    offerId: String,
    userName: String?,
    userDepartment: String? = null,
    detailViewModel: OfferDetailViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by detailViewModel.state.collectAsState()
    var showApplyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val locationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) detailViewModel.load(offerId, context)
    }

    LaunchedEffect(offerId) {
        locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    DisposableEffect(offerId) {
        onDispose {
            detailViewModel.stopTracking(context)
        }
    }

    val lat = state.latitude ?: 4.6015
    val lng = state.longitude ?: -74.0657

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de oferta", fontWeight = FontWeight.W800) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )
        },
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header card
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp), color = AppColors.Surface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Box(
                        modifier = Modifier.background(AppColors.PrimaryYellow.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(state.category, fontSize = 12.sp, fontWeight = FontWeight.W700, color = Color(0xFF9A5B00))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(state.title, fontSize = 26.sp, fontWeight = FontWeight.W900)
                    Spacer(Modifier.height(8.dp))
                    Text(state.valueCop, fontSize = 22.sp, fontWeight = FontWeight.W800, color = AppColors.PrimaryYellow)
                }
            }

            // Info card
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp), color = AppColors.Surface
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("DETALLES", fontSize = 12.sp, letterSpacing = 1.4.sp, color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800)
                    InfoRow(icon = Icons.Default.CalendarMonth, label = "Fecha y hora", value = state.dateTime)
                    HorizontalDivider(color = AppColors.Border)
                    InfoRow(icon = Icons.Default.Schedule, label = "Duración", value = state.durationHours)
                    HorizontalDivider(color = AppColors.Border)
                    InfoRow(icon = Icons.Default.LocationOn, label = "Modalidad", value = state.location)
                }
            }

            // Mapa card
            if (state.isOnSite) {
                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp), color = AppColors.Surface
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("UBICACIÓN", fontSize = 12.sp, letterSpacing = 1.4.sp, color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800)
                        Spacer(Modifier.height(12.dp))
                        OsmMapView(
                            context = context,
                            latitude = lat,
                            longitude = lng,
                            title = state.title,
                            userLatitude = state.userLatitude,
                            userLongitude = state.userLongitude,
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        state.distanceText?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AppColors.PrimaryYellow, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(it, fontSize = 14.sp, color = AppColors.GreyText, fontWeight = FontWeight.W600)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Sprint 3: Feature Calendar Sync — BQ indicator
            // Visible independently of apply status
            // Answers BQ: "Which of the student's applied offers have been added to their calendar?"
            if (state.isAddedToCalendar) {
                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Success.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.Success.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AppColors.Success, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Agregado a tu calendario", fontSize = 14.sp, fontWeight = FontWeight.W600, color = AppColors.Success)
                    }
                }
                Spacer(Modifier.height(8.dp))
            } else if (state.isCalendarPending) {
                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.PrimaryYellow.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.PrimaryYellow.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AppColors.PrimaryYellow, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sincronización pendiente — se agregará cuando haya conexión", fontSize = 14.sp, fontWeight = FontWeight.W600, color = AppColors.PrimaryYellow)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            // Sprint 3: Feature Calendar Sync — END BQ indicator

            // Apply button / already applied
            if (state.hasApplied) {
                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Success.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp), color = AppColors.Success.copy(alpha = 0.08f)
                ) {
                    Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppColors.Success, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Ya aplicaste a esta oferta", fontSize = 17.sp, fontWeight = FontWeight.W800, color = AppColors.Success)
                    }
                }
            } else {
                Button(
                    onClick = { showApplyDialog = true },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aplicar a esta oferta", fontSize = 18.sp, fontWeight = FontWeight.W900)
                }
            }

            if (showApplyDialog) {
                ApplyApplicationDialog(
                    offerId = offerId,
                    userName = userName ?: "Estudiante",
                    userDepartment = userDepartment ?: "Ingeniería",
                    detailViewModel = detailViewModel,
                    context = context,
                    onDismiss = { showApplyDialog = false },
                    onSuccess = {
                        showApplyDialog = false
                        // Sprint 3: Feature Calendar Sync — stay on screen to show calendar banner
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

private val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27BF]")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyApplicationDialog(
    offerId: String,
    userName: String,
    userDepartment: String,
    detailViewModel: OfferDetailViewModel,
    context: Context,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val isLoading by detailViewModel.isLoading.collectAsState()

    var applicantName by remember { mutableStateOf(userName) }
    var career by remember { mutableStateOf(userDepartment) }
    var semester by remember { mutableStateOf("") }
    var gpa by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var motivationLetter by remember { mutableStateOf("") }
    var expandedAvailability by remember { mutableStateOf(false) }

    // Sprint 3: Feature Calendar Sync — user choice to add to calendar
    var addToCalendar by remember { mutableStateOf(true) }
    // Sprint 3: Feature Calendar Sync — END

    var nameError by remember { mutableStateOf<String?>(null) }
    var semesterError by remember { mutableStateOf<String?>(null) }
    var gpaError by remember { mutableStateOf<String?>(null) }
    var availabilityError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Información de la Aplicación", fontWeight = FontWeight.W800) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre del postulante
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nombre del postulante", fontSize = 13.sp, fontWeight = FontWeight.W700)
                    OutlinedTextField(
                        value = applicantName,
                        onValueChange = {
                            applicantName = it
                            nameError = when {
                                it.isBlank() -> "Campo requerido"
                                emojiRegex.containsMatchIn(it) -> "No se permiten emojis"
                                else -> null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    nameError?.let { Text(it, color = Color.Red, fontSize = 11.sp) }
                }

                // Carrera
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Carrera", fontSize = 13.sp, fontWeight = FontWeight.W700)
                    OutlinedTextField(
                        value = career,
                        onValueChange = { career = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                }

                // Semestre
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Semestre (1-12)", fontSize = 13.sp, fontWeight = FontWeight.W700)
                    OutlinedTextField(
                        value = semester,
                        onValueChange = {
                            semester = it
                            semesterError = when {
                                it.isBlank() -> "Campo requerido"
                                it.toIntOrNull() == null -> "Debe ser un número"
                                it.toInt() !in 1..12 -> "Debe estar entre 1 y 12"
                                else -> null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    semesterError?.let { Text(it, color = Color.Red, fontSize = 11.sp) }
                }

                // GPA
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Promedio (GPA) 0.0 - 5.0", fontSize = 13.sp, fontWeight = FontWeight.W700)
                    OutlinedTextField(
                        value = gpa,
                        onValueChange = {
                            gpa = it
                            gpaError = when {
                                it.isBlank() -> "Campo requerido"
                                it.toFloatOrNull() == null -> "Debe ser un número"
                                it.toFloat() !in 0f..5f -> "Debe estar entre 0.0 y 5.0"
                                else -> null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    gpaError?.let { Text(it, color = Color.Red, fontSize = 11.sp) }
                }

                // Disponibilidad
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Disponibilidad", fontSize = 13.sp, fontWeight = FontWeight.W700)
                    ExposedDropdownMenuBox(
                        expanded = expandedAvailability,
                        onExpandedChange = { expandedAvailability = !expandedAvailability }
                    ) {
                        OutlinedTextField(
                            value = availability,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                Icon(
                                    if (expandedAvailability) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAvailability,
                            onDismissRequest = { expandedAvailability = false }
                        ) {
                            listOf("flexible", "fixed", "part-time").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        availability = option
                                        availabilityError = null
                                        expandedAvailability = false
                                    }
                                )
                            }
                        }
                    }
                    availabilityError?.let { Text(it, color = Color.Red, fontSize = 11.sp) }
                }

                // Carta de motivación
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Carta de Motivación", fontSize = 13.sp, fontWeight = FontWeight.W700)
                    OutlinedTextField(
                        value = motivationLetter,
                        onValueChange = { motivationLetter = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("Cuéntanos por qué te interesa esta oferta...") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                }

                // Sprint 3: Feature Calendar Sync — calendar toggle
                // User decides whether to add this offer to their device calendar
                HorizontalDivider(color = AppColors.Border)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = if (addToCalendar) AppColors.PrimaryYellow else AppColors.GreyText,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text("Agregar al calendario", fontSize = 13.sp, fontWeight = FontWeight.W700)
                            Text("Guarda la fecha de esta oferta", fontSize = 11.sp, color = AppColors.GreyText)
                        }
                    }
                    Switch(
                        checked = addToCalendar,
                        onCheckedChange = { addToCalendar = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.DarkText,
                            checkedTrackColor = AppColors.PrimaryYellow
                        )
                    )
                }
                // Sprint 3: Feature Calendar Sync — END calendar toggle
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (availability.isBlank()) availabilityError = "Debe seleccionar una disponibilidad"
                    else availabilityError = null

                    if (validateForm(nameError, semesterError, gpaError) && availabilityError == null) {
                        // Sprint 3: Feature Calendar Sync — pass addToCalendar choice
                        detailViewModel.applyAndSyncCalendar(
                            context = context,
                            offerId = offerId,
                            applicantName = applicantName,
                            career = career,
                            semester = semester.toInt(),
                            gpa = gpa.toFloat(),
                            availability = availability,
                            motivationLetter = motivationLetter,
                            addToCalendar = addToCalendar,
                            onSuccess = onSuccess
                        )
                        // Sprint 3: Feature Calendar Sync — END
                    }
                },
                enabled = !isLoading && nameError == null && semesterError == null && gpaError == null && availability.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryYellow)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                } else {
                    Text("Aplicar", color = Color.Black, fontWeight = FontWeight.W800)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun validateForm(nameError: String?, semesterError: String?, gpaError: String?): Boolean {
    return nameError == null && semesterError == null && gpaError == null
}

fun createColoredMarkerBitmap(color: Int): Bitmap {
    val size = 60
    val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        this.color = color
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    return bitmap
}

@Composable
fun OsmMapView(
    context: Context,
    latitude: Double,
    longitude: Double,
    title: String,
    userLatitude: Double?,
    userLongitude: Double?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(latitude, longitude))

                val offerMarker = Marker(this)
                offerMarker.position = GeoPoint(latitude, longitude)
                offerMarker.title = title
                offerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                overlays.add(offerMarker)
            }
        },
        update = { mapView ->
            if (userLatitude != null && userLongitude != null) {
                mapView.overlays.removeAll { it is Marker && it.title == "Tu ubicación" }

                val userMarker = Marker(mapView)
                userMarker.position = GeoPoint(userLatitude, userLongitude)
                userMarker.title = "Tu ubicación"
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                userMarker.icon = createColoredMarkerBitmap(android.graphics.Color.BLUE).toDrawable(context.resources)
                mapView.overlays.add(userMarker)
                mapView.invalidate()
            }
        },
        modifier = modifier
    )
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppColors.GreyText, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 13.sp, color = AppColors.GreyText)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.W700)
        }
    }
}