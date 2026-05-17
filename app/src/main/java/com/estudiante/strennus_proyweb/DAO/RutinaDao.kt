package com.estudiante.strennus_proyweb.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.estudiante.strennus_proyweb.entities.Rutina

@Dao
interface RutinaDao {
    @Insert
    suspend fun insertar(rutina: Rutina)

    @Update
    suspend fun actualizar(rutina: Rutina)

    @Delete
    suspend fun eliminar(rutina: Rutina)

    @Query("SELECT * FROM rutinas WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    fun obtenerPorUsuario(usuarioId: Int): LiveData<List<Rutina>>

    @Query("SELECT * FROM rutinas WHERE id = :id")
    fun obtenerPorId(id: Int): LiveData<Rutina>
}