package com.example.goatly.ui.home

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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
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
fun StudentHomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val offers by homeViewModel.offers.collectAsState()
    val selectedCategory by homeViewModel.selectedCategory.collectAsState()
    val categories by homeViewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ofertas", fontWeight = FontWeight.W800) },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 14.dp).size(36.dp).background(AppColors.PrimaryYellow, CircleShape), contentAlignment = Alignment.Center) {
                        Text("G", fontWeight = FontWeight.W900, color = Color.White)
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 14.dp)) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(28.dp))
                        Box(modifier = Modifier.size(9.dp).background(Color.Red, CircleShape).align(Alignment.TopEnd))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )
        },
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Filtros de categoría
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { homeViewModel.selectCategory(null) },
                            label = { Text("Todas", fontWeight = FontWeight.W700) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.DarkText,
                                selectedLabelColor = AppColors.PrimaryYellow
                            )
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { homeViewModel.selectCategory(cat) },
                            label = { Text(cat, fontWeight = FontWeight.W700) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.DarkText,
                                selectedLabelColor = AppColors.PrimaryYellow
                            )
                        )
                    }
                }
            }

            // Header
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Ofertas disponibles", fontSize = 22.sp, fontWeight = FontWeight.W800)
                    Text("${offers.size} resultados", color = AppColors.GreyText, fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            if (offers.isEmpty()) {
                item { EmptyOffers(modifier = Modifier.padding(horizontal = 16.dp)) }
            } else {
                items(offers) { offer ->
                    OfferCard(
                        offer = offer,
                        onClick = { onNavigateToDetail(offer.id) },
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OfferCard(offer: HomeViewModel.OfferUiItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = AppColors.Surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.background(AppColors.PrimaryYellow.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(offer.category, fontSize = 12.sp, fontWeight = FontWeight.W700, color = Color(0xFF9A5B00))
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = AppColors.GreyText)
                    Spacer(Modifier.width(2.dp))
                    Text(offer.location, fontSize = 12.sp, color = AppColors.GreyText)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(offer.title, fontSize = 18.sp, fontWeight = FontWeight.W900)
            Spacer(Modifier.height(6.dp))
            Text(offer.categoryAndValue, color = AppColors.GreyText, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text(offer.whenAndDuration, color = AppColors.GreyText, fontSize = 15.sp)
        }
    }
}

@Composable
private fun EmptyOffers(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(14.dp)), shape = RoundedCornerShape(14.dp), color = AppColors.Surface) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No hay ofertas disponibles", fontSize = 20.sp, fontWeight = FontWeight.W900)
            Spacer(Modifier.height(8.dp))
            Text("Intenta con otra categoría o vuelve más tarde.", color = AppColors.GreyText, fontSize = 15.sp)
        }
    }
}