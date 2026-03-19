package com.example.goatly.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.goatly.ui.applications.ApplicationsViewModel
import com.example.goatly.ui.applications.MyApplicationsScreen
import com.example.goatly.ui.home.StudentHomeScreen
import com.example.goatly.ui.profile.ProfileViewModel
import com.example.goatly.ui.profile.StudentProfileScreen
import com.example.goatly.ui.theme.AppColors

@Composable
fun StudentShell(
    profileViewModel: ProfileViewModel,
    appsViewModel: ApplicationsViewModel,
    onNavigateToOfferDetail: (String) -> Unit,
    onNavigateToApplicationDetail: (Int) -> Unit,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            NavigationBar(containerColor = AppColors.Surface) {
                listOf(
                    Triple(Icons.Default.Home, "Inicio", 0),
                    Triple(Icons.AutoMirrored.Filled.List, "Mis apps", 1),
                    Triple(Icons.Default.Person, "Perfil", 2)
                ).forEach { (icon, label, index) ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AppColors.PrimaryYellow,
                            selectedTextColor = AppColors.PrimaryYellow,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color(0xFF8A94A6),
                            unselectedTextColor = Color(0xFF8A94A6)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = androidx.compose.ui.Modifier.padding(padding)) {
            when (selectedIndex) {
                0 -> StudentHomeScreen(onNavigateToDetail = onNavigateToOfferDetail)
                1 -> MyApplicationsScreen(
                    appsViewModel = appsViewModel,
                    onSessionExpired = onLogout,
                    onNavigateToDetail = { app ->
                        appsViewModel.selectApplication(app)
                        onNavigateToApplicationDetail(app.id)
                    }
                )
                2 -> StudentProfileScreen(
                    profileViewModel = profileViewModel,
                    appsViewModel = appsViewModel,
                    onEditProfile = onEditProfile,
                    onSettings = onSettings,
                    onLogout = onLogout
                )
            }
        }
    }
}
