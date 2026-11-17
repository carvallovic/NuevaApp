package com.example.nuevaapp.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUserRole = MutableStateFlow<UserRole?>(null)
    val currentUserRole: StateFlow<UserRole?> = _currentUserRole.asStateFlow()

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

            // 2. Simulamos el tiempo de consulta a la BD o API (1.0 segundo)
            delay(1000)

            // 3. Validación de perfiles de prueba
            val role = when (email.trim().lowercase()) {
                "admin@induztek.cl", "admin@duocuc.cl" -> UserRole.ADMIN
                "tecnico@induztek.cl", "tec@duocuc.cl" -> UserRole.TECNICO
                "cliente@induztek.cl", "cliente@duocuc.cl" -> UserRole.CLIENTE
                "usuario@duocuc.cl" -> UserRole.ADMIN // Cuenta por defecto anterior
                else -> null
            }

            if (role != null && clave == "123456") {
                _currentUserRole.value = role
                _authState.value = AuthState.Success(role)
            } else {
                _authState.value = AuthState.Error("Credenciales incorrectas.\nPruebe con:\n• admin@induztek.cl / 123456\n• tecnico@induztek.cl / 123456\n• cliente@induztek.cl / 123456")
            }
        }
    }

    fun cerrarSesion() {
        _currentUserRole.value = null
        _authState.value = AuthState.Idle
    }

    // Limpia el estado después de mostrar un error
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
