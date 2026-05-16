package com.estudiante.strennus_proyweb

import com.google.gson.annotations.SerializedName

// API Ninjas devuelve una lista directa de ejercicios
// https://api-ninjas.com/api/exercises
data class Exercise(
    @SerializedName("name")         val name: String,
    @SerializedName("type")         val type: String,
    @SerializedName("muscle")       val muscle: String,
    @SerializedName("equipment")    val equipment: String,
    @SerializedName("difficulty")   val difficulty: String,
    @SerializedName("instructions") val instructions: String
)