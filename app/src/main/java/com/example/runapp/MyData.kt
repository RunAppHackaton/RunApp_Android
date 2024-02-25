package com.example.runapp

data class MyData(
    val caloriesBurned: Int,
    val date: String,
    val distance: Int,
    val duration_time: DurationTime,
    val id: Int,
    val notes: String,
    val pace: Pace,
    val photosUrl: String,
    val route: Route,
    val shoesId: Int,
    val training: Training,
    val userId: Int,
    val weatherConditions: String
)