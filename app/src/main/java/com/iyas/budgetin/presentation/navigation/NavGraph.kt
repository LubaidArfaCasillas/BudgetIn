package com.iyas.budgetin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.iyas.budgetin.presentation.account.AccountScreen
import com.iyas.budgetin.presentation.auth.LoginScreen
import com.iyas.budgetin.presentation.auth.RegisterScreen
import com.iyas.budgetin.presentation.charts.ChartsScreen
import com.iyas.budgetin.presentation.home.HomeScreen
import com.iyas.budgetin.presentation.transaction.AddTransactionScreen
import com.iyas.budgetin.presentation.transaction.HistoryScreen

@Composable
fun BudgetInNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val startDest = if (FirebaseAuth.getInstance().currentUser != null) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDest
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
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                onNavigateToCharts = { navController.navigate(Screen.Charts.route) { launchSingleTop = true } },
                onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToAccount = { navController.navigate(Screen.Account.route) { launchSingleTop = true } }
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
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
