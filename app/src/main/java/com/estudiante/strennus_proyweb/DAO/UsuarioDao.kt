package com.estudiante.strennus_proyweb.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.estudiante.strennus_proyweb.entities.Usuario

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insert(user: Usuario) : Long

    @Update
    suspend fun updateUser(user: Usuario)

    @Delete
    suspend fun deleteUser(user: Usuario)

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun getbyID(id: Int): LiveData<Usuario>
}