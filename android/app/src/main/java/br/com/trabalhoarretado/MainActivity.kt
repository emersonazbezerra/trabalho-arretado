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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.trabalhoarretado.domain.AuthEvent
import br.com.trabalhoarretado.domain.AuthEvents
import br.com.trabalhoarretado.ui.auth.LoginScreen
import br.com.trabalhoarretado.ui.auth.RegisterScreen
import br.com.trabalhoarretado.ui.favorites.FavoritesScreen
import br.com.trabalhoarretado.ui.home.HomeScreen
import br.com.trabalhoarretado.ui.navigation.Screen
import br.com.trabalhoarretado.ui.professional.ProfessionalProfileScreen
import br.com.trabalhoarretado.ui.professional.PublishServiceScreen
import br.com.trabalhoarretado.ui.profile.MyProfileScreen
import br.com.trabalhoarretado.ui.search.SearchScreen
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
                                onSearch = { category ->
                                    navController.navigate(Screen.Search.build(category))
                                },
                                onProfessionalClick = { id ->
                                    navController.navigate(Screen.Professional.build(id))
                                },
                                onFavorites = { navController.navigate(Screen.Favorites.route) },
                                onMyProfile = { navController.navigate(Screen.MyProfile.route) },
                                onLoggedOut = {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                            )
                        }
                        composable(
                            route = Screen.Search.route,
                            arguments =
                                listOf(
                                    navArgument("category") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                        nullable = false
                                    },
                                ),
                        ) { backStackEntry ->
                            val raw = backStackEntry.arguments?.getString("category").orEmpty()
                            SearchScreen(
                                initialCategory = raw.takeIf { it.isNotBlank() },
                                onBack = { navController.popBackStack() },
                                onProfessionalClick = { id ->
                                    navController.navigate(Screen.Professional.build(id))
                                },
                            )
                        }
                        composable(
                            route = Screen.Professional.route,
                            arguments = listOf(navArgument("id") { type = NavType.StringType }),
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id").orEmpty()
                            ProfessionalProfileScreen(
                                professionalId = id,
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable(Screen.Favorites.route) {
                            FavoritesScreen(
                                onBack = { navController.popBackStack() },
                                onProfessionalClick = { id ->
                                    navController.navigate(Screen.Professional.build(id))
                                },
                            )
                        }
                        composable(Screen.MyProfile.route) {
                            MyProfileScreen(
                                onBack = { navController.popBackStack() },
                                onPublishNew = { navController.navigate(Screen.PublishService.build(null)) },
                                onEditService = { serviceId ->
                                    navController.navigate(Screen.PublishService.build(serviceId))
                                },
                            )
                        }
                        composable(
                            route = Screen.PublishService.route,
                            arguments =
                                listOf(
                                    navArgument("serviceId") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                        nullable = false
                                    },
                                ),
                        ) { backStackEntry ->
                            val serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty()
                            PublishServiceScreen(
                                serviceId = serviceId.takeIf { it.isNotBlank() },
                                onBack = { navController.popBackStack() },
                                onDone = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
