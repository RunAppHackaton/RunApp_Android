package com.example.runapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


interface ApiService {
    @GET("/run-sessions")
    fun getRunSession(): Call<List<RunSession>>
    @POST("/run-sessions")
    fun createRunSession(@Body requestBody: CreateRunRequestBody): Call<CreateRunResponseBody>
    @POST("/teams")
    fun createGuild(@Body requestBody: Team): Call<TeamResponse>
    @POST("/userteams")
    fun addUserToGuild(@Body requestBody: UsersInTeam)
    @FormUrlEncoded
    @POST(BuildConfig.ADMIN_TOKEN_ENDPOINT)
    fun getAccessToken(
        @Field("grant_type") grantType: String?,
        @Field("client_id") clientId: String?,
        @Field("username") username: String?,
        @Field("password") password: String?
    ): Call<KeycloakToken>
    @POST(BuildConfig.USER_ADD_KC_ENDPOINT)
    fun createUser(
        @Header("Authorization") authorization: String,
        @Body createUserRequest: CreateUserRequest
    ): Call<Void>
}