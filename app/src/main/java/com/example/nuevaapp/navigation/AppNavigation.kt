package com.example.nuevaapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nuevaapp.model.AppDatabase
import com.example.nuevaapp.repository.AuthRepository
import com.example.nuevaapp.repository.EquipoRepository
import com.example.nuevaapp.repository.MedicionRepository
import com.example.nuevaapp.ui.screens.EquipoScreen
import com.example.nuevaapp.ui.screens.HistorialScreen
import com.example.nuevaapp.ui.screens.LoginScreen
import com.example.nuevaapp.ui.screens.MedicionScreen
import com.example.nuevaapp.ui.screens.MenuModulosScreen
import com.example.nuevaapp.viewmodel.AppViewModelFactory
import com.example.nuevaapp.viewmodel.AuthViewModel
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
    val database = remember { AppDatabase.getDatabase(context) }
    val equipoRepository = remember { EquipoRepository(database.equipoDao()) }
    val medicionRepository = remember { MedicionRepository(database.medicionDao()) }
    val authRepository = remember { AuthRepository(database.usuarioDao()) }

    // Instanciación correcta mediante la fábrica (ViewModelProvider.Factory)
    val factory = remember { AppViewModelFactory(equipoRepository, medicionRepository, authRepository) }

    val equipoViewModel: EquipoViewModel = viewModel(factory = factory)
    val medicionViewModel: MedicionViewModel = viewModel(factory = factory)
    val historialViewModel: HistorialViewModel = viewModel(factory = factory)
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        // Pantalla de Login
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Rutas.MENU) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Menú de Módulos
        composable(Rutas.MENU) {
            val userRole by authViewModel.currentUserRole.collectAsState()
            MenuModulosScreen(
                userRole = userRole,
                onNavegarAEquipos = { navController.navigate(Rutas.EQUIPOS) },
                onNavegarAMediciones = { navController.navigate(Rutas.MEDICIONES) },
                onNavegarAHistorial = { navController.navigate(Rutas.HISTORIAL) },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
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

        // Módulo de Mediciones (Acepta ID opcional para edición)
        composable(
            route = "${Rutas.MEDICIONES}?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val registroId = backStackEntry.arguments?.getInt("id") ?: -1
            val currentUserId by authViewModel.currentUserId.collectAsState()
            val currentUserName by authViewModel.currentUserName.collectAsState()
            val userRole by authViewModel.currentUserRole.collectAsState()

            LaunchedEffect(currentUserId, userRole) {
                medicionViewModel.setUserContext(currentUserId, userRole)
            }

            MedicionScreen(
                viewModel = medicionViewModel,
                equipoViewModel = equipoViewModel,
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                registroId = registroId,
                onVolver = { navController.popBackStack() }
            )
        }

        // Módulo de Historial
        composable(Rutas.HISTORIAL) {
            val currentUserId by authViewModel.currentUserId.collectAsState()
            val userRole by authViewModel.currentUserRole.collectAsState()

            LaunchedEffect(currentUserId, userRole) {
                historialViewModel.setUserContext(currentUserId, userRole)
            }

            HistorialScreen(
                viewModel = historialViewModel,
                onEditarRegistro = { id ->
                    navController.navigate("${Rutas.MEDICIONES}?id=$id")
                }
            )
        }
    }
}
