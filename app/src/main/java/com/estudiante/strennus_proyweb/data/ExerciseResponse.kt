package com.estudiante.strennus_proyweb.data

import com.google.gson.annotations.SerializedName

data class ExerciseResponse(
    @SerializedName("count")   val count: Int,
    @SerializedName("results") val exercises: List<Exercise>
)

data class Exercise(
    @SerializedName("id")           val id: Int,
    @SerializedName("translations") val translations: List<Translation>
) {
    val name: String
        get() = translations.firstOrNull { it.language == 2 }?.name
            ?: translations.firstOrNull()?.name
            ?: "Sin nombre"

    val description: String
        get() = translations.firstOrNull { it.language == 2 }?.description
            ?: translations.firstOrNull()?.description
            ?: ""
}

data class Translation(
    @SerializedName("language")    val language: Int,
    @SerializedName("name")        val name: String,
    @SerializedName("description") val description: String
)