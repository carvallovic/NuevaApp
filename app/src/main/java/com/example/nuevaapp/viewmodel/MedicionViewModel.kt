package com.example.nuevaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.repository.MedicionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private val _currentUserId = MutableStateFlow(-1)
    private val _userRole = MutableStateFlow<UserRole?>(null)

    init {
        cargarMediciones()
    }

    fun setUserContext(userId: Int, role: UserRole?) {
        _currentUserId.value = userId
        _userRole.value = role
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun cargarMediciones() {
        viewModelScope.launch {
            combine(_currentUserId, _userRole) { userId, role ->
                Pair(userId, role)
            }
            .filter { (userId, role) -> role != null && (role == UserRole.ADMIN || userId > 0) }
            .flatMapLatest { (userId, role) ->
                when (role) {
                    UserRole.ADMIN -> repository.todasLasMediciones
                    UserRole.TECNICO -> repository.obtenerMedicionesPorTecnico(userId)
                    UserRole.CLIENTE -> repository.obtenerMedicionesPorCliente(userId)
                    null -> flowOf(emptyList())
                }
            }
            .collect { listaFiltrada ->
                _uiState.update {
                    it.copy(
                        mediciones = listaFiltrada,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun agregarMedicion(medicion: Medicion) {
        if (medicion.equipoId <= 0 || medicion.valor <= 0.0 || medicion.fotoUri.isBlank()) {
            _uiState.update { it.copy(error = "Debe seleccionar un equipo, ingresar un valor válido y adjuntar al menos una fotografía.") }
            return
        }

        _uiState.update { it.copy(error = null) }

        viewModelScope.launch {
            repository.insertarMedicion(medicion)
        }
    }

    fun editarMedicion(medicion: Medicion) {
        if (medicion.equipoId <= 0 || medicion.valor <= 0.0 || medicion.fotoUri.isBlank()) {
            _uiState.update { it.copy(error = "Debe seleccionar un equipo, ingresar un valor válido y adjuntar al menos una fotografía.") }
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
