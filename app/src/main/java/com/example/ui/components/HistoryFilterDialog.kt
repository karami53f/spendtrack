package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ItemEntity
import com.example.data.model.TransactionType
import com.example.ui.util.DateTimeUtils
import com.example.ui.util.JalaliCalendar
import com.example.ui.util.JalaliDate
import com.example.ui.viewmodel.HistoryFilter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFilterDialog(
    currentFilter: HistoryFilter,
    itemsList: List<ItemEntity>,
    onDismiss: () -> Unit,
    onApplyFilter: (HistoryFilter) -> Unit
) {
    var startDate by remember { mutableStateOf(currentFilter.startDate) }
    var endDate by remember { mutableStateOf(currentFilter.endDate) }
    var minAmountText by remember { mutableStateOf(currentFilter.minAmount?.toString() ?: "") }
    var maxAmountText by remember { mutableStateOf(currentFilter.maxAmount?.toString() ?: "") }
    var selectedItemName by remember { mutableStateOf(currentFilter.itemName ?: "") }
    var selectedType by remember { mutableStateOf(currentFilter.type) }

    var itemDropdownExpanded by remember { mutableStateOf(false) }

    var pickingStartDate by remember { mutableStateOf(false) }
    var pickingEndDate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فیلتر تراکنش‌ها", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = {
                        startDate = null
                        endDate = null
                        minAmountText = ""
                        maxAmountText = ""
                        selectedItemName = ""
                        selectedType = null
                    }
                ) {
                    Text("حذف فیلترها", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Transaction Type Filter (نوع تراکنش)
                Text(
                    text = "نوع تراکنش",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text("همه") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.PURCHASE,
                        onClick = { selectedType = TransactionType.PURCHASE },
                        label = { Text("خریدها (+)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.SETTLEMENT,
                        onClick = { selectedType = TransactionType.SETTLEMENT },
                        label = { Text("تسویه‌ها (−)") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Date Range Filter in Jalali (بازه زمانی در تقویم شمسی)
                Text(
                    text = "بازه زمانی (گاه‌شماری شمسی)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Jalali Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val now = System.currentTimeMillis()
                    val jNow = JalaliCalendar.fromTimestamp(now)

                    FilterChip(
                        selected = false,
                        onClick = {
                            startDate = jNow.toTimestamp(0, 0, 0)
                            endDate = jNow.toTimestamp(23, 59, 59)
                        },
                        label = { Text("امروز") },
                        shape = RoundedCornerShape(6.dp)
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = now
                            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                            val daysFromSaturday = (dayOfWeek + 1) % 7
                            val startCal = Calendar.getInstance().apply {
                                timeInMillis = now - (daysFromSaturday * 24L * 3600L * 1000L)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                            startDate = startCal.timeInMillis
                            endDate = now
                        },
                        label = { Text("این هفته") },
                        shape = RoundedCornerShape(6.dp)
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            startDate = JalaliDate(jNow.year, jNow.month, 1).toTimestamp(0, 0, 0)
                            val daysInM = JalaliCalendar.getDaysInMonth(jNow.year, jNow.month)
                            endDate = JalaliDate(jNow.year, jNow.month, daysInM).toTimestamp(23, 59, 59)
                        },
                        label = { Text("این ماه شمسی") },
                        shape = RoundedCornerShape(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start Date Button (Jalali)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { pickingStartDate = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = startDate?.let { DateTimeUtils.formatDate(it) } ?: "از تاریخ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (startDate != null) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // End Date Button (Jalali)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { pickingEndDate = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = endDate?.let { DateTimeUtils.formatDate(it) } ?: "تا تاریخ",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (endDate != null) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Amount Range (محدوده مبلغ به تومان)
                Text(
                    text = "محدوده مبلغ (تومان)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minAmountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) minAmountText = it },
                        label = { Text("از مبلغ") },
                        placeholder = { Text("مثلاً ۵۰,۰۰۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = maxAmountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) maxAmountText = it },
                        label = { Text("تا مبلغ") },
                        placeholder = { Text("مثلاً ۵۰۰,۰۰۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Item Name Filter (نام قلم)
                Text(
                    text = "فیلتر بر اساس قلم",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = itemDropdownExpanded,
                    onExpandedChange = { itemDropdownExpanded = !itemDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (selectedItemName.isBlank()) "همه اقلام" else selectedItemName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("انتخاب قلم") },
                        trailingIcon = {
                            if (selectedItemName.isNotBlank()) {
                                IconButton(onClick = { selectedItemName = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "پاک کردن قلم")
                                }
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemDropdownExpanded)
                            }
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = itemDropdownExpanded,
                        onDismissRequest = { itemDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("همه اقلام", fontWeight = FontWeight.Bold) },
                            onClick = {
                                selectedItemName = ""
                                itemDropdownExpanded = false
                            }
                        )
                        itemsList.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = DateTimeUtils.getIconForName(item.iconName),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    selectedItemName = item.name
                                    itemDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyFilter(
                        HistoryFilter(
                            startDate = startDate,
                            endDate = endDate,
                            minAmount = minAmountText.toLongOrNull(),
                            maxAmount = maxAmountText.toLongOrNull(),
                            itemName = selectedItemName.ifBlank { null },
                            type = selectedType
                        )
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("apply_filter_button")
            ) {
                Text("اعمال فیلتر", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("انصراف")
            }
        }
    )

    // Jalali Date Picker for Start Date
    if (pickingStartDate) {
        JalaliDatePickerDialog(
            initialTimestamp = startDate ?: System.currentTimeMillis(),
            onDismiss = { pickingStartDate = false },
            onConfirm = { picked ->
                startDate = picked
                pickingStartDate = false
            }
        )
    }

    // Jalali Date Picker for End Date
    if (pickingEndDate) {
        JalaliDatePickerDialog(
            initialTimestamp = endDate ?: System.currentTimeMillis(),
            onDismiss = { pickingEndDate = false },
            onConfirm = { picked ->
                endDate = picked
                pickingEndDate = false
            }
        )
    }
}
