package com.example.nuevaapp.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

// Se registran las entidades incluyendo Usuario
@Database(entities = [Equipo::class, Medicion::class, Usuario::class], version = 7, exportSchema = false)
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
                        insertDefaultInitialData(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        insertDefaultInitialData(db)
                    }

                    private fun insertDefaultInitialData(db: SupportSQLiteDatabase) {
                        // 1. Usuarios por defecto (IDs 1 al 6)
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Administrador Induztek', 'admin@induztek.cl', '123456', 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'admin@induztek.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Administrador Duoc', 'admin@duocuc.cl', '123456', 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'admin@duocuc.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Técnico Induztek', 'tecnico@induztek.cl', '123456', 'TECNICO' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'tecnico@induztek.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Técnico Duoc', 'tec@duocuc.cl', '123456', 'TECNICO' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'tec@duocuc.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Cliente Induztek', 'cliente@induztek.cl', '123456', 'CLIENTE' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'cliente@induztek.cl')")
                        db.execSQL("INSERT INTO tabla_usuarios (nombre_usuario, correo, contrasena, perfil) SELECT 'Cliente Duoc', 'cliente@duocuc.cl', '123456', 'CLIENTE' WHERE NOT EXISTS (SELECT 1 FROM tabla_usuarios WHERE correo = 'cliente@duocuc.cl')")

                        // 2. Equipos de Prueba:
                        // Cliente ID 5 (Cliente Induztek) con 2 equipos
                        db.execSQL("INSERT INTO tabla_equipos (id, nombre_equipo, codigo_equipo, ubicacion_equipo, estado_equipo, cliente_id, cliente_nombre) SELECT 1, 'Transformador Principal', 'TR-501', 'Subestación Planta A', 'Operativo', 5, 'Cliente Induztek' WHERE NOT EXISTS (SELECT 1 FROM tabla_equipos WHERE id = 1)")
                        db.execSQL("INSERT INTO tabla_equipos (id, nombre_equipo, codigo_equipo, ubicacion_equipo, estado_equipo, cliente_id, cliente_nombre) SELECT 2, 'Generador de Respuesto', 'GEN-502', 'Sala de Máquinas', 'Operativo', 5, 'Cliente Induztek' WHERE NOT EXISTS (SELECT 1 FROM tabla_equipos WHERE id = 2)")

                        // Cliente ID 6 (Cliente Duoc) con 1 equipo
                        db.execSQL("INSERT INTO tabla_equipos (id, nombre_equipo, codigo_equipo, ubicacion_equipo, estado_equipo, cliente_id, cliente_nombre) SELECT 3, 'Tablero Eléctrico General', 'TG-601', 'Edificio Central', 'Operativo', 6, 'Cliente Duoc' WHERE NOT EXISTS (SELECT 1 FROM tabla_equipos WHERE id = 3)")

                        // 3. Mediciones de Prueba:
                        // Realizadas únicamente por el Técnico ID 4 (Técnico Duoc) al Equipo ID 1 del Cliente ID 5
                        db.execSQL("INSERT INTO tabla_mediciones (id, equipo_id, valor_medido, unidad_medida, fecha_registro, observaciones, foto_uri, usuario_id, tecnico_nombre) SELECT 1, 1, 650.0, 'Watts', '10/11/2026 10:15 hrs.', 'Medición inicial de potencia de arranque', '', 4, 'Técnico Duoc' WHERE NOT EXISTS (SELECT 1 FROM tabla_mediciones WHERE id = 1)")
                        db.execSQL("INSERT INTO tabla_mediciones (id, equipo_id, valor_medido, unidad_medida, fecha_registro, observaciones, foto_uri, usuario_id, tecnico_nombre) SELECT 2, 1, 666.0, 'Watts', '17/11/2026 14:35 hrs.', 'Inspección rutinaria todo OK', '', 4, 'Técnico Duoc' WHERE NOT EXISTS (SELECT 1 FROM tabla_mediciones WHERE id = 2)")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
