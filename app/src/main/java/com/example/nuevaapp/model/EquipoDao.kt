package com.example.nuevaapp.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// @Dao (Data Access Object) contiene todas las operaciones CRUD y las consultas SQL (Queries).
@Dao
interface EquipoDao {

    // QUERY: Obtiene todos los equipos ordenados alfabéticamente por nombre.
    // Retorna un Flow para emitir datos en tiempo real cuando la tabla SQLite sufra cambios.
    @Query("SELECT * FROM tabla_equipos ORDER BY nombre_equipo ASC")
    fun obtenerTodosLosEquipos(): Flow<List<Equipo>>

    // QUERY: Filtra y busca un equipo por su ID único.
    @Query("SELECT * FROM tabla_equipos WHERE id = :equipoId")
    suspend fun obtenerEquipoPorId(equipoId: Int): Equipo?

    // QUERY/INSERT: Inserta un nuevo equipo. Si el ID ya existe, lo reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEquipo(equipo: Equipo)

    // QUERY/UPDATE: Actualiza los datos del equipo existente en SQLite.
    @Update
    suspend fun actualizarEquipo(equipo: Equipo)

    // QUERY/DELETE: Elimina el registro especificado de la tabla.
    @Delete
    suspend fun eliminarEquipo(equipo: Equipo)
}