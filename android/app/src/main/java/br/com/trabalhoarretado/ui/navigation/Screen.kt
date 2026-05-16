package br.com.trabalhoarretado.ui.navigation

sealed class Screen(
    val route: String,
) {
    data object Splash : Screen("splash")

    data object Login : Screen("auth/login")

    data object Register : Screen("auth/register")

    data object Home : Screen("home")
}
