package com.estudiante.strennus_proyweb

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface APIService {

    // GET /v1/exercises?name=pushup
    // API Ninjas requiere header X-Api-Key en cada petición
    @GET("v1/exercises")
    suspend fun getExercises(
        @Header("X-Api-Key") apiKey: String,
        @Query("name")       name: String = "",
        @Query("type")       type: String = "",
        @Query("muscle")     muscle: String = "",
        @Query("difficulty") difficulty: String = ""
    ): Response<List<Exercise>>

}