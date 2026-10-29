package com.example.nuevaapp.repository

import com.example.nuevaapp.model.Medicion
import com.example.nuevaapp.model.MedicionDao
import kotlinx.coroutines.flow.Flow

class MedicionRepository(private val medicionDao: MedicionDao) {

    val todasLasMediciones: Flow<List<Medicion>> = medicionDao.obtenerTodasLasMediciones()

    fun obtenerMedicionesPorEquipo(equipoId: Int): Flow<List<Medicion>> {
        return medicionDao.obtenerMedicionesPorEquipo(equipoId)
    }

    suspend fun insertarMedicion(medicion: Medicion) {
        medicionDao.insertarMedicion(medicion)
    }

    // NUEVO: Función para actualizar una medición existente
    suspend fun actualizarMedicion(medicion: Medicion) {
        medicionDao.actualizarMedicion(medicion)
    }

    // NUEVO: Función para obtener una medición por su ID
    suspend fun obtenerMedicionPorId(id: Int): Medicion? {
        return medicionDao.obtenerMedicionPorId(id)
    }

    suspend fun eliminarMedicion(medicion: Medicion) {
        medicionDao.eliminarMedicion(medicion)
    }

    fun buscarMediciones(busqueda: String): Flow<List<Medicion>> {
        return medicionDao.buscarMediciones(busqueda)
    }
}