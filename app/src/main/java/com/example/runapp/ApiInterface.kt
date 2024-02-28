package com.example.runapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/run-sessions")
    fun getRunSession(): Call<List<RunSession>>
    @POST("/run-sessions")
    fun createRunSession(@Body requestBody: CreateRunRequestBody): Call<CreateRunResponseBody>
}