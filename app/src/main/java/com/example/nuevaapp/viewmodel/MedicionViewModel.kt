package com.example.nuevaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.repository.MedicionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MedicionUiState(
    val mediciones: List<Medicion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

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
                    it.copy(
                        mediciones = lista,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun agregarMedicion(medicion: Medicion) {
        if (medicion.equipoId <= 0 || medicion.valor <= 0.0) {
            _uiState.update { it.copy(error = "Ingrese un ID de equipo y valor válidos.") }
            return
        }

        _uiState.update { it.copy(error = null) }

        viewModelScope.launch {
            repository.insertarMedicion(medicion)
        }
    }

    fun editarMedicion(medicion: Medicion) {
        if (medicion.equipoId <= 0 || medicion.valor <= 0.0) {
            _uiState.update { it.copy(error = "Ingrese un ID de equipo y valor válidos.") }
            return
        }

        _uiState.update { it.copy(error = null) }

        viewModelScope.launch {
            repository.actualizarMedicion(medicion)
        }
    }

    fun borrarMedicion(medicion: Medicion) {
        viewModelScope.launch {
            repository.eliminarMedicion(medicion)
        }
    }
}