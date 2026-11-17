package com.example.nuevaapp.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

// Se registran las entidades incluyendo Usuario
@Database(entities = [Equipo::class, Medicion::class, Usuario::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun equipoDao(): EquipoDao
    abstract fun medicionDao(): MedicionDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nueva_app_database.db"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        insertDefaultUsers(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        insertDefaultUsers(db)
                    }

                    private fun insertDefaultUsers(db: SupportSQLiteDatabase) {
                        db.execSQL("INSERT OR IGNORE INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) VALUES ('Administrador Induztek', 'admin@induztek.cl', '123456', 'ADMIN')")
                        db.execSQL("INSERT OR IGNORE INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) VALUES ('Administrador Duoc', 'admin@duocuc.cl', '123456', 'ADMIN')")
                        db.execSQL("INSERT OR IGNORE INTO tabla_usuarios (nombre_website, correo, contrasena, perfil) VALUES ('Técnico Induztek', 'tecnico@induztek.cl', '123456', 'TECNICO')".replace("nombre_website", "nombre_usuario"))
                        db.execSQL("INSERT OR IGNORE INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) VALUES ('Técnico Duoc', 'tec@duocuc.cl', '123456', 'TECNICO')")
                        db.execSQL("INSERT OR IGNORE INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) VALUES ('Cliente Induztek', 'cliente@induztek.cl', '123456', 'CLIENTE')")
                        db.execSQL("INSERT OR IGNORE INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) VALUES ('Cliente Duoc', 'cliente@duocuc.cl', '123456', 'CLIENTE')")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
