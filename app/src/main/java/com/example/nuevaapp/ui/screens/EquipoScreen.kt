package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nuevaapp.model.Equipo
import com.example.nuevaapp.viewmodel.EquipoViewModel

@Composable
fun EquipoScreen(viewModel: EquipoViewModel) {
    val state by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("Operativo") }
    var equipoEnEdicion by remember { mutableStateOf<Equipo?>(null) }
    var equipoAEliminar by remember { mutableStateOf<Equipo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (equipoEnEdicion == null) "Registro de Equipos (SQLite)" else "Editando Equipo",
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (equipoEnEdicion == null) {
                        viewModel.agregarEquipo(nombre, codigo, ubicacion, estado)
                    } else {
                        equipoEnEdicion?.let { equipo ->
                            viewModel.editarEquipo(
                                equipo.copy(
                                    nombre = nombre,
                                    codigo = codigo,
                                    ubicacion = ubicacion,
                                    estado = estado
                                )
                            )
                        }
                        equipoEnEdicion = null
                    }
                    nombre = ""
                    codigo = ""
                    ubicacion = ""
                    estado = "Operativo"
                },
                modifier = Modifier.weight(1.8f)
            ) {
                Text(if (equipoEnEdicion == null) "Guardar en BD" else "Guardar Cambios")
            }

            if (equipoEnEdicion != null) {
                OutlinedButton(
                    onClick = {
                        equipoEnEdicion = null
                        nombre = ""
                        codigo = ""
                        ubicacion = ""
                        estado = "Operativo"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
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
                    EquipoCard(
                        equipo = equipo,
                        onEdit = {
                            equipoEnEdicion = it
                            nombre = it.nombre
                            codigo = it.codigo
                            ubicacion = it.ubicacion
                            estado = it.estado
                        },
                        onDelete = { equipoAEliminar = it }
                    )
                }
            }
        }
    }

    // Modal de confirmación colocado DENTRO de EquipoScreen
    equipoAEliminar?.let { equipo ->
        AlertDialog(
            onDismissRequest = { equipoAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar el equipo \"${equipo.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.borrarEquipo(equipo)
                        equipoAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { equipoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EquipoCard(equipo: Equipo, onEdit: (Equipo) -> Unit, onDelete: (Equipo) -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = equipo.nombre, style = MaterialTheme.typography.titleMedium)
                Text(text = "TAG: ${equipo.codigo} | Ubic: ${equipo.ubicacion}")
                Text(text = "Estado: ${equipo.estado}", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                TextButton(onClick = { onEdit(equipo) }) {
                    Text("Editar")
                }
                TextButton(onClick = { onDelete(equipo) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}