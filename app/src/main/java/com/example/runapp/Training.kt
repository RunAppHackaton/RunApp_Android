package com.example.runapp

data class Training(
    val hitch: Int,
    val id: Int,
    val intervalModelList: List<IntervalModel>,
    val kilometers: Int,
    val runType: RunType,
    val stage: Stage,
    val warmUp: Int
)