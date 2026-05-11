package com.kelompoksatu.kafecraft.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kelompoksatu.kafecraft.data.SessionManager
import com.kelompoksatu.kafecraft.ui.home.HomeScreen
import com.kelompoksatu.kafecraft.ui.home.HomeViewModel
import com.kelompoksatu.kafecraft.ui.myrecipes.MyRecipesScreen
import com.kelompoksatu.kafecraft.ui.myrecipes.MyRecipesViewModel
import com.kelompoksatu.kafecraft.ui.profile.ProfileScreen

@Composable
fun MainScreen(
    sessionManager: SessionManager,
    homeViewModel: HomeViewModel,
    myRecipesViewModel: MyRecipesViewModel,
    rootNavController: NavController,
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(bottomNavController) }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home_tab",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_tab") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToDetail = { recipeId ->
                        rootNavController.navigate("recipe_detail/$recipeId")
                    }
                )
            }
            composable("menu_tab") {
                MyRecipesScreen(
                    viewModel = myRecipesViewModel,
                    onNavigateToCreate = {
                        rootNavController.navigate("create_edit_recipe?recipeId=")
                    },
                    onNavigateToEdit = { recipeId ->
                        rootNavController.navigate("create_edit_recipe?recipeId=$recipeId")
                    }
                )
            }
            composable("profile_tab") {
                ProfileScreen(
                    sessionManager = sessionManager,
                    homeViewModel = homeViewModel,
                    myRecipesViewModel = myRecipesViewModel,
                    onLogout = onLogout,
                    onNavigateToDetail = { recipeId ->
                        rootNavController.navigate("recipe_detail/$recipeId")
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Home", "home_tab", Icons.Default.Home),
        BottomNavItem("Menu", "menu_tab", Icons.Default.AddCircle), // AddCircle to represent the icon in design
        BottomNavItem("Profile", "profile_tab", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF7A45),
                    selectedTextColor = Color(0xFFFF7A45),
                    indicatorColor = Color(0xFFF5E6E0),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                ),
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val title: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
