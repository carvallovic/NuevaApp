package com.example.nuevaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuevaapp.model.Usuario
import com.example.nuevaapp.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UsuarioUiState(
    val usuarios: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class UsuarioViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UsuarioUiState(isLoading = true))
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        viewModelScope.launch {
            authRepository.todosLosUsuarios.collect { lista ->
                _uiState.update {
                    it.copy(
                        usuarios = lista,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun guardarUsuario(id: Int = 0, nombre: String, correo: String, contrasena: String, perfil: String) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            _uiState.update { it.copy(error = "Todos los campos son obligatorios.") }
            return
        }

        viewModelScope.launch {
            val usuario = Usuario(
                id = id,
                nombre = nombre.trim(),
                correo = correo.trim().lowercase(),
                contrasena = contrasena,
                perfil = perfil
            )
            authRepository.registrarUsuario(usuario)
            _uiState.update { it.copy(error = null) }
        }
    }

    fun eliminarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            authRepository.eliminarUsuario(usuario)
        }
    }
}
