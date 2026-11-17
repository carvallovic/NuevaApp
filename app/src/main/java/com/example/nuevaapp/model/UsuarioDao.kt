package com.example.nuevaapp.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM tabla_usuarios WHERE correo = :correo AND contrasena = :contrasena")
    suspend fun obtenerUsuarioPorCredenciales(correo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM tabla_usuarios")
    suspend fun obtenerTodosLosUsuarios(): List<Usuario>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario)
}
