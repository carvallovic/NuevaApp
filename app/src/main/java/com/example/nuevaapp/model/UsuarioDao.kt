package com.example.nuevaapp.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM tabla_usuarios WHERE correo = :correo AND contrasena = :contrasena")
    suspend fun obtenerUsuarioPorCredenciales(correo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM tabla_usuarios ORDER BY id ASC")
    fun obtenerTodosLosUsuarios(): Flow<List<Usuario>>

    @Query("SELECT * FROM tabla_usuarios WHERE UPPER(perfil) = 'CLIENTE' ORDER BY id ASC")
    fun obtenerUsuariosCliente(): Flow<List<Usuario>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario)

    @Update
    suspend fun actualizarUsuario(usuario: Usuario)

    @Delete
    suspend fun eliminarUsuario(usuario: Usuario)
}
