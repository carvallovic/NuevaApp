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
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Administrador Induztek', 'admin@induztek.cl', '123456', 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'admin@induztek.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Administrador Duoc', 'admin@duocuc.cl', '123456', 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'admin@duocuc.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Técnico Induztek', 'tecnico@induztek.cl', '123456', 'TECNICO' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'tecnico@induztek.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Técnico Duoc', 'tec@duocuc.cl', '123456', 'TECNICO' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'tec@duocuc.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Cliente Induztek', 'cliente@induztek.cl', '123456', 'CLIENTE' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'cliente@induztek.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Cliente Duoc', 'cliente@duocuc.cl', '123456', 'CLIENTE' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'cliente@duocuc.cl')")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
