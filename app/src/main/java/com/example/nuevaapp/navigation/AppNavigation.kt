package com.example.nuevaapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nuevaapp.ui.screens.EquipoScreen
import com.example.nuevaapp.ui.screens.LoginScreen
import com.example.nuevaapp.ui.screens.MenuModulosScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // 1. Pantalla de Login
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("menu") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 2. Pantalla del Menú Principal
        composable("menu") {
            MenuModulosScreen(
                nombreTecnico = "Admin", // Aquí simulamos el nombre del usuario
                onNavigateToEquipos = { navController.navigate("equipos") },
                onNavigateToRegistro = { },
                onNavigateToHistorial = { },
                onNavigateToProtocolos = { },
                onNavigateToSync = { }
            )
        }

        // 3. Pantalla del Listado de Equipos
        composable("equipos") {
            EquipoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}