package br.com.trabalhoarretado.ui.navigation

sealed class Screen(
    val route: String,
) {
    data object Splash : Screen("splash")

    data object Login : Screen("auth/login")

    data object Register : Screen("auth/register")

    data object Home : Screen("home")

    data object Search : Screen("search?category={category}") {
        fun build(category: String? = null): String =
            "search?category=${category.orEmpty()}"
    }

    data object Professional : Screen("professional/{id}") {
        fun build(id: String): String = "professional/$id"
    }

    data object Favorites : Screen("favorites")

    data object MyProfile : Screen("profile")

    data object PublishService : Screen("service?serviceId={serviceId}") {
        fun build(serviceId: String? = null): String = "service?serviceId=${serviceId.orEmpty()}"
    }
}
