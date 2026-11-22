package com.example.nuevaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuevaapp.model.HistorialUiState
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.repository.EquipoRepository
import com.example.nuevaapp.repository.MedicionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistorialViewModel(
    private val medicionRepository: MedicionRepository,
    private val equipoRepository: EquipoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState(isLoading = true))
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val _textoBusqueda = MutableStateFlow("")
    private val _currentUserId = MutableStateFlow(-1)
    private val _userRole = MutableStateFlow<UserRole?>(null)

    init {
        observarEquipos()
        observarHistorial()
    }

    private fun observarEquipos() {
        viewModelScope.launch {
            equipoRepository.todosLosEquipos.collect { listaEquipos ->
                _uiState.update { it.copy(equipos = listaEquipos) }
            }
        }
    }

    fun setUserContext(userId: Int, role: UserRole?) {
        _currentUserId.value = userId
        _userRole.value = role
        _uiState.update { it.copy(userRole = role) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarHistorial() {
        viewModelScope.launch {
            combine(_textoBusqueda, _currentUserId, _userRole) { query, userId, role ->
                Triple(query, userId, role)
            }
            .filter { (_, userId, role) -> role != null && (role == UserRole.ADMIN || userId > 0) }
            .flatMapLatest { (query, userId, role) ->
                when (role) {
                    UserRole.ADMIN -> {
                        if (query.isBlank()) {
                            medicionRepository.todasLasMediciones
                        } else {
                            medicionRepository.buscarMediciones(query)
                        }
                    }
                    UserRole.TECNICO -> {
                        if (query.isBlank()) {
                            medicionRepository.obtenerMedicionesPorTecnico(userId)
                        } else {
                            medicionRepository.buscarMedicionesPorTecnico(userId, query)
                        }
                    }
                    UserRole.CLIENTE -> {
                        if (query.isBlank()) {
                            medicionRepository.obtenerMedicionesPorCliente(userId)
                        } else {
                            medicionRepository.buscarMedicionesPorCliente(userId, query)
                        }
                    }
                    null -> flowOf(emptyList())
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

    fun onBusquedaCambiada(nuevoTexto: String) {
        _textoBusqueda.value = nuevoTexto
        _uiState.update { it.copy(textoBusqueda = nuevoTexto) }
    }

    fun borrarMedicion(medicion: Medicion) {
        viewModelScope.launch {
            medicionRepository.eliminarMedicion(medicion)
        }
    }
}
