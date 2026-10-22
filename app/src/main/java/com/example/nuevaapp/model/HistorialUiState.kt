package com.example.nuevaapp.model

// Representa el estado de la UI del Historial
data class HistorialUiState(
    val mediciones: List<Medicion> = emptyList(), // Lista filtrada de mediciones
    val textoBusqueda: String = "",               // Texto ingresado en el buscador
    val isLoading: Boolean = false,              // Estado de carga
    val error: String? = null                     // Mensaje en caso de error
)