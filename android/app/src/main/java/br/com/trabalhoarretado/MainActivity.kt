package br.com.trabalhoarretado

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.trabalhoarretado.domain.AuthEvent
import br.com.trabalhoarretado.domain.AuthEvents
import br.com.trabalhoarretado.ui.auth.LoginScreen
import br.com.trabalhoarretado.ui.auth.RegisterScreen
import br.com.trabalhoarretado.ui.home.HomeScreen
import br.com.trabalhoarretado.ui.navigation.Screen
import br.com.trabalhoarretado.ui.splash.SplashScreen
import br.com.trabalhoarretado.ui.theme.TrabalhoArretadoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrabalhoArretadoTheme {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                ) {
                    val navController = rememberNavController()

                    LaunchedEffect(Unit) {
                        AuthEvents.events.collect { event ->
                            if (event is AuthEvent.Unauthorized) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                onAuthenticated = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                },
                                onUnauthenticated = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                },
                            )
                        }
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoggedIn = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                            )
                        }
                        composable(Screen.Register.route) {
                            RegisterScreen(
                                onRegistered = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onBackToLogin = { navController.popBackStack() },
                            )
                        }
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onLoggedOut = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
