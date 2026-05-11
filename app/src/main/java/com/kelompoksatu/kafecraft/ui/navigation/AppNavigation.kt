package com.kelompoksatu.kafecraft.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kelompoksatu.kafecraft.data.AppDatabase
import com.kelompoksatu.kafecraft.data.SessionManager
import com.kelompoksatu.kafecraft.ui.auth.*
import com.kelompoksatu.kafecraft.ui.home.HomeViewModel
import com.kelompoksatu.kafecraft.ui.home.RecipeDetailScreen
import com.kelompoksatu.kafecraft.ui.main.MainScreen
import com.kelompoksatu.kafecraft.ui.myrecipes.CreateEditRecipeScreen
import com.kelompoksatu.kafecraft.ui.myrecipes.MyRecipesViewModel

@Composable
fun AppNavigation(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(sessionManager))
    
    val bookmarkDao = remember { AppDatabase.getDatabase(context).bookmarkDao() }
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(bookmarkDao, sessionManager))
    val myRecipesViewModel: MyRecipesViewModel = viewModel(factory = MyRecipesViewModel.Factory(sessionManager))

    val startDestination = if (sessionManager.isLoggedIn()) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToForgot = { navController.navigate("forgot_password") }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangePassword = {
                    navController.navigate("change_password") {
                        popUpTo("forgot_password") { inclusive = true }
                    }
                }
            )
        }

        composable("change_password") {
            ChangePasswordScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) // clear backstack
                    }
                }
            )
        }

        composable("home") {
            MainScreen(
                sessionManager = sessionManager,
                homeViewModel = homeViewModel,
                myRecipesViewModel = myRecipesViewModel,
                rootNavController = navController,
                onLogout = {
                    sessionManager.logout()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(
            route = "recipe_detail/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailScreen(
                recipeId = recipeId,
                viewModel = homeViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "create_edit_recipe?recipeId={recipeId}",
            arguments = listOf(navArgument("recipeId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            CreateEditRecipeScreen(
                recipeId = recipeId,
                viewModel = myRecipesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
