package com.example.ui.review

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ai.ExtractedReportData
import com.example.data.ReportEntity
import com.example.data.ReportRepository
import kotlinx.coroutines.launch

@Composable
fun ReviewEditScreen(
    extractedData: ExtractedReportData?,
    scannedBitmap: Bitmap?,
    repository: ReportRepository,
    onQueueSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var centerName by remember(extractedData) { mutableStateOf(extractedData?.centerName ?: "Anganwadi Center #104") }
    var centerCode by remember(extractedData) { mutableStateOf(extractedData?.centerCode ?: "AWC-104") }
    var reportMonthYear by remember(extractedData) { mutableStateOf(extractedData?.reportMonthYear ?: "August 2026") }
    var totalBeneficiaries by remember(extractedData) { mutableStateOf(extractedData?.totalBeneficiaries?.toString() ?: "45") }
    var attendanceCount by remember(extractedData) { mutableStateOf(extractedData?.attendanceCount?.toString() ?: "38") }
    var nutritionDistributed by remember(extractedData) { mutableStateOf(extractedData?.nutritionDistributed ?: true) }
    var inspectionCompleted by remember(extractedData) { mutableStateOf(extractedData?.inspectionCompleted ?: true) }
    var meetingHeldDate by remember(extractedData) { mutableStateOf(extractedData?.meetingHeldDate ?: "2026-08-01") }

    var isSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // AI Extraction Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Data Extraction Review",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "AI Confidence Score: ${((extractedData?.confidenceScore ?: 0.95f) * 100).toInt()}% • Verify & Edit before queuing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Optional Scanned Thumbnail Preview
        if (scannedBitmap != null) {
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = scannedBitmap.asImageBitmap(),
                        contentDescription = "Scanned Form Thumbnail",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 1: Identification Fields
        Text(
            text = "CENTER & REPORT DETAILS",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = centerName,
            onValueChange = { centerName = it },
            label = { Text("Center Name") },
            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = centerCode,
                onValueChange = { centerCode = it },
                label = { Text("Center Code") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = reportMonthYear,
                onValueChange = { reportMonthYear = it },
                label = { Text("Report Month") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Numbers & Counts
        Text(
            text = "NUMERIC ATTENDANCE & COUNTS",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = totalBeneficiaries,
                onValueChange = { totalBeneficiaries = it },
                label = { Text("Total Beneficiaries") },
                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = attendanceCount,
                onValueChange = { attendanceCount = it },
                label = { Text("Attendance Count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = meetingHeldDate,
            onValueChange = { meetingHeldDate = it },
            label = { Text("Meeting Date (YYYY-MM-DD)") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Yes/No Fields
        Text(
            text = "STATUS TOGGLES (YES / NO)",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nutrition Distributed (THR)",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Take Home Ration / Meal distribution verified",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = nutritionDistributed,
                        onCheckedChange = { nutritionDistributed = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Inspection Completed",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Monthly supervisor site inspection performed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = inspectionCompleted,
                        onCheckedChange = { inspectionCompleted = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Save & Queue Action
        Button(
            onClick = {
                val entity = ReportEntity(
                    centerName = centerName.ifBlank { "Anganwadi Center" },
                    centerCode = centerCode.ifBlank { "AWC-100" },
                    reportMonthYear = reportMonthYear.ifBlank { "August 2026" },
                    totalBeneficiaries = totalBeneficiaries.toIntOrNull() ?: 0,
                    attendanceCount = attendanceCount.toIntOrNull() ?: 0,
                    nutritionDistributed = nutritionDistributed,
                    inspectionCompleted = inspectionCompleted,
                    meetingHeldDate = meetingHeldDate.ifBlank { "2026-08-01" },
                    status = ReportEntity.STATUS_PENDING,
                    lastLogMessage = "Queued for Accessibility Bot"
                )

                scope.launch {
                    repository.insertReport(entity)
                    isSaved = true
                    onQueueSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.Queue, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSaved) "Queued Successfully!" else "Save & Queue for Auto-fill Bot",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
