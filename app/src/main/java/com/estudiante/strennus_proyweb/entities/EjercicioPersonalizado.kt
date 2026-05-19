package com.estudiante.strennus_proyweb.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ejercicios_personalizados",
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class EjercicioPersonalizado(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val categoria: String? = null,
    val descripcion: String? = null,
    val fechaCreacion: Long = System.currentTimeMillis()
)