package com.estudiante.strennus_proyweb.data

import com.estudiante.strennus_proyweb.data.ExerciseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {

    // Carga inicial sin filtro
    @GET("api/v2/exerciseinfo/")
    suspend fun getExercises(
        @Query("format") format: String = "json",
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ExerciseResponse>


}