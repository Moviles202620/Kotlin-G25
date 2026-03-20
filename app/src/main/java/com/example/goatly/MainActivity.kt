package com.example.goatly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.goatly.ui.auth.AuthViewModel
import com.example.goatly.ui.auth.StudentLoginScreen
import com.example.goatly.ui.auth.StudentRegisterScreen
import com.example.goatly.ui.home.OfferDetailScreen
import com.example.goatly.ui.navigation.Routes
import com.example.goatly.ui.navigation.StudentShell
import com.example.goatly.ui.theme.GoatlyTheme

class MainActivity : FragmentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tryBiometric()
    }

    private fun tryBiometric() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            showApp()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                showApp()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                showApp()
            }
            override fun onAuthenticationFailed() { }
        }

        BiometricPrompt(this as FragmentActivity, executor, callback).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Bienvenido a Goatly")
                .setSubtitle("Confirma tu identidad para continuar")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        )
    }

    private fun showApp() {
        setContent { GoatlyTheme { GoatlyStudentApp(authViewModel) } }
    }
}

@Composable
fun GoatlyStudentApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val currentUser by authViewModel.user.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            StudentLoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.SHELL) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            StudentRegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.SHELL) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Routes.SHELL) {
            StudentShell(
                userName       = currentUser?.name,
                userMajor      = currentUser?.major,
                userUniversity = currentUser?.university,
                onNavigateToOfferDetail = { offerId -> navController.navigate(Routes.offerDetail(offerId)) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.SHELL) { inclusive = true } }
                }
            )
        }

        composable(Routes.OFFER_DETAIL) { backStackEntry ->
            val offerId = backStackEntry.arguments?.getString("offerId") ?: return@composable
            OfferDetailScreen(
                offerId = offerId,
                userName = currentUser?.name,
                onBack = { navController.popBackStack() }
            )
        }
    }
}