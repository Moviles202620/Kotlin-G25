package com.example.goatly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.goatly.ui.applications.ApplicationDetailScreen
import com.example.goatly.ui.applications.ApplicationsViewModel
import com.example.goatly.ui.auth.AuthViewModel
import com.example.goatly.ui.auth.StudentLoginScreen
import com.example.goatly.ui.auth.StudentRegisterScreen
import com.example.goatly.ui.home.OfferDetailScreen
import com.example.goatly.ui.navigation.Routes
import com.example.goatly.ui.navigation.StudentShell
import com.example.goatly.ui.profile.ChangePasswordScreen
import com.example.goatly.ui.profile.EditProfileScreen
import com.example.goatly.ui.profile.ProfileViewModel
import com.example.goatly.ui.profile.SettingsScreen
import com.example.goatly.ui.theme.GoatlyTheme

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val appsViewModel: ApplicationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GoatlyTheme { GoatlyStudentApp(authViewModel, profileViewModel, appsViewModel) } }
    }
}

@Composable
fun GoatlyStudentApp(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    appsViewModel: ApplicationsViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            StudentLoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    authViewModel.user.value?.let { u ->
                        profileViewModel.seedFromAuth(u.name, u.email, u.major, u.language, u.isDarkMode)
                    }
                    profileViewModel.loadProfile()
                    navController.navigate(Routes.SHELL) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            StudentRegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    authViewModel.user.value?.let { u ->
                        profileViewModel.seedFromAuth(u.name, u.email, u.major, u.language, u.isDarkMode)
                    }
                    profileViewModel.loadProfile()
                    navController.navigate(Routes.SHELL) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Routes.SHELL) {
            StudentShell(
                profileViewModel = profileViewModel,
                appsViewModel = appsViewModel,
                onNavigateToOfferDetail = { offerId -> navController.navigate(Routes.offerDetail(offerId)) },
                onNavigateToApplicationDetail = { appId ->
                    navController.navigate(Routes.applicationDetail(appId))
                },
                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onLogout = {
                    authViewModel.logout()
                    profileViewModel.clear()
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.SHELL) { inclusive = true } }
                }
            )
        }

        composable(Routes.OFFER_DETAIL) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: return@composable
            val currentUser by authViewModel.user.collectAsState()
            OfferDetailScreen(
                offerId = offerId,
                userName = currentUser?.name,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                profileViewModel = profileViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) }
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                profileViewModel = profileViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.APPLICATION_DETAIL) {
            ApplicationDetailScreen(
                appsViewModel = appsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
