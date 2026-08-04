package com.example.ai

data class ExtractedReportData(
    val centerName: String = "",
    val centerCode: String = "",
    val reportMonthYear: String = "",
    val totalBeneficiaries: Int = 0,
    val attendanceCount: Int = 0,
    val nutritionDistributed: Boolean = false,
    val inspectionCompleted: Boolean = false,
    val meetingHeldDate: String = "",
    val confidenceScore: Float = 0.95f,
    val rawNotes: String = ""
)
