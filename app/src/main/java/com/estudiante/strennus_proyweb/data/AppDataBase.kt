package com.estudiante.strennus_proyweb.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.estudiante.strennus_proyweb.DAO.DetalleSesionDao
import com.estudiante.strennus_proyweb.DAO.RutinaDao
import com.estudiante.strennus_proyweb.DAO.SesionDao
import com.estudiante.strennus_proyweb.DAO.UsuarioDao
import com.estudiante.strennus_proyweb.entities.DetalleSesion
import com.estudiante.strennus_proyweb.entities.Rutina
import com.estudiante.strennus_proyweb.entities.Sesion
import com.estudiante.strennus_proyweb.entities.Usuario

@Database(
    entities = [Usuario::class, Sesion::class, DetalleSesion::class, Rutina::class],
    version = 1,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun rutinaDao(): RutinaDao
    abstract fun sesionDao(): SesionDao
    abstract fun detalleDao(): DetalleSesionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "strennus_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
