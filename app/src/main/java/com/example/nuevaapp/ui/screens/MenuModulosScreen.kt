package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nuevaapp.viewmodel.UserRole

@Composable
fun MenuModulosScreen(
    userRole: UserRole?,
    onNavegarAUsuarios: () -> Unit,
    onNavegarAEquipos: () -> Unit,
    onNavegarAMediciones: () -> Unit,
    onNavegarAHistorial: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Menú de Módulos",
            style = MaterialTheme.typography.headlineMedium
        )
        val roleDesc = when (userRole) {
            UserRole.ADMIN -> "Perfil: Administrador (ADMIN)"
            UserRole.TECNICO -> "Perfil: Técnico (TECNICO)"
            UserRole.CLIENTE -> "Perfil: Cliente (CLIENTE)"
            null -> "Perfil: Usuario"
        }
        Text(
            text = roleDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ADMIN: Gestión de Usuarios (Primer módulo arriba)
        if (userRole == UserRole.ADMIN) {
            Button(
                onClick = onNavegarAUsuarios,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Gestión de Usuarios")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ADMIN: Gestión de Equipos
        if (userRole == UserRole.ADMIN) {
            Button(
                onClick = onNavegarAEquipos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Gestión de Equipos")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ADMIN y TECNICO: Registro de Mediciones
        if (userRole == UserRole.ADMIN || userRole == UserRole.TECNICO) {
            Button(
                onClick = onNavegarAMediciones,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Registro de Mediciones")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // TODOS (ADMIN, TECNICO, CLIENTE): Historial de Mediciones
        OutlinedButton(
            onClick = onNavegarAHistorial,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Historial de Mediciones")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para cerrar sesión
        TextButton(onClick = onCerrarSesion) {
            Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error)
        }
    }
}
