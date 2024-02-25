package com.example.runapp

data class Pace(
    val nano: Int,
    val negative: Boolean,
    val seconds: Int,
    val units: List<Unit>,
    val zero: Boolean
)