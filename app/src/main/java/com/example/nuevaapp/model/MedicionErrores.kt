package com.example.nuevaapp.model

// Contiene los mensajes de error de validación para los campos del formulario de mediciones.
data class MedicionErrores(
    val valorError: String? = null,
    val unidadError: String? = null,
    val equipoError: String? = null
)