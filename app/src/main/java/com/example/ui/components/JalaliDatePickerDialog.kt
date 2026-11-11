package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.DateTimeUtils
import com.example.ui.util.JalaliCalendar
import com.example.ui.util.JalaliDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JalaliDatePickerDialog(
    initialTimestamp: Long,
    onDismiss: () -> Unit,
    onConfirm: (selectedTimestamp: Long) -> Unit
) {
    val initialJalali = remember(initialTimestamp) { JalaliCalendar.fromTimestamp(initialTimestamp) }

    var selectedYear by remember { mutableIntStateOf(initialJalali.year) }
    var selectedMonth by remember { mutableIntStateOf(initialJalali.month) }
    var selectedDay by remember { mutableIntStateOf(initialJalali.day) }

    var yearDropdownExpanded by remember { mutableStateOf(false) }

    val daysInMonth = remember(selectedYear, selectedMonth) {
        JalaliCalendar.getDaysInMonth(selectedYear, selectedMonth)
    }

    if (selectedDay > daysInMonth) {
        selectedDay = daysInMonth
    }

    val currentJalaliDate = remember(selectedYear, selectedMonth, selectedDay) {
        JalaliDate(selectedYear, selectedMonth, selectedDay)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "انتخاب تاریخ شمسی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentJalaliDate.formatWithMonthName(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // انتخاب سال
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سال:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    ExposedDropdownMenuBox(
                        expanded = yearDropdownExpanded,
                        onExpandedChange = { yearDropdownExpanded = !yearDropdownExpanded },
                        modifier = Modifier.width(140.dp)
                    ) {
                        OutlinedTextField(
                            value = DateTimeUtils.toPersianDigits(selectedYear.toString()),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearDropdownExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = yearDropdownExpanded,
                            onDismissRequest = { yearDropdownExpanded = false }
                        ) {
                            (1395..1415).forEach { yr ->
                                DropdownMenuItem(
                                    text = { Text(DateTimeUtils.toPersianDigits(yr.toString())) },
                                    onClick = {
                                        selectedYear = yr
                                        yearDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // انتخاب ماه (۱۲ ماه شمسی)
                Text(
                    text = "ماه:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(JalaliCalendar.monthNames.indices.toList()) { index ->
                        val mNum = index + 1
                        val isSelected = selectedMonth == mNum
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMonth = mNum },
                            label = {
                                Text(
                                    text = JalaliCalendar.monthNames[index],
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // انتخاب روز (۱ تا ۳۱)
                Text(
                    text = "روز:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(daysInMonth) { dayIndex ->
                        val d = dayIndex + 1
                        val isSelected = selectedDay == d
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { selectedDay = d },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = DateTimeUtils.toPersianDigits(d.toString()),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = initialTimestamp }
                    val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                    val minute = cal.get(java.util.Calendar.MINUTE)
                    val newTimestamp = currentJalaliDate.toTimestamp(hour, minute)
                    onConfirm(newTimestamp)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("تأیید", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("انصراف")
            }
        }
    )
}
