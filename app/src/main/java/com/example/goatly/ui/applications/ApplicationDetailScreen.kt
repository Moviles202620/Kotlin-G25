package com.example.goatly.ui.applications

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goatly.data.network.MyApplicationItemDto
import com.example.goatly.ui.theme.AppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationDetailScreen(
    appsViewModel: ApplicationsViewModel,
    onBack: () -> Unit
) {
    val app by appsViewModel.selectedApplication.collectAsState()

    if (app == null) {
        onBack()
        return
    }

    val item = app!!

    val (statusLabel, statusColor, statusBg) = when (item.status) {
        "accepted" -> Triple("ACEPTADA",  AppColors.Success,        AppColors.Success.copy(alpha = 0.08f))
        "rejected" -> Triple("RECHAZADA", AppColors.Danger,         AppColors.Danger.copy(alpha = 0.08f))
        else       -> Triple("PENDIENTE", Color(0xFF9AA4B2), Color(0xFF9AA4B2).copy(alpha = 0.08f))
    }

    val applicationDate = remember(item.createdAt) { formatIso(item.createdAt, "dd/MM/yyyy") }
    val offerDate       = remember(item.offer.dateTime) { formatIso(item.offer.dateTime, "dd/MM/yyyy HH:mm") }
    val copFormatted    = remember(item.offer.valueCop) {
        NumberFormat.getNumberInstance(Locale("es", "CO")).format(item.offer.valueCop) + " COP"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de aplicación", fontWeight = FontWeight.W800) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )
        },
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Estado de la aplicación
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = statusBg
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "ESTADO DE TU APLICACIÓN",
                        fontSize = 12.sp, letterSpacing = 1.4.sp,
                        color = statusColor, fontWeight = FontWeight.W800
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(statusLabel, fontSize = 28.sp, fontWeight = FontWeight.W900, color = statusColor)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Aplicado el $applicationDate",
                        fontSize = 14.sp, color = statusColor.copy(alpha = 0.7f)
                    )
                }
            }

            // Encabezado de la oferta
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp), color = AppColors.Surface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "OFERTA",
                        fontSize = 12.sp, letterSpacing = 1.4.sp,
                        color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(item.offer.title, fontSize = 24.sp, fontWeight = FontWeight.W900)
                    Spacer(Modifier.height(6.dp))
                    Text(copFormatted, fontSize = 20.sp, fontWeight = FontWeight.W800, color = AppColors.PrimaryYellow)
                }
            }

            // Detalles de la oferta
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp), color = AppColors.Surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "DETALLES",
                        fontSize = 12.sp, letterSpacing = 1.4.sp,
                        color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800
                    )
                    DetailRow(
                        icon = Icons.Default.CalendarMonth,
                        label = "Fecha y hora",
                        value = offerDate
                    )
                    HorizontalDivider(color = AppColors.Border)
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Duración",
                        value = "${item.offer.durationHours} horas"
                    )
                    HorizontalDivider(color = AppColors.Border)
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Modalidad",
                        value = if (item.offer.isOnSite) "Presencial" else "Remoto"
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppColors.GreyText, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 13.sp, color = AppColors.GreyText)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.W700)
        }
    }
}

private fun formatIso(iso: String, pattern: String): String = try {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    formatter.format(parser.parse(iso.substringBefore("Z").substringBefore("+"))!!)
} catch (e: Exception) {
    iso.take(10)
}
