package com.estudiante.strennus_proyweb.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.estudiante.strennus_proyweb.entities.Sesion

@Dao
interface SesionDao {
    @Insert
    suspend fun insert(sesion : Sesion) : Long

    @Update
    suspend fun update(sesion : Sesion)

    @Delete
    suspend fun delete(sesion : Sesion)

    @Query("SELECT * FROM sesiones WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun allsesionsbyID(usuarioId : Int) : LiveData<List<Sesion>>

    @Query("SELECT * FROM sesiones WHERE usuarioId = :usuarioId")
    fun sesionbyID(usuarioId : Int) : LiveData<Sesion>
}