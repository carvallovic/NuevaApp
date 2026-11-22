package com.example.nuevaapp.model

import com.example.nuevaapp.viewmodel.UserRole

// Representa el estado de la UI del Historial
data class HistorialUiState(
    val mediciones: List<Medicion> = emptyList(), // Lista filtrada de mediciones
    val equipos: List<Equipo> = emptyList(),     // Lista de equipos para mostrar nombres
    val textoBusqueda: String = "",               // Texto ingresado en el buscador
    val userRole: UserRole? = null,              // Rol del usuario actual
    val isLoading: Boolean = false,              // Estado de carga
    val error: String? = null                     // Mensaje en caso de error
)
