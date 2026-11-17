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

    // QUERY: Obtiene todas las mediciones ordenadas por ID descendente (más recientes primero)
    @Query("SELECT * FROM tabla_mediciones ORDER BY id DESC")
    fun obtenerTodasLasMediciones(): Flow<List<Medicion>>

    // QUERY DE BÚSQUEDA GENERAL CON JOIN DE EQUIPOS (ADMIN)
    @Query("""
        SELECT DISTINCT m.* FROM tabla_mediciones m 
        LEFT JOIN tabla_equipos e ON m.equipo_id = e.id 
        WHERE m.observaciones LIKE '%' || :busqueda || '%' 
           OR m.unidad_medida LIKE '%' || :busqueda || '%' 
           OR e.nombre_equipo LIKE '%' || :busqueda || '%' 
           OR e.codigo_equipo LIKE '%' || :busqueda || '%' 
        ORDER BY m.id DESC
    """)
    fun buscarMediciones(busqueda: String): Flow<List<Medicion>>

    // QUERY: Obtiene mediciones por usuario ID ordenadas por ID descendente
    @Query("SELECT * FROM tabla_mediciones WHERE usuario_id = :usuarioId ORDER BY id DESC")
    fun obtenerMedicionesPorUsuario(usuarioId: Int): Flow<List<Medicion>>

    // QUERY DE BÚSQUEDA POR USUARIO CON JOIN DE EQUIPOS (TECNICO / CLIENTE)
    @Query("""
        SELECT DISTINCT m.* FROM tabla_mediciones m 
        LEFT JOIN tabla_equipos e ON m.equipo_id = e.id 
        WHERE m.usuario_id = :usuarioId 
          AND (m.observaciones LIKE '%' || :busqueda || '%' 
               OR m.unidad_medida LIKE '%' || :busqueda || '%' 
               OR e.nombre_equipo LIKE '%' || :busqueda || '%' 
               OR e.codigo_equipo LIKE '%' || :busqueda || '%') 
        ORDER BY m.id DESC
    """)
    fun buscarMedicionesPorUsuario(usuarioId: Int, busqueda: String): Flow<List<Medicion>>

    // QUERY: Filtra las mediciones de un equipo en específico por su ID
    @Query("SELECT * FROM tabla_mediciones WHERE equipo_id = :equipoId ORDER BY id DESC")
    fun obtenerMedicionesPorEquipo(equipoId: Int): Flow<List<Medicion>>

    // QUERY SINCRÓNICA: Obtiene lista de mediciones por equipo para cálculos e histórico
    @Query("SELECT * FROM tabla_mediciones WHERE equipo_id = :equipoId ORDER BY id DESC")
    suspend fun obtenerMedicionesPorEquipoSync(equipoId: Int): List<Medicion>

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
