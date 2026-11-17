package com.example.nuevaapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabla_usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "nombre_usuario")
    val nombre: String,

    @ColumnInfo(name = "correo")
    val correo: String,

    @ColumnInfo(name = "contrasena")
    val contrasena: String,

    @ColumnInfo(name = "perfil")
    val perfil: String // "ADMIN", "TECNICO", "CLIENTE"
)
