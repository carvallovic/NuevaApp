package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nuevaapp.model.Equipo
import com.example.nuevaapp.viewmodel.EquipoViewModel

// @Composable indica que esta función renderiza elementos visuales en la interfaz de usuario.
@Composable
fun EquipoScreen(viewModel: EquipoViewModel) {
    // CollectAsStateWithLifecycle o collectAsState observa los cambios de estado en tiempo real.
    val state by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Operativo") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Registro de Equipos (SQLite)",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Formulario de entrada de datos
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del Equipo") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it },
            label = { Text("Código / TAG") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Ubicación / Planta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.agregarEquipo(nombre, codigo, ubicacion, estado)
                nombre = ""
                codigo = ""
                ubicacion = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar en BD")
        }

        // Mostrar alerta de error si existe
        state.error?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista que despliega los registros de SQLite
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.equipos, key = { it.id }) { equipo ->
                    EquipoCard(equipo = equipo, onDelete = { viewModel.borrarEquipo(it) })
                }
            }
        }
    }
}

@Composable
fun EquipoCard(equipo: Equipo, onDelete: (Equipo) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = equipo.nombre, style = MaterialTheme.typography.titleMedium)
                Text(text = "TAG: ${equipo.codigo} | Ubic: ${equipo.ubicacion}")
                Text(text = "Estado: ${equipo.estado}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { onDelete(equipo) }) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}