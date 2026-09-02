package com.example.gesturgaitai.model

data class ScoreResponse(
    val score: Int?,
    val deviations: List<Deviation>?,
    val explanation: String?,
    val recommendation: String?,
    val message: String?
)

data class Deviation(
    val feature: String,
    val deltaPercent: Double,
    val direction: String
)

data class AuthResponse(
    val token: String?,
    val user: User?,
    val message: String?
)

data class User(
    val _id: String,
    val email: String,
    val patientId: String
)

data class SummaryItem(
    val label: String,
    val avgScore: Int
)
