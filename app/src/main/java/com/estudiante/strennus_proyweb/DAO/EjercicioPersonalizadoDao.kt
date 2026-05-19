package com.estudiante.strennus_proyweb.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.estudiante.strennus_proyweb.entities.EjercicioPersonalizado

@Dao
interface EjercicioPersonalizadoDao {

    @Insert
    suspend fun insertar(ejercicio: EjercicioPersonalizado)

    @Update
    suspend fun actualizar(ejercicio: EjercicioPersonalizado)

    @Delete
    suspend fun eliminar(ejercicio: EjercicioPersonalizado)

    // Obtener todos los ejercicios del usuario ordenados por fecha
    @Query("SELECT * FROM ejercicios_personalizados WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    fun obtenerPorUsuario(usuarioId: Int): LiveData<List<EjercicioPersonalizado>>

    // Buscar por nombre
    @Query("SELECT * FROM ejercicios_personalizados WHERE usuarioId = :usuarioId AND nombre LIKE '%' || :query || '%' ORDER BY fechaCreacion DESC")
    fun buscarPorNombre(usuarioId: Int, query: String): LiveData<List<EjercicioPersonalizado>>

    // Obtener todos sin LiveData (para usar en coroutines)
    @Query("SELECT * FROM ejercicios_personalizados WHERE usuarioId = :usuarioId ORDER BY fechaCreacion DESC")
    suspend fun obtenerTodos(usuarioId: Int): List<EjercicioPersonalizado>
}