package com.example.nuevaapp.repository

import com.example.nuevaapp.model.Usuario
import com.example.nuevaapp.model.UsuarioDao
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val usuarioDao: UsuarioDao) {

    val todosLosUsuarios: Flow<List<Usuario>> = usuarioDao.obtenerTodosLosUsuarios()

    suspend fun autenticar(correo: String, contrasena: String): Usuario? {
        return usuarioDao.obtenerUsuarioPorCredenciales(correo, contrasena)
    }

    suspend fun registrarUsuario(usuario: Usuario) {
        usuarioDao.insertarUsuario(usuario)
    }

    suspend fun actualizarUsuario(usuario: Usuario) {
        usuarioDao.actualizarUsuario(usuario)
    }

    suspend fun eliminarUsuario(usuario: Usuario) {
        usuarioDao.eliminarUsuario(usuario)
    }
}
