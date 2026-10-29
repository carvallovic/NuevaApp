package com.example.nuevaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuevaapp.model.HistorialUiState
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.repository.MedicionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistorialViewModel(private val repository: MedicionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState(isLoading = true))
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    // Variable reactiva para el filtro de texto
    private val _textoBusqueda = MutableStateFlow("")

    init {
        observarHistorial()
    }

    // Escucha los cambios del texto de búsqueda y reconsulta SQLite dinámicamente
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarHistorial() {
        viewModelScope.launch {
            _textoBusqueda
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        repository.todasLasMediciones
                    } else {
                        repository.buscarMediciones(query)
                    }
                }
                .collect { listaFiltrada ->
                    _uiState.update {
                        it.copy(
                            mediciones = listaFiltrada,
                            isLoading = false
                        )
                    }
                }
        }
    }

    // Actualiza el texto de búsqueda ingresado por el usuario
    fun onBusquedaCambiada(nuevoTexto: String) {
        _textoBusqueda.value = nuevoTexto
        _uiState.update { it.copy(textoBusqueda = nuevoTexto) }
    }

    // Elimina un registro del historial en la base de datos
    fun borrarMedicion(medicion: Medicion) {
        viewModelScope.launch {
            repository.eliminarMedicion(medicion)
        }
    }
}