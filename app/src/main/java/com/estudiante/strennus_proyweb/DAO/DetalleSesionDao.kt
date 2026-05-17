package com.estudiante.strennus_proyweb.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.estudiante.strennus_proyweb.entities.DetalleSesion

@Dao
interface DetalleSesionDao {
    @Insert
    suspend fun insert(detalle: DetalleSesion)

    @Update
    suspend fun update(detalle: DetalleSesion)

    @Delete
    suspend fun delete(detalle: DetalleSesion)

    @Query("SELECT * FROM detalle_sesion WHERE sesionId = :sesionId")
    fun obtenerPorSesion(sesionId: Int): LiveData<List<DetalleSesion>>

    @Query("DELETE FROM detalle_sesion WHERE sesionId = :sesionId")
    suspend fun eliminarPorSesion(sesionId: Int)
}