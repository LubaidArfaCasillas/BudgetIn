package com.iyas.budgetin.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.iyas.budgetin.presentation.account.AccountScreen
import com.iyas.budgetin.presentation.auth.LoginScreen
import com.iyas.budgetin.presentation.auth.RegisterScreen
import com.iyas.budgetin.presentation.charts.ChartsScreen
import com.iyas.budgetin.presentation.home.HomeScreen
import com.iyas.budgetin.presentation.transaction.AddTransactionScreen
import com.iyas.budgetin.presentation.transaction.EditTransactionScreen
import com.iyas.budgetin.presentation.transaction.HistoryScreen

// Transisi halaman form: naik dari bawah sambil fade, dan sebaliknya saat kembali
private const val FORM_ANIM_MS = 300

private fun formEnter() =
    slideInVertically(animationSpec = tween(FORM_ANIM_MS)) { fullHeight -> fullHeight / 4 } +
            fadeIn(animationSpec = tween(FORM_ANIM_MS))

private fun formExit() =
    slideOutVertically(animationSpec = tween(FORM_ANIM_MS)) { fullHeight -> fullHeight / 4 } +
            fadeOut(animationSpec = tween(FORM_ANIM_MS))

@Composable
fun BudgetInNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val auth = FirebaseAuth.getInstance()
    var startDest by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startDest = Screen.Home.route
                } else {
                    auth.signOut()
                    startDest = Screen.Login.route
                }
            }
        } else {
            startDest = Screen.Login.route
        }
    }

    if (startDest == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDest!!
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToCharts = {
                    navController.navigate(Screen.Charts.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToAccount = {
                    navController.navigate(Screen.Account.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToEdit = { id -> navController.navigate(Screen.EditTransaction.createRoute(id)) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                onNavigateToCharts = { navController.navigate(Screen.Charts.route) { launchSingleTop = true } },
                onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToAccount = { navController.navigate(Screen.Account.route) { launchSingleTop = true } },
                onNavigateToEdit = { id -> navController.navigate(Screen.EditTransaction.createRoute(id)) }
            )
        }

        composable(Screen.Charts.route) {
            ChartsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                onNavigateToHistory = { navController.navigate(Screen.History.route) { launchSingleTop = true } },
                onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToAccount = { navController.navigate(Screen.Account.route) { launchSingleTop = true } }
            )
        }

        composable(Screen.Account.route) {
            AccountScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                onNavigateToHistory = { navController.navigate(Screen.History.route) { launchSingleTop = true } },
                onNavigateToCharts = { navController.navigate(Screen.Charts.route) { launchSingleTop = true } },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.AddTransaction.route,
            enterTransition = { formEnter() },
            popExitTransition = { formExit() }
        ) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType }),
            enterTransition = { formEnter() },
            popExitTransition = { formExit() }
        ) { backStackEntry ->
            EditTransactionScreen(
                transactionId = backStackEntry.arguments?.getString("transactionId").orEmpty(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
