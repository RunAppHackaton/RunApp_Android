package com.example.runapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/DenisDyrbalov/Run_App/1.0.0/run-sessions")
    fun getRunSession(): Call<RunSession>
    @POST("/DenisDyrbalov/Run_App/1.0.0/run-sessions")
    fun createRunSession(@Body requestBody: CreateRunRequestBody): Call<CreateRunResponseBody>
}