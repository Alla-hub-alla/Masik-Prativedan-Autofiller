package com.example.data

import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {
    val allReports: Flow<List<ReportEntity>> = reportDao.getAllReports()

    suspend fun getPendingReports(): List<ReportEntity> {
        return reportDao.getReportsByStatus(ReportEntity.STATUS_PENDING)
    }

    suspend fun getReportById(id: Long): ReportEntity? {
        return reportDao.getReportById(id)
    }

    suspend fun insertReport(report: ReportEntity): Long {
        return reportDao.insertReport(report)
    }

    suspend fun updateReport(report: ReportEntity) {
        reportDao.updateReport(report)
    }

    suspend fun updateReportStatus(id: Long, status: String, logMsg: String, screenshotPath: String? = null) {
        val existing = reportDao.getReportById(id) ?: return
        val updated = existing.copy(
            status = status,
            lastLogMessage = logMsg,
            screenshotPath = screenshotPath ?: existing.screenshotPath
        )
        reportDao.updateReport(updated)
    }

    suspend fun deleteReport(id: Long) {
        reportDao.deleteReportById(id)
    }

    suspend fun deleteCompleted() {
        reportDao.deleteReportsByStatus(ReportEntity.STATUS_COMPLETED)
    }

    suspend fun clearAll() {
        reportDao.clearAll()
    }
}
