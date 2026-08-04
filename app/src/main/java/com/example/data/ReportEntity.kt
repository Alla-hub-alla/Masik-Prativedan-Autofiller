package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val centerName: String = "",
    val centerCode: String = "",
    val reportMonthYear: String = "",
    val totalBeneficiaries: Int = 0,
    val attendanceCount: Int = 0,
    val nutritionDistributed: Boolean = false,
    val inspectionCompleted: Boolean = false,
    val meetingHeldDate: String = "",
    val customFieldsJson: String = "{}",
    val status: String = STATUS_PENDING, // PENDING, PROCESSING, COMPLETED, FAILED
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val lastLogMessage: String = "Scanned and queued",
    val screenshotPath: String? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_FAILED = "FAILED"
    }
}
