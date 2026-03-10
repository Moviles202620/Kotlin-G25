package com.example.goatly.ui.applications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goatly.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    appsViewModel: ApplicationsViewModel = viewModel()
) {
    val items by appsViewModel.items.collectAsState()

    // Refresh every time this screen is shown
    LaunchedEffect(Unit) { appsViewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis aplicaciones", fontWeight = FontWeight.W800) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )
        },
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        if (items.isEmpty()) {
            EmptyApplications(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Summary card
                item {
                    SummaryCard(
                        pending = items.count { it.statusType == ApplicationsViewModel.StatusType.PENDING },
                        accepted = items.count { it.statusType == ApplicationsViewModel.StatusType.ACCEPTED },
                        rejected = items.count { it.statusType == ApplicationsViewModel.StatusType.REJECTED }
                    )
                }

                item {
                    Text("Historial", fontSize = 22.sp, fontWeight = FontWeight.W800)
                }

                items(items) { app ->
                    ApplicationCard(app = app)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(pending: Int, accepted: Int, rejected: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp), color = AppColors.Surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("RESUMEN", fontSize = 12.sp, letterSpacing = 1.4.sp, color = Color(0xFF9AA4B2), fontWeight = FontWeight.W800)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryMetric("$pending", "Pendientes", Modifier.weight(1f))
                Box(modifier = Modifier.width(1.dp).height(50.dp).background(AppColors.Border))
                SummaryMetric("$accepted", "Aceptadas", Modifier.weight(1f), valueColor = AppColors.Success)
                Box(modifier = Modifier.width(1.dp).height(50.dp).background(AppColors.Border))
                SummaryMetric("$rejected", "Rechazadas", Modifier.weight(1f), valueColor = AppColors.Danger)
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
private fun ApplicationCard(app: ApplicationsViewModel.ApplicationUiItem) {
    val statusColor = when (app.statusType) {
        ApplicationsViewModel.StatusType.PENDING  -> Color(0xFF9AA4B2)
        ApplicationsViewModel.StatusType.ACCEPTED -> AppColors.Success
        ApplicationsViewModel.StatusType.REJECTED -> AppColors.Danger
    }
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp), color = AppColors.Surface
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).background(AppColors.PrimaryYellow.copy(alpha = 0.15f), CircleShape).border(1.dp, AppColors.PrimaryYellow.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("G", fontWeight = FontWeight.W900, color = Color(0xFF9A5B00))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.offerTitle, fontSize = 17.sp, fontWeight = FontWeight.W900)
                Spacer(Modifier.height(2.dp))
                Text("Aplicado el ${app.dateLabel}", color = AppColors.GreyText, fontSize = 14.sp)
            }
            Text(app.statusLabel, fontWeight = FontWeight.W900, color = statusColor, fontSize = 12.sp)
        }
    }
}

@Composable
private fun EmptyApplications(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(100.dp))
        Box(
            modifier = Modifier.size(120.dp).border(1.dp, AppColors.Border, CircleShape).background(AppColors.Surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(46.dp), tint = Color(0xFFB6BFCC))
        }
        Spacer(Modifier.height(22.dp))
        Text("Aún no has aplicado", fontSize = 28.sp, fontWeight = FontWeight.W900)
        Spacer(Modifier.height(10.dp))
        Text("Explora las ofertas disponibles\ny aplica a las que te interesen.", fontSize = 18.sp, color = AppColors.GreyText, lineHeight = 24.sp)
    }
}
