package com.example.goatly.ui.applications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goatly.data.network.ApplicationStatsDto
import com.example.goatly.data.network.MyApplicationItemDto
import com.example.goatly.ui.theme.AppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    appsViewModel: ApplicationsViewModel = viewModel(),
    onSessionExpired: () -> Unit = {},
    onNavigateToDetail: (app: MyApplicationItemDto) -> Unit = {}
) {
    val uiState by appsViewModel.uiState.collectAsState()
    val activeFilter by appsViewModel.activeFilter.collectAsState()
    val isOffline by appsViewModel.isOffline.collectAsState()
    val searchQuery by appsViewModel.searchQuery.collectAsState()
    val filteredApplications by appsViewModel.filteredApplications.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val topOffers by appsViewModel.topOffers.collectAsState()

    // Carga inicial — load() ya incluye top offers en paralelo
    LaunchedEffect(Unit) {
        appsViewModel.load()
    }

    // One-time navigation event for 401
    LaunchedEffect(Unit) {
        appsViewModel.navigateToLogin.collect { onSessionExpired() }
    }

    // Show error in snackbar
    LaunchedEffect(uiState) {
        if (uiState is ApplicationsViewModel.UiState.Error) {
            snackbarHostState.showSnackbar((uiState as ApplicationsViewModel.UiState.Error).message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis aplicaciones", fontWeight = FontWeight.W800) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        when (val state = uiState) {
            is ApplicationsViewModel.UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.PrimaryYellow)
                }
            }

            is ApplicationsViewModel.UiState.Success,
            is ApplicationsViewModel.UiState.SuccessOffline -> {
                val apps = if (searchQuery.isEmpty()) {
                    when (val s = uiState) {
                        is ApplicationsViewModel.UiState.Success -> s.response.applications
                        is ApplicationsViewModel.UiState.SuccessOffline -> s.applications
                        else -> emptyList()
                    }
                } else filteredApplications

                val stats = when (val s = uiState) {
                    is ApplicationsViewModel.UiState.Success -> s.response.stats
                    is ApplicationsViewModel.UiState.SuccessOffline -> s.stats
                    else -> null
                }

                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Banner offline
                    if (isOffline) {
                        item { OfflineBanner() }
                    }

                    // Stats banner
                    if (stats != null) {
                        item { StatsCard(stats = stats) }
                    }

                    // BQ top offers
                    if (topOffers.isNotEmpty()) {
                        item { TopOffersCard(topOffers = topOffers) }
                    }

                    // Search field + filter row
                    item {
                        SearchField(
                            query = searchQuery,
                            onQueryChange = { appsViewModel.onSearchQueryChanged(it) }
                        )
                    }
                    item {
                        FilterRow(activeFilter = activeFilter, onFilter = { appsViewModel.load(it) })
                    }

                    if (apps.isEmpty()) {
                        item { EmptyApplications() }
                    } else {
                        item {
                            Text("Historial", fontSize = 22.sp, fontWeight = FontWeight.W800)
                        }
                        items(apps) { app ->
                            ApplicationCard(app = app, onClick = { onNavigateToDetail(app) })
                        }
                    }
                }
            }

            is ApplicationsViewModel.UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    EmptyApplications()
                }
            }
        }
    }
}

@Composable
private fun StatsCard(stats: ApplicationStatsDto) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp), color = AppColors.Surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("RESUMEN", fontSize = 12.sp, letterSpacing = 1.4.sp, color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryMetric("${stats.pending}", "Pendientes", Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).height(50.dp).background(AppColors.Border))
                SummaryMetric("${stats.accepted}", "Aceptadas", Modifier.weight(1f), valueColor = AppColors.Success)
                Box(modifier = Modifier.width(1.dp).height(50.dp).background(AppColors.Border))
                SummaryMetric("${stats.total}", "Total", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryMetric(value: String, label: String, modifier: Modifier = Modifier, valueColor: Color = AppColors.DarkText) {
    Column(modifier = modifier.padding(horizontal = 10.dp)) {
        Text(value, fontSize = 34.sp, fontWeight = FontWeight.W900, color = valueColor)
        Spacer(Modifier.height(4.dp))
        Text(label, color = AppColors.GreyText, fontSize = 14.sp)
    }
}

@Composable
private fun FilterRow(activeFilter: String?, onFilter: (String?) -> Unit) {
    val filters = listOf(
        null to "Todas",
        "pending" to "Pendientes",
        "accepted" to "Aceptadas",
        "rejected" to "Rechazadas"
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(filters) { (value, label) ->
            val selected = activeFilter == value
            FilterChip(
                selected = selected,
                onClick = { onFilter(value) },
                label = { Text(label, fontWeight = if (selected) FontWeight.W800 else FontWeight.W500, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.PrimaryYellow,
                    selectedLabelColor = AppColors.DarkText,
                    containerColor = AppColors.Surface,
                    labelColor = AppColors.GreyText
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = AppColors.Border,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun ApplicationCard(app: MyApplicationItemDto, onClick: () -> Unit) {
    val (statusLabel, statusColor) = when (app.status) {
        "accepted" -> "ACEPTADA" to AppColors.Success
        "rejected" -> "RECHAZADA" to AppColors.Danger
        else       -> "PENDIENTE" to Color(0xFF9AA4B2)
    }

    val dateLabel = remember(app.createdAt) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            formatter.format(parser.parse(app.createdAt.substringBefore("Z").substringBefore("+"))!!)
        } catch (e: Exception) {
            app.createdAt.take(10)
        }
    }

    val copFormatted = remember(app.offer.valueCop) {
        NumberFormat.getNumberInstance(Locale("es", "CO")).format(app.offer.valueCop) + " COP"
    }

    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)).clickable { onClick() },
        shape = RoundedCornerShape(14.dp), color = AppColors.Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(AppColors.PrimaryYellow.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, AppColors.PrimaryYellow.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", fontWeight = FontWeight.W900, color = Color(0xFF9A5B00))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.offer.title, fontSize = 17.sp, fontWeight = FontWeight.W900)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "$copFormatted · ${app.offer.durationHours}h",
                        color = AppColors.GreyText, fontSize = 13.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("Aplicado el $dateLabel", color = AppColors.GreyText, fontSize = 13.sp)
                }
                Text(statusLabel, fontWeight = FontWeight.W900, color = statusColor, fontSize = 12.sp)
            }

            // Performance badges (if completed and has rating)
            if (app.isCompleted && app.rating != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = AppColors.Border)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PerformanceBadgeSmall(rating = app.rating!!)
                    if (app.gpa != null && app.gpa!! >= 4.0f) {
                        BadgeChip("Alto GPA", AppColors.Success)
                    }
                    if (app.semester != null && app.semester!! >= 8) {
                        BadgeChip("Avanzado", AppColors.PrimaryYellow)
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceBadgeSmall(rating: Float) {
    val badgeColor = when {
        rating >= 4.5f -> AppColors.Success
        rating >= 3.5f -> AppColors.PrimaryYellow
        rating >= 2.5f -> Color(0xFFFFB800)
        else -> AppColors.Danger
    }

    Surface(
        modifier = Modifier
            .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .border(0.5.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Text(
            "⭐ ${String.format("%.1f", rating)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.W800,
            color = badgeColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BadgeChip(text: String, color: Color) {
    Surface(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Text(
            text,
            fontSize = 11.sp,
            fontWeight = FontWeight.W700,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyApplications(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(1.dp, AppColors.Border, CircleShape)
                .background(AppColors.Surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(46.dp), tint = Color(0xFFB6BFCC))
        }
        Spacer(Modifier.height(22.dp))
        Text("Sin aplicaciones", fontSize = 28.sp, fontWeight = FontWeight.W900)
        Spacer(Modifier.height(10.dp))
        Text(
            "No hay aplicaciones para este filtro.",
            fontSize = 18.sp, color = AppColors.GreyText, lineHeight = 24.sp
        )
    }
}

@Composable
private fun OfflineBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF5F0E8)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFF9A7B3A), modifier = Modifier.size(18.dp))
            Text(
                "Sin conexión — mostrando datos guardados",
                fontSize = 13.sp,
                color = Color(0xFF9A7B3A),
                fontWeight = FontWeight.W600
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar por oferta o carrera…", color = AppColors.GreyText) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.GreyText) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.PrimaryYellow,
            unfocusedBorderColor = AppColors.Border,
            focusedContainerColor = AppColors.Surface,
            unfocusedContainerColor = AppColors.Surface
        )
    )
}

@Composable
private fun TopOffersCard(topOffers: List<ApplicationsViewModel.TopOfferItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp), color = AppColors.Surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "OFERTAS MÁS SOLICITADAS",
                fontSize = 12.sp,
                letterSpacing = 1.4.sp,
                color = Color(0xFF9AA4B2),
                fontWeight = FontWeight.W800
            )
            Spacer(Modifier.height(14.dp))
            topOffers.forEachIndexed { index, offer ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(AppColors.PrimaryYellow.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W900,
                            color = Color(0xFF9A5B00)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        offer.title,
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W600
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.PrimaryYellow.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "${offer.total} apps",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W800,
                            color = Color(0xFF9A5B00)
                        )
                    }
                }
                if (index < topOffers.size - 1) {
                    Divider(color = AppColors.Border, thickness = 0.5.dp)
                }
            }
        }
    }
}
