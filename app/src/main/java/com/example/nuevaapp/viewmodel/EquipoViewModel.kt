package com.example.nuevaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuevaapp.model.Equipo
import com.example.nuevaapp.model.EquipoUiState
import com.example.nuevaapp.repository.EquipoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EquipoViewModel(private val repository: EquipoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipoUiState(isLoading = true))
    val uiState: StateFlow<EquipoUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow(-1)
    private val _userRole = MutableStateFlow<UserRole?>(null)

    init {
        cargarClientes()
        cargarEquipos()
    }

    fun setUserContext(userId: Int, role: UserRole?) {
        _currentUserId.value = userId
        _userRole.value = role
    }

    private fun cargarClientes() {
        viewModelScope.launch {
            repository.clientes.collect { listaClientes ->
                _uiState.update { it.copy(clientes = listaClientes) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun cargarEquipos() {
        viewModelScope.launch {
            combine(repository.todosLosEquipos, _currentUserId, _userRole) { lista, userId, role ->
                Triple(lista, userId, role)
            }
            .map { (lista, userId, role) ->
                if (role == UserRole.CLIENTE) {
                    lista.filter { it.clienteId == userId }
                } else {
                    lista
                }
            }
            .collect { listaFiltrada ->
                _uiState.update {
                    it.copy(
                        equipos = listaFiltrada,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun agregarEquipo(nombre: String, codigo: String, ubicacion: String, estado: String, clienteId: Int, clienteNombre: String) {
        if (nombre.isBlank() || codigo.isBlank() || clienteId <= 0) {
            _uiState.update { it.copy(error = "Nombre, código y cliente son obligatorios.") }
            return
        }

        viewModelScope.launch {
            val nuevo = Equipo(
                nombre = nombre,
                codigo = codigo,
                ubicacion = ubicacion,
                estado = estado,
                clienteId = clienteId,
                clienteNombre = clienteNombre
            )
            repository.insertarEquipo(nuevo)
            _uiState.update { it.copy(error = null) }
        }
    }

    fun editarEquipo(equipo: Equipo) {
        if (equipo.nombre.isBlank() || equipo.codigo.isBlank() || equipo.clienteId <= 0) {
            _uiState.update { it.copy(error = "Nombre, código y cliente son obligatorios.") }
            return
        }

        viewModelScope.launch {
            repository.actualizarEquipo(equipo)
            _uiState.update { it.copy(error = null) }
        }
    }

    fun borrarEquipo(equipo: Equipo) {
        viewModelScope.launch {
            repository.eliminarEquipo(equipo)
        }
    }
}
