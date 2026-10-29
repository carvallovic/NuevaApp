package com.example.nuevaapp.model

// Representa el estado reactivo de la pantalla de Equipos.
data class EquipoUiState(
    val equipos: List<Equipo> = emptyList(), // Lista cargada desde SQLite
    val isLoading: Boolean = false,          // Indicador de carga
    val error: String? = null                // Mensaje de error si falla una operación
)