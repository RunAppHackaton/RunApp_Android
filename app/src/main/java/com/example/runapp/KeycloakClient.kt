package com.example.runapp

import okhttp3.OkHttpClient
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class KeycloakClient(baseUrl: String) {

    private val keycloakService: ApiService

    init {
        val httpClient = OkHttpClient.Builder()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()

        keycloakService = retrofit.create(ApiService::class.java)
    }

    fun getAccessToken(
        grantType: String = "password",
        clientId: String,
        username: String,
        password: String,
        callback: Callback<KeycloakToken>
    ) {
        keycloakService.getAccessToken(grantType, clientId, username, password)
            .enqueue(callback)
    }
    fun createUser(
        adminToken: String,
        createUserRequest: CreateUserRequest,
        callback: Callback<Void>
    ) {
        val authorizationValue = "Bearer $adminToken"
        keycloakService.createUser(authorizationValue, createUserRequest).enqueue(callback)
    }
}
