package com.example.nuevaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuevaapp.model.Equipo
import com.example.nuevaapp.model.EquipoUiState
import com.example.nuevaapp.repository.EquipoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EquipoViewModel(private val repository: EquipoRepository) : ViewModel() {

    // Estado privado mutable para evitar modificaciones externas directas.
    private val _uiState = MutableStateFlow(EquipoUiState(isLoading = true))
    // Estado público inmutable expuesto a la vista (Compose).
    val uiState: StateFlow<EquipoUiState> = _uiState.asStateFlow()

    init {
        cargarEquipos()
    }

    // Escucha continuamente los cambios en la tabla SQLite.
    private fun cargarEquipos() {
        viewModelScope.launch {
            repository.todosLosEquipos.collect { listaEquipos ->
                _uiState.update {
                    it.copy(
                        equipos = listaEquipos,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    // Inserta un nuevo registro invocando la consulta de SQLite en segundo plano.
    fun agregarEquipo(nombre: String, codigo: String, ubicacion: String, estado: String) {
        if (nombre.isBlank() || codigo.isBlank()) {
            _uiState.update { it.copy(error = "Nombre y código son obligatorios.") }
            return
        }

        viewModelScope.launch {
            val nuevo = Equipo(nombre = nombre, codigo = codigo, ubicacion = ubicacion, estado = estado)
            repository.insertarEquipo(nuevo)
        }
    }

    // Elimina un equipo en SQLite.
    fun borrarEquipo(equipo: Equipo) {
        viewModelScope.launch {
            repository.eliminarEquipo(equipo)
        }
    }
}