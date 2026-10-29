package com.example.nuevaapp.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

@Dao
interface MedicionDao {

    // QUERY: Obtiene todas las mediciones ordenadas de la más reciente a la más antigua
    @Query("SELECT * FROM tabla_mediciones ORDER BY fecha_registro DESC")
    fun obtenerTodasLasMediciones(): Flow<List<Medicion>>

    // QUERY DE BÚSQUEDA: Filtra registros por observaciones o unidad (Necesario para el Historial)
    @Query("SELECT * FROM tabla_mediciones WHERE observaciones LIKE '%' || :busqueda || '%' OR unidad_medida LIKE '%' || :busqueda || '%' ORDER BY fecha_registro DESC")
    fun buscarMediciones(busqueda: String): Flow<List<Medicion>>

    // QUERY: Filtra las mediciones de un equipo en específico por su ID
    @Query("SELECT * FROM tabla_mediciones WHERE equipo_id = :equipoId ORDER BY fecha_registro DESC")
    fun obtenerMedicionesPorEquipo(equipoId: Int): Flow<List<Medicion>>

    // NUEVO: Obtiene una medición específica por su ID para editarla
    @Query("SELECT * FROM tabla_mediciones WHERE id = :id")
    suspend fun obtenerMedicionPorId(id: Int): Medicion?

    // Operación para guardar/insertar una medición
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMedicion(medicion: Medicion)

    // NUEVO: Operación para actualizar una medición existente
    @Update
    suspend fun actualizarMedicion(medicion: Medicion)

    // Operación para eliminar una medición
    @Delete
    suspend fun eliminarMedicion(medicion: Medicion)
}