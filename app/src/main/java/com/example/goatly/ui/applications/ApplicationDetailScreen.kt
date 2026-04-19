package com.example.goatly.ui.applications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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

            // Tu perfil (student profile section)
            if (!item.applicantName.isNullOrBlank()) {
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
                            "TU PERFIL",
                            fontSize = 12.sp, letterSpacing = 1.4.sp,
                            color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800
                        )
                        if (!item.applicantName.isNullOrBlank()) {
                            ProfileField("Nombre", item.applicantName!!)
                            HorizontalDivider(color = AppColors.Border)
                        }
                        if (!item.career.isNullOrBlank()) {
                            ProfileField("Carrera", item.career!!)
                            HorizontalDivider(color = AppColors.Border)
                        }
                        if (item.semester != null) {
                            ProfileField("Semestre", item.semester.toString())
                            HorizontalDivider(color = AppColors.Border)
                        }
                        if (item.gpa != null) {
                            ProfileField("Promedio (GPA)", String.format("%.2f", item.gpa))
                            HorizontalDivider(color = AppColors.Border)
                        }
                        if (!item.availability.isNullOrBlank()) {
                            ProfileField("Disponibilidad", item.availability!!)
                            HorizontalDivider(color = AppColors.Border)
                        }
                        if (!item.motivationLetter.isNullOrBlank()) {
                            Text("Carta de motivación", fontSize = 13.sp, color = AppColors.GreyText)
                            Text(
                                item.motivationLetter!!,
                                fontSize = 15.sp,
                                color = AppColors.DarkText,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Performance indicators (if completed)
            if (item.isCompleted && item.rating != null) {
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
                            "INDICADORES DE DESEMPEÑO",
                            fontSize = 12.sp, letterSpacing = 1.4.sp,
                            color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800
                        )
                        PerformanceBadge(item.rating!!)
                        HorizontalDivider(color = AppColors.Border)
                        Text("Desglose de evaluaciones", fontSize = 13.sp, color = AppColors.GreyText, fontWeight = FontWeight.W700)
                        Spacer(Modifier.height(8.dp))
                        if (item.ratingPunctuality != null) {
                            RatingBar("Puntualidad", item.ratingPunctuality!!)
                        }
                        if (item.ratingQuality != null) {
                            RatingBar("Calidad", item.ratingQuality!!)
                        }
                        if (item.ratingAttitude != null) {
                            RatingBar("Actitud", item.ratingAttitude!!)
                        }
                    }
                }
            }

            // Feedback from staff (if completed)
            if (item.isCompleted && !item.ratingFeedback.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp), color = AppColors.Surface
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "FEEDBACK DEL STAFF",
                            fontSize = 12.sp, letterSpacing = 1.4.sp,
                            color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800
                        )
                        Text(
                            item.ratingFeedback!!,
                            fontSize = 15.sp,
                            color = AppColors.DarkText,
                            lineHeight = 20.sp
                        )
                    }
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

@Composable
private fun ProfileField(label: String, value: String) {
    Column {
        Text(label, fontSize = 13.sp, color = AppColors.GreyText)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.W700, color = AppColors.DarkText)
    }
}

@Composable
private fun PerformanceBadge(rating: Float) {
    val (badgeColor, badgeIcon, badgeText) = when {
        rating >= 4.5f -> Triple(AppColors.Success, "⭐", "Excelente")
        rating >= 3.5f -> Triple(AppColors.PrimaryYellow, "✓", "Muy Bueno")
        rating >= 2.5f -> Triple(Color(0xFFFFB800), "→", "Bueno")
        else -> Triple(AppColors.Danger, "!", "Necesita mejora")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = badgeColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(badgeColor.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(badgeIcon, fontSize = 28.sp)
            }
            Column {
                Text("Calificación General", fontSize = 12.sp, color = AppColors.GreyText)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(String.format("%.1f", rating), fontSize = 24.sp, fontWeight = FontWeight.W900, color = badgeColor)
                    Text(badgeText, fontSize = 14.sp, fontWeight = FontWeight.W800, color = badgeColor)
                }
            }
        }
    }
}

@Composable
private fun RatingBar(label: String, value: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 13.sp, color = AppColors.GreyText)
            Text(String.format("%.1f/5.0", value), fontSize = 13.sp, fontWeight = FontWeight.W700, color = AppColors.DarkText)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(AppColors.Border, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (value / 5f).coerceIn(0f, 1f))
                    .background(AppColors.Success, RoundedCornerShape(3.dp))
            )
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
