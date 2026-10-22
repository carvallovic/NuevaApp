package com.example.nuevaapp.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Diferentes estados posibles de la pantalla de Login
sealed class AuthState {
    object Idle : AuthState() // Esperando acción del usuario
    object Loading : AuthState() // Cargando (validando en el servidor)
    object Success : AuthState() // Login exitoso
    data class Error(val message: String) : AuthState() // Error de validación
}

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

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

            // 2. Simulamos el tiempo de consulta a una base de datos o API (1.5 segundos)
            delay(1500)

            // 3. Validación de credenciales de prueba (Luego lo cambiaremos por Room o Firebase)
            if (email == "usuario@duocuc.cl" && clave == "123456") {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Correo o contraseña incorrectos")
            }
        }
    }

    // Limpia el estado después de mostrar un error
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}