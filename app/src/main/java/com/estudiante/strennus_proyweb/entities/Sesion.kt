package com.estudiante.strennus_proyweb.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sesiones",
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Sesion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String = "Sin nombre",   // ← campo nuevo
    val fecha: Long,
    val horaInicio: Long,
    val horaFin: Long? = null,
    val duracionMinutos: Int? = null
)