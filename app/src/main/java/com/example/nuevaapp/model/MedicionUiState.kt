package com.example.nuevaapp.model

// Maneja el estado completo de la UI para el módulo de Mediciones.
data class MedicionUiState(
    val mediciones: List<Medicion> = emptyList(),
    val isLoading: Boolean = false,
    val errorGeneral: String? = null,
    val erroresFormulario: MedicionErrores = MedicionErrores()
)