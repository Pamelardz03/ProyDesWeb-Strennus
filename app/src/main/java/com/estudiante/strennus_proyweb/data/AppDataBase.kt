package com.estudiante.strennus_proyweb.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.estudiante.strennus_proyweb.DAO.DetalleSesionDao
import com.estudiante.strennus_proyweb.DAO.EjercicioPersonalizadoDao
import com.estudiante.strennus_proyweb.DAO.RutinaDao
import com.estudiante.strennus_proyweb.DAO.SesionDao
import com.estudiante.strennus_proyweb.DAO.UsuarioDao
import com.estudiante.strennus_proyweb.entities.DetalleSesion
import com.estudiante.strennus_proyweb.entities.EjercicioPersonalizado
import com.estudiante.strennus_proyweb.entities.Rutina
import com.estudiante.strennus_proyweb.entities.Sesion
import com.estudiante.strennus_proyweb.entities.Usuario

@Database(
    entities = [Usuario::class, Sesion::class, DetalleSesion::class, Rutina::class, EjercicioPersonalizado::class],
    version = 3,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun rutinaDao(): RutinaDao
    abstract fun sesionDao(): SesionDao
    abstract fun detalleDao(): DetalleSesionDao
    abstract fun ejercicioPersonalizadoDao(): EjercicioPersonalizadoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "strennus_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
