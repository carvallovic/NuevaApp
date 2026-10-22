package com.example.nuevaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.model.MedicionErrores
import com.example.nuevaapp.model.MedicionUiState
import com.example.nuevaapp.repository.MedicionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicionViewModel(private val repository: MedicionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicionUiState(isLoading = true))
    val uiState: StateFlow<MedicionUiState> = _uiState.asStateFlow()

    init {
        cargarMediciones()
    }

    private fun cargarMediciones() {
        viewModelScope.launch {
            repository.todasLasMediciones.collect { lista ->
                _uiState.update {
                    it.copy(mediciones = lista, isLoading = false)
                }
            }
        }
    }

    // Valida los datos introducidos por el usuario e inserta en la BD si son válidos
    fun guardarMedicion(equipoIdStr: String, valorStr: String, unidad: String, observaciones: String) {
        val equipoId = equipoIdStr.toIntOrNull()
        val valor = valorStr.toDoubleOrNull()

        var errores = MedicionErrores()
        var hayError = false

        if (equipoId == null) {
            errores = errores.copy(equipoError = "ID de equipo no válido")
            hayError = true
        }
        if (valor == null) {
            errores = errores.copy(valorError = "Ingrese un valor numérico")
            hayError = true
        }
        if (unidad.isBlank()) {
            errores = errores.copy(unidadError = "Especifique la unidad (Ej: V, °C)")
            hayError = true
        }

        if (hayError) {
            _uiState.update { it.copy(erroresFormulario = errores) }
            return
        }

        viewModelScope.launch {
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaActual = formatoFecha.format(Date())

            val nuevaMedicion = Medicion(
                equipoId = equipoId!!,
                valor = valor!!,
                unidad = unidad,
                fecha = fechaActual,
                observaciones = observaciones
            )

            repository.insertarMedicion(nuevaMedicion)
            _uiState.update { it.copy(erroresFormulario = MedicionErrores()) }
        }
    }

    fun borrarMedicion(medicion: Medicion) {
        viewModelScope.launch {
            repository.eliminarMedicion(medicion)
        }
    }
}