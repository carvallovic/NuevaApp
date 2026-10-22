package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.viewmodel.HistorialViewModel

// @Composable renderiza la interfaz declarativa en Jetpack Compose
@Composable
fun HistorialScreen(viewModel: HistorialViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de Mediciones",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de búsqueda en tiempo real
        OutlinedTextField(
            value = state.textoBusqueda,
            onValueChange = { viewModel.onBusquedaCambiada(it) },
            label = { Text("Buscar en historial...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (state.mediciones.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.textoBusqueda.isBlank())
                        "No hay mediciones registradas en la base de datos."
                    else
                        "No se encontraron coincidencias.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // Despliega la lista recuperada de la tabla SQLite
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.mediciones, key = { it.id }) { medicion ->
                    HistorialItem(
                        medicion = medicion,
                        onDelete = { viewModel.borrarMedicion(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistorialItem(medicion: Medicion, onDelete: (Medicion) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${medicion.valor} ${medicion.unidad}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Equipo ID: ${medicion.equipoId}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Fecha: ${medicion.fecha}",
                    style = MaterialTheme.typography.labelMedium
                )
                if (medicion.observaciones.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Obs: ${medicion.observaciones}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(onClick = { onDelete(medicion) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar registro",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}