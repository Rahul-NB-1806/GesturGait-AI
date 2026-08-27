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

data class HistoryResponse(
    val history: List<ScoreResponse>?
)

data class SummaryResponse(
    val period: String,
    val data: List<SummaryItem>
)

data class SummaryItem(
    val label: String,
    val avgScore: Int,
    val minScore: Int,
    val maxScore: Int,
    val scoreCount: Int
)

data class BaselineResponse(
    val establishedAt: String?,
    val daysCollected: Int?,
    val daysRequired: Int?,
    val message: String?
)
