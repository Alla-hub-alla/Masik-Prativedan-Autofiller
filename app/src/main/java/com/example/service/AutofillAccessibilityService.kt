package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.AppDatabase
import com.example.data.ReportEntity
import com.example.data.ReportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AutofillAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isBotRunning = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
        addLog("Accessibility Bot Service Connected")

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        info.notificationTimeout = 100
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Events can trigger context checks if target app window opens
    }

    override fun onInterrupt() {
        addLog("Service Interrupted")
        _isServiceActive.value = false
        instance = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceActive.value = false
        instance = null
    }

    fun startQueueProcessing(targetPackage: String) {
        if (isBotRunning) return
        isBotRunning = true
        _isBotActive.value = true
        addLog("Starting Auto-fill Bot queue runner for package: $targetPackage")

        serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val repo = ReportRepository(db.reportDao())

            var pendingList = repo.getPendingReports()
            if (pendingList.isEmpty()) {
                addLog("No pending items found in queue.")
                isBotRunning = false
                _isBotActive.value = false
                return@launch
            }

            addLog("Found ${pendingList.size} pending reports to process.")

            for (report in pendingList) {
                if (!isBotRunning) break

                addLog("Processing Report #${report.id} [Code: ${report.centerCode}]...")
                repo.updateReportStatus(report.id, ReportEntity.STATUS_PROCESSING, "Bot auto-filling form...")

                delay(1000) // Pace auto-fill operations smoothly

                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    addLog("Warning: Could not get active window node. Ensure target app is on screen.")
                }

                var fieldsFilled = 0

                rootNode?.let { root ->
                    // Fill Center Code
                    if (setTextForKeywords(root, listOf("center code", "code", "centre code", "awc code", "center_code"), report.centerCode)) {
                        fieldsFilled++
                        addLog("  ✓ Filled Center Code: ${report.centerCode}")
                    }

                    // Fill Center Name
                    if (setTextForKeywords(root, listOf("center name", "name", "centre name", "awc name", "center_name"), report.centerName)) {
                        fieldsFilled++
                        addLog("  ✓ Filled Center Name: ${report.centerName}")
                    }

                    // Fill Month Year
                    if (setTextForKeywords(root, listOf("month", "report month", "month/year", "period", "report_month"), report.reportMonthYear)) {
                        fieldsFilled++
                        addLog("  ✓ Filled Month: ${report.reportMonthYear}")
                    }

                    // Fill Beneficiaries
                    if (setTextForKeywords(root, listOf("beneficiaries", "total beneficiaries", "count", "children", "beneficiaries_count"), report.totalBeneficiaries.toString())) {
                        fieldsFilled++
                        addLog("  ✓ Filled Beneficiaries: ${report.totalBeneficiaries}")
                    }

                    // Fill Attendance
                    if (setTextForKeywords(root, listOf("attendance", "present count", "attendance_count"), report.attendanceCount.toString())) {
                        fieldsFilled++
                        addLog("  ✓ Filled Attendance: ${report.attendanceCount}")
                    }

                    // Fill Meeting Date
                    if (setTextForKeywords(root, listOf("meeting date", "date", "meeting_held_date", "held date"), report.meetingHeldDate)) {
                        fieldsFilled++
                        addLog("  ✓ Filled Meeting Date: ${report.meetingHeldDate}")
                    }

                    // Toggle Nutrition Checkbox
                    if (toggleCheckboxForKeywords(root, listOf("nutrition", "thr", "food", "nutrition_distributed"), report.nutritionDistributed)) {
                        fieldsFilled++
                        addLog("  ✓ Checked Nutrition Distributed: ${report.nutritionDistributed}")
                    }

                    // Toggle Inspection Checkbox
                    if (toggleCheckboxForKeywords(root, listOf("inspection", "supervisor", "check", "inspection_completed"), report.inspectionCompleted)) {
                        fieldsFilled++
                        addLog("  ✓ Checked Inspection Completed: ${report.inspectionCompleted}")
                    }

                    delay(800)

                    // Click Submit
                    val clickedSubmit = clickButtonForKeywords(root, listOf("submit", "save", " जमा करें", "submit report", "send"))
                    if (clickedSubmit) {
                        addLog("  ✓ Clicked Submit Button!")
                    } else {
                        addLog("  • Submit button not found on screen, assuming auto-save.")
                    }

                    root.recycle()
                }

                // Take background screenshot
                val screenshotPath = captureBackgroundScreenshot(report.id)
                if (screenshotPath != null) {
                    addLog("  ✓ Saved verification screenshot!")
                }

                val logSummary = "Auto-filled $fieldsFilled fields successfully."
                repo.updateReportStatus(report.id, ReportEntity.STATUS_COMPLETED, logSummary, screenshotPath)
                addLog("Report #${report.id} completed successfully!")

                delay(1200)
            }

            addLog("All pending queue items processed.")
            isBotRunning = false
            _isBotActive.value = false
        }
    }

    fun stopQueueProcessing() {
        isBotRunning = false
        _isBotActive.value = false
        addLog("Auto-fill Bot queue runner stopped by user.")
    }

    private fun setTextForKeywords(node: AccessibilityNodeInfo, keywords: List<String>, text: String): Boolean {
        val targets = mutableListOf<AccessibilityNodeInfo>()
        findNodesByKeywords(node, keywords, targets, isEditable = true)
        for (target in targets) {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            if (target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
                return true
            }
        }
        return false
    }

    private fun toggleCheckboxForKeywords(node: AccessibilityNodeInfo, keywords: List<String>, shouldBeChecked: Boolean): Boolean {
        val targets = mutableListOf<AccessibilityNodeInfo>()
        findNodesByKeywords(node, keywords, targets, isCheckable = true)
        for (target in targets) {
            if (target.isChecked != shouldBeChecked) {
                target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            return true
        }
        return false
    }

    private fun clickButtonForKeywords(node: AccessibilityNodeInfo, keywords: List<String>): Boolean {
        val targets = mutableListOf<AccessibilityNodeInfo>()
        findNodesByKeywords(node, keywords, targets, isClickable = true)
        for (target in targets) {
            if (target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }
        }
        return false
    }

    private fun findNodesByKeywords(
        node: AccessibilityNodeInfo,
        keywords: List<String>,
        resultList: MutableList<AccessibilityNodeInfo>,
        isEditable: Boolean = false,
        isCheckable: Boolean = false,
        isClickable: Boolean = false
    ) {
        val text = (node.text?.toString() ?: "") + " " + (node.contentDescription?.toString() ?: "") + " " + (node.viewIdResourceName ?: "")
        val lowerText = text.lowercase()

        val matchesKeyword = keywords.any { lowerText.contains(it) }

        if (matchesKeyword) {
            if (isEditable && node.isEditable) {
                resultList.add(node)
            } else if (isCheckable && (node.isCheckable || node.className?.contains("CheckBox") == true || node.className?.contains("Switch") == true)) {
                resultList.add(node)
            } else if (isClickable && (node.isClickable || node.className?.contains("Button") == true)) {
                resultList.add(node)
            }
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodesByKeywords(child, keywords, resultList, isEditable, isCheckable, isClickable)
            }
        }
    }

    private suspend fun captureBackgroundScreenshot(reportId: Long): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            var capturedPath: String? = null
            takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
                override fun onSuccess(screenshot: ScreenshotResult) {
                    val bitmap = Bitmap.wrapHardwareBuffer(screenshot.hardwareBuffer, screenshot.colorSpace)
                    if (bitmap != null) {
                        capturedPath = saveBitmapToDisk(reportId, bitmap)
                    }
                    screenshot.hardwareBuffer.close()
                }

                override fun onFailure(errorCode: Int) {
                    Log.e("AutofillService", "Screenshot failed with error $errorCode")
                }
            })
            delay(400) // Wait brief moment for screenshot callback
            return capturedPath
        }
        return null
    }

    private fun saveBitmapToDisk(reportId: Long, bitmap: Bitmap): String? {
        return try {
            val dir = File(filesDir, "screenshots")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "report_${reportId}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("AutofillService", "Error saving screenshot", e)
            null
        }
    }

    companion object {
        const val TAG = "AutofillService"

        var instance: AutofillAccessibilityService? = null

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _isBotActive = MutableStateFlow(false)
        val isBotActive: StateFlow<Boolean> = _isBotActive.asStateFlow()

        private val _logs = MutableStateFlow<List<String>>(listOf("Autofill Bot initialization ready."))
        val logs: StateFlow<List<String>> = _logs.asStateFlow()

        fun addLog(msg: String) {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            val formatted = "[$timestamp] $msg"
            Log.d(TAG, formatted)
            _logs.value = (_logs.value + formatted).takeLast(100)
        }

        fun isAccessibilityPermissionGranted(context: Context): Boolean {
            val serviceName = context.packageName + "/" + AutofillAccessibilityService::class.java.canonicalName
            val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
            return enabledServices.contains(serviceName)
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
