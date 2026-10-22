package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// @Composable construirá la pantalla del Menú Principal
@Composable
fun MenuModulosScreen(
    onNavegarAEquipos: () -> Unit,
    onNavegarAMediciones: () -> Unit,
    onNavegarAHistorial: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Menú de Módulos",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Seleccione una opción para continuar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón/Opción hacia Módulo de Equipos
        Button(
            onClick = onNavegarAEquipos,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Gestión de Equipos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón/Opción hacia Módulo de Mediciones
        Button(
            onClick = onNavegarAMediciones,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Registro de Mediciones")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón/Opción hacia Historial
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