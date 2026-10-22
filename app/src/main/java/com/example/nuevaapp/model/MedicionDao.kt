package com.example.nuevaapp.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicionDao {

    // QUERY: Obtiene todas las mediciones ordenadas de la más reciente a la más antigua
    @Query("SELECT * FROM tabla_mediciones ORDER BY fecha_registro DESC")
    fun obtenerTodasLasMediciones(): Flow<List<Medicion>>

    // QUERY: Filtra las mediciones de un equipo en específico por su ID
    @Query("SELECT * FROM tabla_mediciones WHERE equipo_id = :equipoId ORDER BY fecha_registro DESC")
    fun obtenerMedicionesPorEquipo(equipoId: Int): Flow<List<Medicion>>

    // Operación para guardar/insertar una medición
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMedicion(medicion: Medicion)

    // Operación para eliminar una medición
    @Delete
    suspend fun eliminarMedicion(medicion: Medicion)
}