package com.example.nuevaapp.repository

import com.example.nuevaapp.model.Equipo
import com.example.nuevaapp.model.EquipoDao
import kotlinx.coroutines.flow.Flow

class EquipoRepository(private val equipoDao: EquipoDao) {

    // Flujo continuo de equipos directamente desde la base de datos SQLite.
    val todosLosEquipos: Flow<List<Equipo>> = equipoDao.obtenerTodosLosEquipos()

    suspend fun obtenerEquipoPorId(id: Int): Equipo? {
        return equipoDao.obtenerEquipoPorId(id)
    }

    suspend fun insertarEquipo(equipo: Equipo) {
        equipoDao.insertarEquipo(equipo)
    }

    suspend fun actualizarEquipo(equipo: Equipo) {
        equipoDao.actualizarEquipo(equipo)
    }

    suspend fun eliminarEquipo(equipo: Equipo) {
        equipoDao.eliminarEquipo(equipo)
    }
}