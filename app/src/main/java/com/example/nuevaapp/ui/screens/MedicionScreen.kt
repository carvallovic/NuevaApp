package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.viewmodel.MedicionViewModel

@Composable
fun MedicionScreen(
    viewModel: MedicionViewModel,
    registroId: Int = -1,
    onVolver: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    var equipoId by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    var medicionEnEdicion by remember { mutableStateOf<Medicion?>(null) }
    var medicionAEliminar by remember { mutableStateOf<Medicion?>(null) }

    // Cargar medición cuando se pasa un registroId desde la navegación
    LaunchedEffect(registroId, state.mediciones) {
        if (registroId != -1) {
            val encontrada = state.mediciones.find { it.id == registroId }
            if (encontrada != null) {
                medicionEnEdicion = encontrada
                equipoId = encontrada.equipoId.toString()
                valor = encontrada.valor.toString()
                unidad = encontrada.unidad
                observaciones = encontrada.observaciones
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (medicionEnEdicion == null) "Registro de Mediciones" else "Editando Medición",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Formulario
        OutlinedTextField(
            value = equipoId,
            onValueChange = { equipoId = it },
            label = { Text("ID del Equipo") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Valor Medido") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = unidad,
            onValueChange = { unidad = it },
            label = { Text("Unidad (Ej: V, A, °C, PSI)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones (Opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val eqId = equipoId.toIntOrNull() ?: 0
                    val valMed = valor.toDoubleOrNull() ?: 0.0

                    if (medicionEnEdicion == null) {
                        val nuevaMedicion = Medicion(
                            equipoId = eqId,
                            valor = valMed,
                            unidad = unidad,
                            observaciones = observaciones,
                            fecha = "29/10/2026"
                        )
                        viewModel.agregarMedicion(nuevaMedicion)
                    } else {
                        medicionEnEdicion?.let { med ->
                            viewModel.editarMedicion(
                                med.copy(
                                    equipoId = eqId,
                                    valor = valMed,
                                    unidad = unidad,
                                    observaciones = observaciones
                                )
                            )
                        }
                        medicionEnEdicion = null
                    }
                    equipoId = ""
                    valor = ""
                    unidad = ""
                    observaciones = ""

                    if (registroId != -1) {
                        onVolver()
                    }
                },
                modifier = Modifier.weight(1.8f)
            ) {
                Text(if (medicionEnEdicion == null) "Registrar Medición" else "Guardar Cambios")
            }

            if (medicionEnEdicion != null) {
                OutlinedButton(
                    onClick = {
                        medicionEnEdicion = null
                        equipoId = ""
                        valor = ""
                        unidad = ""
                        observaciones = ""
                        if (registroId != -1) {
                            onVolver()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            }
        }

        state.error?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.mediciones, key = { it.id }) { medicion ->
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
                                Text(text = "${medicion.valor} ${medicion.unidad}", style = MaterialTheme.typography.titleMedium)
                                Text(text = "Equipo ID: ${medicion.equipoId} | Fecha: ${medicion.fecha}")
                                if (medicion.observaciones.isNotBlank()) {
                                    Text(text = "Obs: ${medicion.observaciones}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Row {
                                IconButton(onClick = {
                                    medicionEnEdicion = medicion
                                    equipoId = medicion.equipoId.toString()
                                    valor = medicion.valor.toString()
                                    unidad = medicion.unidad
                                    observaciones = medicion.observaciones
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { medicionAEliminar = medicion }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de confirmación para eliminar
    medicionAEliminar?.let { medicion ->
        AlertDialog(
            onDismissRequest = { medicionAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar la medición de ${medicion.valor} ${medicion.unidad}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.borrarMedicion(medicion)
                        medicionAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { medicionAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}