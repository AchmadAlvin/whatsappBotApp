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

    NavHost(navController, startDestination = if (sessionManager.isLoggedIn()) "home" else "login") {
        composable("login") {
            LoginScreen(authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToHome = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                onNavigateToForgot = { navController.navigate("forgot_password") })
        }
        composable("register") { RegisterScreen(authViewModel) { navController.popBackStack() } }
        composable("forgot_password") {
            ForgotPasswordScreen(authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangePassword = { navController.navigate("change_password") { popUpTo("forgot_password") { inclusive = true } } })
        }
        composable("change_password") {
            ChangePasswordScreen(authViewModel) { navController.navigate("login") { popUpTo(0) } }
        }
        composable("home") {
            MainScreen(sessionManager, homeViewModel, myRecipesViewModel, navController) {
                sessionManager.logout(); navController.navigate("login") { popUpTo(0) }
            }
        }
        composable("recipe_detail/{recipeId}", arguments = listOf(navArgument("recipeId") { type = NavType.StringType })) {
            RecipeDetailScreen(it.arguments?.getString("recipeId") ?: "", homeViewModel) { navController.popBackStack() }
        }
        composable("create_edit_recipe?recipeId={recipeId}", arguments = listOf(navArgument("recipeId") { type = NavType.StringType; nullable = true; defaultValue = null })) {
            CreateEditRecipeScreen(it.arguments?.getString("recipeId") ?: "", myRecipesViewModel) { navController.popBackStack() }
        }
    }
}
