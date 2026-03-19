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
    detailViewModel: OfferDetailViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by detailViewModel.state.collectAsState()
    val context = LocalContext.current

    val locationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) detailViewModel.load(offerId, context)
    }

    LaunchedEffect(offerId) {
        locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
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

            // Botón aplicar
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
                    onClick = { detailViewModel.apply(offerId, userName ?: "Estudiante Uniandes") },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aplicar a esta oferta", fontSize = 18.sp, fontWeight = FontWeight.W900)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
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

                // Marcador de la oferta (verde por defecto)
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