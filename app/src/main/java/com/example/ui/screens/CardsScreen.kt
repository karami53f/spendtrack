package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CardEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.PurchaseRedColor
import com.example.ui.components.SettlementGreenColor
import com.example.ui.util.DateTimeUtils

@Composable
fun CardsScreen(
    cardsList: List<CardEntity>,
    purchases: List<TransactionEntity>,
    onAddCard: (name: String, isDefault: Boolean, colorHex: Long) -> Unit,
    onUpdateCard: (CardEntity) -> Unit,
    onDeleteCard: (CardEntity) -> Unit,
    onSetDefaultCard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<CardEntity?>(null) }
    var cardToDelete by remember { mutableStateOf<CardEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("افزودن کارت", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_card_button")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مدیریت کارت‌ها",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "روش‌های پرداخت و انتخاب کارت پیش‌فرض",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Explanatory info
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "یک کارت می‌تواند به عنوان کارت پیش‌فرض انتخاب شود تا در فرم ثبت خرید یا تسویه به‌صورت خودکار در بالای فهرست قرار گیرد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "کارت‌ها و حساب‌های شما",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${DateTimeUtils.toPersianDigits(cardsList.size.toString())} کارت",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty state
            if (cardsList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "هیچ کارتی ثبت نشده است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "کارت‌های بانکی مانند پول نقد، بانک ملت، یا بانک ملی را اضافه کنید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(onClick = { showAddDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("افزودن اولین کارت")
                            }
                        }
                    }
                }
            } else {
                items(cardsList, key = { it.id }) { card ->
                    val cardPurchases = purchases.filter { it.cardName.equals(card.name, ignoreCase = true) }
                    val totalSpentOnCard = cardPurchases.sumOf { if (it.type == TransactionType.PURCHASE) it.amount else -it.amount }
                    val baseColor = Color(card.colorHex)

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_item_${card.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            baseColor,
                                            baseColor.copy(alpha = 0.85f),
                                            Color(0xFF1E2522)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                // Top row: Card Chip + Default Status Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(width = 36.dp, height = 26.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    if (card.isDefault) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color.White.copy(alpha = 0.95f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD48B00),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "کارت پیش‌فرض",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF1B221E),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Card Name
                                Text(
                                    text = card.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                val prefix = if (totalSpentOnCard >= 0) "+" else "−"
                                Text(
                                    text = "مانده: $prefix ${DateTimeUtils.formatToman(kotlin.math.abs(totalSpentOnCard))} • ${DateTimeUtils.toPersianDigits(cardPurchases.size.toString())} تراکنش",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f)
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // Bottom Actions on Card
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!card.isDefault) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.15f),
                                            modifier = Modifier
                                                .clickable { onSetDefaultCard(card.id) }
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.StarOutline,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "انتخاب به عنوان پیش‌فرض",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { cardToEdit = card },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "ویرایش کارت",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { cardToDelete = card },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "حذف کارت",
                                                tint = Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Add Card Dialog
    if (showAddDialog) {
        CardFormDialog(
            title = "افزودن کارت بانکی",
            initialName = "",
            initialIsDefault = cardsList.isEmpty(),
            initialColorHex = 0xFF0A6E51,
            confirmButtonText = "افزودن کارت",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, isDefault, colorHex ->
                onAddCard(name, isDefault, colorHex)
                showAddDialog = false
            }
        )
    }

    // Edit Card Dialog
    cardToEdit?.let { card ->
        CardFormDialog(
            title = "ویرایش کارت بانکی",
            initialName = card.name,
            initialIsDefault = card.isDefault,
            initialColorHex = card.colorHex,
            confirmButtonText = "ذخیره تغییرات",
            onDismiss = { cardToEdit = null },
            onConfirm = { updatedName, updatedIsDefault, updatedColorHex ->
                onUpdateCard(
                    card.copy(
                        name = updatedName,
                        isDefault = updatedIsDefault,
                        colorHex = updatedColorHex
                    )
                )
                cardToEdit = null
            }
        )
    }

    // Delete Card Dialog
    cardToDelete?.let { card ->
        val cardPurchases = purchases.filter { it.cardName.equals(card.name, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("حذف کارت «${card.name}»؟") },
            text = {
                Column {
                    if (cardPurchases.isNotEmpty()) {
                        Text("این کارت در ${DateTimeUtils.toPersianDigits(cardPurchases.size.toString())} تراکنش استفاده شده است. سوابق حفظ می‌شوند اما این کارت از فهرست انتخاب‌های آینده حذف خواهد شد.")
                    } else {
                        Text("آیا از حذف این کارت بانکی مطمئن هستید؟")
                    }
                    if (card.isDefault) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "توجه: این کارت پیش‌فرض شماست. پس از حذف، لطفاً کارت دیگری را به عنوان پیش‌فرض تعیین کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCard(card)
                        cardToDelete = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun CardFormDialog(
    title: String,
    initialName: String,
    initialIsDefault: Boolean,
    initialColorHex: Long,
    confirmButtonText: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, isDefault: Boolean, colorHex: Long) -> Unit
) {
    var cardName by remember { mutableStateOf(initialName) }
    var isDefault by remember { mutableStateOf(initialIsDefault) }
    var selectedColorHex by remember { mutableLongStateOf(initialColorHex) }

    val colorPresets = listOf(
        0xFF0A6E51 to "سبز زمردی",
        0xFF1976D2 to "آبی",
        0xFFC2185B to "قرمز یاقوتی",
        0xFF7B1FA2 to "بنفش",
        0xFFE65100 to "نارنجی",
        0xFF00796B to "فیروزه‌ای",
        0xFF263238 to "طوسی تیره"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("نام کارت یا حساب") },
                    placeholder = { Text("مثلاً: پول نقد، بانک ملت، بانک ملی") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("card_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "رنگ کارت",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorPresets) { (hex, _) ->
                        val isSelected = selectedColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    2.dp,
                                    if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "انتخاب شده",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDefault = !isDefault }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "تنظیم به عنوان کارت پیش‌فرض",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "هنگام ثبت تراکنش جدید به‌صورت خودکار انتخاب می‌شود",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cardName.isNotBlank()) {
                        onConfirm(cardName.trim(), isDefault, selectedColorHex)
                    }
                },
                enabled = cardName.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_card_dialog_button")
            ) {
                Text(confirmButtonText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
