package com.estudiante.strennus_proyweb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val name : String,
    val correo : String,
    val password : String,
    val fechaRegistro : Long
)
