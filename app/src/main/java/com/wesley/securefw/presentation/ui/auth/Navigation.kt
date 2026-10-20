package com.wesley.securefw.presentation.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.wesley.securefw.data.local.entity.TransactionEntity
import com.wesley.securefw.presentation.ui.auth.HomeScreen
import com.wesley.securefw.presentation.ui.auth.auth.LoginScreen
import com.wesley.securefw.presentation.ui.auth.auth.RegisterScreen
import com.wesley.securefw.presentation.ui.auth.transaction.AddEditTransactionScreen
import com.wesley.securefw.presentation.viewmodel.AuthViewModel
import com.google.gson.Gson

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object EditTransaction : Screen("edit_transaction/{transactionJson}") {
        fun createRoute(transaction: TransactionEntity): String {
            val json = Gson().toJson(transaction)
            return "edit_transaction/$json"
        }
    }
}

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.state.collectAsStateWithLifecycle()

    // Usuário logado (pegar direto do AuthRepository via ViewModel)
    val userId = authState.userId ?: ""

    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) Screen.Home.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                userId = userId,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onEditTransaction = { transaction ->
                    navController.navigate(Screen.EditTransaction.createRoute(transaction))
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddEditTransactionScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(navArgument("transactionJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val json = backStackEntry.arguments?.getString("transactionJson")
            val transaction = json?.let { Gson().fromJson(it, TransactionEntity::class.java) }
            AddEditTransactionScreen(
                userId = userId,
                transaction = transaction,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}