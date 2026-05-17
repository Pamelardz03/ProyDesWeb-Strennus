package com.estudiante.strennus_proyweb.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "rutinas",
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Rutina(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val usuarioId : Int,
    val nombre : String,
    val descripcion : String? = null,
    val ejerciciosJson : String,
    val fechaCreacion : Long
)
