package com.iyas.budgetin.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(id: String) = "edit_transaction/$id"
    }
    object History : Screen("history")
    object Charts : Screen("charts")
    object Account : Screen("account")
}
