package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CardEntity
import com.example.data.model.ItemEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.util.DateTimeUtils
import java.util.Calendar

val PurchaseRedColor = Color(0xFFD32F2F)
val SettlementGreenColor = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionBottomSheet(
    sheetState: SheetState,
    transactionToEdit: TransactionEntity? = null,
    initialType: TransactionType = TransactionType.PURCHASE,
    items: List<ItemEntity>,
    cards: List<CardEntity>,
    defaultCard: CardEntity?,
    onDismissRequest: () -> Unit,
    onSaveTransaction: (type: TransactionType, itemName: String, amount: Long, timestamp: Long, cardName: String, note: String) -> Unit,
    onAddNewItem: (name: String) -> Unit,
    onSetDefaultCard: (Long) -> Unit,
    onNavigateToItems: () -> Unit,
    onNavigateToCards: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = transactionToEdit != null

    var selectedType by remember {
        mutableStateOf(transactionToEdit?.type ?: initialType)
    }
    var selectedItemName by remember {
        mutableStateOf(transactionToEdit?.itemName ?: "")
    }
    var amountText by remember {
        mutableStateOf(transactionToEdit?.amount?.toString() ?: "")
    }
    var noteText by remember {
        mutableStateOf(transactionToEdit?.note ?: "")
    }
    var selectedTimestamp by remember {
        mutableLongStateOf(transactionToEdit?.timestamp ?: System.currentTimeMillis())
    }
    var selectedCardName by remember {
        mutableStateOf(transactionToEdit?.cardName ?: "")
    }

    var itemDropdownExpanded by remember { mutableStateOf(false) }
    var cardDropdownExpanded by remember { mutableStateOf(false) }

    var showJalaliDatePicker by remember { mutableStateOf(false) }
    var showAddNewItemDialog by remember { mutableStateOf(false) }
    var newItemNameInput by remember { mutableStateOf("") }

    LaunchedEffect(items) {
        if (selectedItemName.isEmpty() && items.isNotEmpty()) {
            selectedItemName = items.first().name
        }
    }

    LaunchedEffect(cards, defaultCard) {
        if (selectedCardName.isEmpty()) {
            selectedCardName = defaultCard?.name ?: cards.firstOrNull()?.name ?: ""
        }
    }

    val parsedAmount = amountText.toLongOrNull() ?: 0L
    val isFormValid = selectedItemName.isNotBlank() && parsedAmount > 0L && selectedCardName.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("add_edit_transaction_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 28.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEditing) "ویرایش تراکنش" else if (selectedType == TransactionType.PURCHASE) "ثبت خرید جدید" else "ثبت تسویه جدید",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (selectedType == TransactionType.PURCHASE) "هزینه‌ای که به جمع مخارج افزوده می‌شود (قرمز)" else "تسویه‌ای که از مانده کسر می‌شود (سبز)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedType == TransactionType.PURCHASE) PurchaseRedColor.copy(alpha = 0.15f) else SettlementGreenColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (selectedType == TransactionType.PURCHASE) Icons.Default.Add else Icons.Default.Remove,
                            contentDescription = null,
                            tint = if (selectedType == TransactionType.PURCHASE) PurchaseRedColor else SettlementGreenColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Transaction Type Toggle (خرید / تسویه)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                val isPurchase = selectedType == TransactionType.PURCHASE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPurchase) PurchaseRedColor else Color.Transparent)
                        .clickable { selectedType = TransactionType.PURCHASE }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = if (isPurchase) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "خرید (+)",
                            fontWeight = FontWeight.Bold,
                            color = if (isPurchase) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val isSettlement = selectedType == TransactionType.SETTLEMENT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSettlement) SettlementGreenColor else Color.Transparent)
                        .clickable { selectedType = TransactionType.SETTLEMENT }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = null,
                            tint = if (isSettlement) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تسویه (−)",
                            fontWeight = FontWeight.Bold,
                            color = if (isSettlement) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Item Selection (نام قلم) + گزینه "افزودن قلم جدید" در انتهای دراپ‌داون
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نام قلم",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = { showAddNewItemDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("افزودن قلم جدید", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = itemDropdownExpanded,
                onExpandedChange = { itemDropdownExpanded = !itemDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedItemName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("انتخاب قلم") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemDropdownExpanded)
                    },
                    leadingIcon = {
                        val icon = DateTimeUtils.getIconForName(selectedItemName)
                        Icon(imageVector = icon, contentDescription = null)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("item_dropdown_field")
                )

                ExposedDropdownMenu(
                    expanded = itemDropdownExpanded,
                    onDismissRequest = { itemDropdownExpanded = false }
                ) {
                    // اقلام ذخیره‌شده (بنزین، وسایل پیتزا، هزینه‌های ماشین و اقلام اضافه‌شده)
                    items.forEach { item ->
                        val icon = DateTimeUtils.getIconForName(item.name)
                        DropdownMenuItem(
                            text = { Text(item.name, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                if (item.name == selectedItemName) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "انتخاب شده",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            onClick = {
                                selectedItemName = item.name
                                itemDropdownExpanded = false
                            }
                        )
                    }

                    // گزینه «افزودن» در انتهای لیست کشویی طبق خواسته کاربر
                    Divider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "افزودن قلم جدید (افزودن)...",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            itemDropdownExpanded = false
                            showAddNewItemDialog = true
                        }
                    )
                }
            }

            // Quick chips
            if (items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        FilterChip(
                            selected = item.name == selectedItemName,
                            onClick = { selectedItemName = item.name },
                            label = { Text(item.name) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Amount Input in Iranian Toman (مبلغ به تومان)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مبلغ (تومان)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (parsedAmount > 0L) {
                    Text(
                        text = DateTimeUtils.formatToman(parsedAmount),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedType == TransactionType.PURCHASE) PurchaseRedColor else SettlementGreenColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        amountText = input
                    }
                },
                label = { Text("مبلغ به عدد صحیح") },
                suffix = {
                    Text(
                        text = "تومان",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input_field")
            )

            // Quick amount add chips (تومان)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10000L, 50000L, 100000L, 500000L).forEach { increment ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val current = amountText.toLongOrNull() ?: 0L
                                amountText = (current + increment).toString()
                            }
                    ) {
                        Text(
                            text = "+${DateTimeUtils.formatTomanRaw(increment)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Optional Description / Note field (توضیحات یا یادداشت)
            Text(
                text = "توضیحات یا یادداشت (اختیاری)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("مثلاً: بنزین سوپر، تعویض روغن، پنیر پیتزا") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input_field")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Date & Time Selection in Jalali Calendar (تاریخ و زمان شمسی)
            Text(
                text = "تاریخ و زمان (گاه‌شماری شمسی)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // دکمه تقویم شمسی
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1.3f)
                        .clickable { showJalaliDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "ویرایش تاریخ شمسی",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateTimeUtils.formatDate(selectedTimestamp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // دکمه زمان
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val updated = Calendar.getInstance().apply {
                                        timeInMillis = selectedTimestamp
                                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        set(Calendar.MINUTE, minute)
                                    }
                                    selectedTimestamp = updated.timeInMillis
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                true
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "ویرایش زمان",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateTimeUtils.formatTime(selectedTimestamp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Card Selection with Default Selection (کارت و انتخاب به عنوان پیش‌فرض)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "کارت یا روش پرداخت",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (defaultCard != null && defaultCard.name == selectedCardName) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "پیش‌فرض",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (cards.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "کارتی ثبت نشده است.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(onClick = {
                            onDismissRequest()
                            onNavigateToCards()
                        }) {
                            Text("مدیریت کارت‌ها")
                        }
                    }
                }
            } else {
                val currentCard = cards.find { it.name == selectedCardName }
                val cardColor = currentCard?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary

                ExposedDropdownMenuBox(
                    expanded = cardDropdownExpanded,
                    onExpandedChange = { cardDropdownExpanded = !cardDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCardName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("انتخاب کارت") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(cardColor)
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardDropdownExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("card_dropdown_field")
                    )

                    ExposedDropdownMenu(
                        expanded = cardDropdownExpanded,
                        onDismissRequest = { cardDropdownExpanded = false }
                    ) {
                        cards.forEach { card ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(card.name, fontWeight = FontWeight.Medium)
                                            if (card.isDefault) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(پیش‌فرض)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // دکمه تنظیم سریع کارت پیش‌فرض مستقیم در منو
                                        if (!card.isDefault) {
                                            TextButton(
                                                onClick = {
                                                    onSetDefaultCard(card.id)
                                                    selectedCardName = card.name
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.StarOutline,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("تنظیم پیش‌فرض", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(card.colorHex))
                                    )
                                },
                                onClick = {
                                    selectedCardName = card.name
                                    cardDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("انصراف")
                }

                Button(
                    onClick = {
                        if (isFormValid) {
                            onSaveTransaction(
                                selectedType,
                                selectedItemName,
                                parsedAmount,
                                selectedTimestamp,
                                selectedCardName,
                                noteText
                            )
                            onDismissRequest()
                        }
                    },
                    enabled = isFormValid,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.PURCHASE) PurchaseRedColor else SettlementGreenColor
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("save_transaction_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "ذخیره تغییرات" else "ثبت تراکنش",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // دیالوگ انتخاب تاریخ شمسی (Jalali Date Picker)
    if (showJalaliDatePicker) {
        JalaliDatePickerDialog(
            initialTimestamp = selectedTimestamp,
            onDismiss = { showJalaliDatePicker = false },
            onConfirm = { pickedTimestamp ->
                selectedTimestamp = pickedTimestamp
                showJalaliDatePicker = false
            }
        )
    }

    // دیالوگ افزودن قلم جدید از داخل دراپ‌داون (افزودن)
    if (showAddNewItemDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddNewItemDialog = false
                newItemNameInput = ""
            },
            title = { Text("افزودن قلم جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("نام قلم جدید را وارد کنید. از این پس در فهرست اقلام قابل انتخاب خواهد بود:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemNameInput,
                        onValueChange = { newItemNameInput = it },
                        label = { Text("نام قلم") },
                        placeholder = { Text("مثلاً: قهوه، نان، قبض...") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_item_quick_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemNameInput.isNotBlank()) {
                            val trimmed = newItemNameInput.trim()
                            onAddNewItem(trimmed)
                            selectedItemName = trimmed
                            showAddNewItemDialog = false
                            newItemNameInput = ""
                        }
                    },
                    enabled = newItemNameInput.isNotBlank()
                ) {
                    Text("افزودن", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddNewItemDialog = false
                        newItemNameInput = ""
                    }
                ) {
                    Text("انصراف")
                }
            }
        )
    }
}
