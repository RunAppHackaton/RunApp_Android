package com.example.runapp

import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("/apis/DenisDyrbalov/Run_App/1.0.0#/Run%20Session%20Management/getRunSessionById")
    fun getData(): Call<List<MyData>>
}