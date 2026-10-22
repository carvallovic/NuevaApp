package com.example.nuevaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.viewmodel.MedicionViewModel

@Composable
fun MedicionScreen(viewModel: MedicionViewModel) {
    val state by viewModel.uiState.collectAsState()

    var equipoIdInput by remember { mutableStateOf("") }
    var valorInput by remember { mutableStateOf("") }
    var unidadInput by remember { mutableStateOf("") }
    var observacionesInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Registro de Mediciones", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = equipoIdInput,
            onValueChange = { equipoIdInput = it },
            label = { Text("ID del Equipo") },
            isError = state.erroresFormulario.equipoError != null,
            modifier = Modifier.fillMaxWidth()
        )
        state.erroresFormulario.equipoError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = valorInput,
            onValueChange = { valorInput = it },
            label = { Text("Valor Medido") },
            isError = state.erroresFormulario.valorError != null,
            modifier = Modifier.fillMaxWidth()
        )
        state.erroresFormulario.valorError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = unidadInput,
            onValueChange = { unidadInput = it },
            label = { Text("Unidad (Ej: V, A, °C, PSI)") },
            isError = state.erroresFormulario.unidadError != null,
            modifier = Modifier.fillMaxWidth()
        )
        state.erroresFormulario.unidadError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = observacionesInput,
            onValueChange = { observacionesInput = it },
            label = { Text("Observaciones (Opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.guardarMedicion(
                    equipoIdStr = equipoIdInput,
                    valorStr = valorInput,
                    unidad = unidadInput,
                    observaciones = observacionesInput
                )
                if (state.erroresFormulario.equipoError == null &&
                    state.erroresFormulario.valorError == null &&
                    state.erroresFormulario.unidadError == null) {
                    equipoIdInput = ""
                    valorInput = ""
                    unidadInput = ""
                    observacionesInput = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Medición")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.mediciones, key = { it.id }) { medicion ->
                    MedicionCard(medicion = medicion, onDelete = { viewModel.borrarMedicion(it) })
                }
            }
        }
    }
}

@Composable
fun MedicionCard(medicion: Medicion, onDelete: (Medicion) -> Unit) {
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
                Text(
                    text = "${medicion.valor} ${medicion.unidad}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = "Equipo ID: ${medicion.equipoId} | Fecha: ${medicion.fecha}")
                if (medicion.observaciones.isNotBlank()) {
                    Text(text = "Obs: ${medicion.observaciones}", style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(onClick = { onDelete(medicion) }) {
                Text("Borrar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}