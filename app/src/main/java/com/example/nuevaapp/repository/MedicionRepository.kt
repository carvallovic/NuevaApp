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

    suspend fun eliminarMedicion(medicion: Medicion) {
        medicionDao.eliminarMedicion(medicion)
    }
}