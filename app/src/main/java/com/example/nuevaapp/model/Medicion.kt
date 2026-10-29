package com.example.nuevaapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_mediciones")
data class Medicion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "equipo_id")
    val equipoId: Int,

    @ColumnInfo(name = "valor_medido")
    val valor: Double,

    @ColumnInfo(name = "unidad_medida")
    val unidad: String, // Ejemplo: "V", "A", "°C", "PSI"

    @ColumnInfo(name = "fecha_registro")
    val fecha: String,

    @ColumnInfo(name = "observaciones")
    val observaciones: String = ""
)