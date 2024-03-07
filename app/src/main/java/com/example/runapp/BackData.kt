package com.example.runapp

data class RunSession(
    val id: Int,
    val date: String,
    val distance: Double,
    val duration_time: DurationTime,
    val caloriesBurned: Int,
    val notes: String,
    val photosUrl: String,
    val route: Route,
    val shoesId: Int,
    val userId: Int,
    val training: Training,
    val weatherConditions: String,
    val pace: Pace
)

data class CreateRunRequestBody(
    val distance_km: Double,
    val duration_time: DurationTime,
    val caloriesBurned: Int,
    val notes: String,
    val routeId: Int,
    val shoesId: Int,
    val userId: Int,
    val route_points: List<RoutePointPost>,
    val training_id_from_run_plan: Int,
    val weatherConditions: String,
    val pace: PacePost
)

data class CreateRunResponseBody(
    val id: Int,
    val date: String,
    val distance: Double,
    val duration_time: DurationTime,
    val pace: Pace,
    val caloriesBurned: Int,
    val weatherConditions: String,
    val notes: String,
    val photosUrl: String?,
    val route: Route,
    val training: Training,
    val userId: Int,
    val shoesId: Int
)

data class DurationTime(
    val seconds: Int,
    val zero: Boolean,
    val nano: Int,
    val negative: Boolean,
    val units: List<Unit>
)

data class Pace(
    val seconds: Int,
    val zero: Boolean,
    val nano: Int,
    val negative: Boolean,
    val units: List<Unit>
)

data class PacePost(
    val seconds: Int,
    val zero: Boolean,
    val nano: Int,
    val negative: Boolean
)

data class Unit(
    val durationEstimated: Boolean,
    val timeBased: Boolean,
    val dateBased: Boolean
)

data class Route(
    val id: Int,
    val routePoints: List<RoutePoint>
)

data class RoutePoint(
    val id: Int,
    val latitude: Double,
    val longitude: Double
)

data class RoutePointPost(
    val latitude: Double,
    val longitude: Double
)

data class Training(
    val id: Int,
    val kilometers: Double,
    val warmUp: Double,
    val hitch: Double,
    val stage: Stage,
    val runType: RunType,
    val intervalModelList: List<IntervalModel>
)

data class Stage(
    val id: Int,
    val stageEnum: String,
    val name: String,
    val description: String
)

data class RunType(
    val id: Int,
    val typeName: String,
    val description: String,
    val runtypeImageUrl: String
)

data class IntervalModel(
    val id: Int,
    val runMetres: Int,
    val runPace: String,
    val intervalRestType: String,
    val restMetres: Int,
    val restPace: String,
    val timeBreak: String,
    val timeRunIntervals: String
)
