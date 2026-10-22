package com.example.nuevaapp.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Se registran ambas entidades en el parámetro 'entities'
@Database(entities = [Equipo::class, Medicion::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun equipoDao(): EquipoDao

    // Método abstracto para obtener el DAO de mediciones
    abstract fun medicionDao(): MedicionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nueva_app_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}