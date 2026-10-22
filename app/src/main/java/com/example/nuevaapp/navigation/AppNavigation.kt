package com.example.nuevaapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nuevaapp.model.AppDatabase
import com.example.nuevaapp.repository.EquipoRepository
import com.example.nuevaapp.repository.MedicionRepository
import com.example.nuevaapp.ui.screens.EquipoScreen
import com.example.nuevaapp.ui.screens.HistorialScreen
import com.example.nuevaapp.ui.screens.LoginScreen
import com.example.nuevaapp.ui.screens.MedicionScreen
import com.example.nuevaapp.ui.screens.MenuModulosScreen
import com.example.nuevaapp.viewmodel.EquipoViewModel
import com.example.nuevaapp.viewmodel.HistorialViewModel
import com.example.nuevaapp.viewmodel.MedicionViewModel

// Rutas de navegación estáticas
object Rutas {
    const val LOGIN = "login"
    const val MENU = "menu"
    const val EQUIPOS = "equipos"
    const val MEDICIONES = "mediciones"
    const val HISTORIAL = "historial"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Inicialización de la base de datos SQLite y los Repositorios
    val database = AppDatabase.getDatabase(context)
    val equipoRepository = EquipoRepository(database.equipoDao())
    val medicionRepository = MedicionRepository(database.medicionDao())

    // Instanciación de ViewModels con sus respectivos repositorios
    val equipoViewModel = EquipoViewModel(equipoRepository)
    val medicionViewModel = MedicionViewModel(medicionRepository)
    val historialViewModel = HistorialViewModel(medicionRepository)

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        // Pantalla de Login
        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Rutas.MENU) {
                        popUpTo(Rutas.LOGIN) { inclusive = true } // Evita volver al login con el botón de regreso
                    }
                }
            )
        }

        // Menú de Módulos
        composable(Rutas.MENU) {
            MenuModulosScreen(
                onNavegarAEquipos = { navController.navigate(Rutas.EQUIPOS) },
                onNavegarAMediciones = { navController.navigate(Rutas.MEDICIONES) },
                onNavegarAHistorial = { navController.navigate(Rutas.HISTORIAL) },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.MENU) { inclusive = true }
                    }
                }
            )
        }

        // Módulo de Equipos
        composable(Rutas.EQUIPOS) {
            EquipoScreen(viewModel = equipoViewModel)
        }

        // Módulo de Mediciones
        composable(Rutas.MEDICIONES) {
            MedicionScreen(viewModel = medicionViewModel)
        }

        // Módulo de Historial
        composable(Rutas.HISTORIAL) {
            HistorialScreen(viewModel = historialViewModel)
        }
    }
}