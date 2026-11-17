package com.example.nuevaapp.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nuevaapp.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Perfiles de usuario requeridos
enum class UserRole {
    ADMIN,   // Puede crear equipos, mediciones y ver historial
    TECNICO, // Puede crear mediciones y ver historial
    CLIENTE  // Solo ver historial
}

// Diferentes estados posibles de la pantalla de Login
sealed class AuthState {
    object Idle : AuthState() // Esperando acción del usuario
    object Loading : AuthState() // Cargando (validando en el servidor)
    data class Success(val role: UserRole) : AuthState() // Login exitoso con su rol
    data class Error(val message: String) : AuthState() // Error de validación
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUserRole = MutableStateFlow<UserRole?>(null)
    val currentUserRole: StateFlow<UserRole?> = _currentUserRole.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int>(0)
    val currentUserId: StateFlow<Int> = _currentUserId.asStateFlow()

    private val _currentUserName = MutableStateFlow("")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    fun iniciarSesion(email: String, clave: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // 1. Validaciones locales
            if (email.isBlank() || clave.isBlank()) {
                _authState.value = AuthState.Error("Los campos no pueden estar vacíos")
                return@launch
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthState.Error("Ingresa un correo válido")
                return@launch
            }
            if (clave.length < 6) {
                _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
                return@launch
            }

            // 2. Simulamos el tiempo de consulta a la BD (0.8 segundos)
            delay(800)

            // 3. Consultar a la base de datos SQLite (tabla_usuarios)
            val usuario = authRepository.autenticar(email.trim().lowercase(), clave)

            if (usuario != null) {
                val role = when (usuario.perfil.uppercase()) {
                    "ADMIN" -> UserRole.ADMIN
                    "TECNICO" -> UserRole.TECNICO
                    else -> UserRole.CLIENTE
                }
                _currentUserRole.value = role
                _currentUserId.value = usuario.id
                _currentUserName.value = usuario.nombre
                _currentUserEmail.value = usuario.correo
                _authState.value = AuthState.Success(role)
            } else {
                _authState.value = AuthState.Error("Correo o contraseña incorrectos en la base de datos")
            }
        }
    }

    fun cerrarSesion() {
        _currentUserRole.value = null
        _currentUserId.value = 0
        _currentUserName.value = ""
        _currentUserEmail.value = ""
        _authState.value = AuthState.Idle
    }

    // Limpia el estado después de mostrar un error
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
