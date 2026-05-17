package com.estudiante.strennus_proyweb.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "detalle_sesion",
    foreignKeys = [ForeignKey(
        entity = Sesion::class,
        parentColumns = ["id"],
        childColumns = ["sesionId"],
        onDelete = ForeignKey.CASCADE
    )
    ]
)
data class DetalleSesion(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val sesionId : Int,
    val nombreEjercicio : String,
    val series : Int,
    val repeticiones : Int,
    val peso : Float? = null,
    val notas : String? = null
)
