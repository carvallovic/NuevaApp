package com.example.nuevaapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity le indica a Room que esta clase representa una tabla en la base de datos SQLite.
@Entity(tableName = "tabla_equipos")
data class Equipo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nombre_equipo")
    val nombre: String,

    @ColumnInfo(name = "codigo_equipo")
    val codigo: String,       // El código interno, TAG o número de serie

    @ColumnInfo(name = "ubicacion_equipo")
    val ubicacion: String,    // Sector o planta donde se encuentra

    @ColumnInfo(name = "estado_equipo")
    val estado: String = "Operativo", // Estado: Operativo, Falla, Mantenimiento

    @ColumnInfo(name = "cliente_id")
    val clienteId: Int = 0,

    @ColumnInfo(name = "cliente_nombre")
    val clienteNombre: String = ""
)
