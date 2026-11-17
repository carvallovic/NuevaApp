package com.example.nuevaapp.repository

import com.example.nuevaapp.model.Usuario
import com.example.nuevaapp.model.UsuarioDao

class AuthRepository(private val usuarioDao: UsuarioDao) {

    suspend fun autenticar(correo: String, contrasena: String): Usuario? {
        return usuarioDao.obtenerUsuarioPorCredenciales(correo, contrasena)
    }

    suspend fun registrarUsuario(usuario: Usuario) {
        usuarioDao.insertarUsuario(usuario)
    }
}
