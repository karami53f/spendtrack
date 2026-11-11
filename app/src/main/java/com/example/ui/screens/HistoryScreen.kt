package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.ItemEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.HistoryFilterDialog
import com.example.ui.components.PurchaseRedColor
import com.example.ui.components.SettlementGreenColor
import com.example.ui.util.DateTimeUtils
import com.example.ui.viewmodel.HistoryFilter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    transactions: List<TransactionEntity>,
    cards: List<CardEntity>,
    itemsList: List<ItemEntity>,
    currentFilter: HistoryFilter,
    totalPurchases: Long,
    totalSettlements: Long,
    netTotal: Long,
    filteredPurchasesTotal: Long,
    filteredSettlementsTotal: Long,
    filteredNetTotal: Long,
    spendingByCard: Map<String, Long>,
    onAddPurchaseClick: () -> Unit,
    onAddSettlementClick: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onBulkDeleteTransactions: (List<Long>) -> Unit,
    onUpdateFilter: (HistoryFilter) -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIds = remember { mutableStateListOf<Long>() }
    val isSelectionMode = selectedIds.isNotEmpty()

    var showFilterDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (!isSelectionMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // دکمه ثبت تسویه (سبز)
                    FloatingActionButton(
                        onClick = onAddSettlementClick,
                        containerColor = SettlementGreenColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("add_settlement_fab")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت تسویه", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // دکمه ثبت خرید (قرمز)
                    ExtendedFloatingActionButton(
                        onClick = onAddPurchaseClick,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("ثبت خرید", fontWeight = FontWeight.Bold) },
                        containerColor = PurchaseRedColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("add_purchase_fab")
                    )
                }
            } else {
                // حالت انتخاب چندگانه: حذف گروهی
                ExtendedFloatingActionButton(
                    onClick = { showBulkDeleteConfirm = true },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    text = { Text("حذف (${DateTimeUtils.toPersianDigits(selectedIds.size.toString())} مورد)", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("bulk_delete_fab")
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // نوار بالایی: عنوان یا حالت انتخاب
            item {
                if (isSelectionMode) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { selectedIds.clear() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "لغو انتخاب",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "${DateTimeUtils.toPersianDigits(selectedIds.size.toString())} مورد انتخاب شده",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        if (selectedIds.size == transactions.size) {
                                            selectedIds.clear()
                                        } else {
                                            selectedIds.clear()
                                            selectedIds.addAll(transactions.map { it.id })
                                        }
                                    }
                                ) {
                                    Text(
                                        if (selectedIds.size == transactions.size) "لغو همه" else "انتخاب همه",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { showBulkDeleteConfirm = true },
                                    enabled = selectedIds.isNotEmpty()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف موارد انتخابی",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "تاریخچه تراکنش‌ها",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "مبالغ به تومان (عدد صحیح)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // دکمه فیلتر
                            IconButton(
                                onClick = { showFilterDialog = true },
                                modifier = Modifier.testTag("filter_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (currentFilter.isActive) {
                                            Badge { Text(DateTimeUtils.toPersianDigits(currentFilter.activeCount.toString())) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = "فیلتر تراکنش‌ها",
                                        tint = if (currentFilter.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // دکمه انتخاب گروهی
                            if (transactions.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        transactions.firstOrNull()?.let { selectedIds.add(it.id) }
                                    },
                                    modifier = Modifier.testTag("select_mode_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SelectAll,
                                        contentDescription = "انتخاب چندگانه",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // کارت مجموع تراکنش‌های فیلتر شده (وقتی فیلتر فعال است)
            if (currentFilter.isActive) {
                item {
                    val purchasesDominate = filteredPurchasesTotal >= filteredSettlementsTotal
                    val dominantColor = if (purchasesDominate) PurchaseRedColor else SettlementGreenColor

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = dominantColor.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                dominantColor.copy(alpha = 0.35f),
                                RoundedCornerShape(20.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = null,
                                        tint = dominantColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مجموع تراکنش‌های فیلتر شده",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = dominantColor
                                    )
                                }

                                TextButton(onClick = onClearFilter) {
                                    Text("حذف فیلتر", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // مجموع به رنگ غالب (قرمز اگر خرید بیشتر باشد، سبز اگر تسویه بیشتر باشد)
                            val dominantPrefix = if (purchasesDominate) "+" else "−"
                            val displaySum = if (purchasesDominate) filteredNetTotal else -filteredNetTotal
                            Text(
                                text = "$dominantPrefix ${DateTimeUtils.formatToman(kotlin.math.abs(filteredNetTotal))}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = dominantColor
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // تفکیک مبالغ فیلتر شده
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PurchaseRedColor.copy(alpha = 0.12f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "خریدها: + ${DateTimeUtils.formatToman(filteredPurchasesTotal)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PurchaseRedColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SettlementGreenColor.copy(alpha = 0.12f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "تسویه‌ها: − ${DateTimeUtils.formatToman(filteredSettlementsTotal)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SettlementGreenColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // کارت خلاصه کلی (وقتی فیلتری فعال نیست)
                item {
                    val purchasesDominateGlobal = totalPurchases >= totalSettlements
                    val globalDominantColor = if (purchasesDominateGlobal) PurchaseRedColor else SettlementGreenColor

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                RoundedCornerShape(24.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "مانده کل مخارج",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            text = "${DateTimeUtils.toPersianDigits(transactions.size.toString())} تراکنش",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val prefix = if (netTotal >= 0) "+" else "−"
                                Text(
                                    text = "$prefix ${DateTimeUtils.formatToman(kotlin.math.abs(netTotal))}",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = globalDominantColor
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // جمع خریدها
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PurchaseRedColor.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            PurchaseRedColor.copy(alpha = 0.25f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "جمع خریدها (+)",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PurchaseRedColor
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "+ ${DateTimeUtils.formatToman(totalPurchases)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = PurchaseRedColor
                                            )
                                        }
                                    }

                                    // جمع تسویه‌ها
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SettlementGreenColor.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            SettlementGreenColor.copy(alpha = 0.25f)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "جمع تسویه‌ها (−)",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SettlementGreenColor
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "− ${DateTimeUtils.formatToman(totalSettlements)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = SettlementGreenColor
                                            )
                                        }
                                    }
                                }

                                if (spendingByCard.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "مانده به تفکیک کارت / روش پرداخت",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        spendingByCard.forEach { (cardName, cardNet) ->
                                            val matchedCard = cards.find { it.name == cardName }
                                            val indicatorColor = matchedCard?.let { Color(it.colorHex) }
                                                ?: MaterialTheme.colorScheme.primary

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(indicatorColor)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = cardName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    val cardPrefix = if (cardNet >= 0) "+" else "−"
                                                    Text(
                                                        text = "$cardPrefix ${DateTimeUtils.formatToman(kotlin.math.abs(cardNet))}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (cardNet >= 0) PurchaseRedColor else SettlementGreenColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // فهرست تراکنش‌ها
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فهرست تراکنش‌ها",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${DateTimeUtils.toPersianDigits(transactions.size.toString())} مورد",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (transactions.isEmpty()) {
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
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (currentFilter.isActive) "تراکنشی مطابق با فیلتر یافت نشد" else "هنوز تراکنشی ثبت نشده است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (currentFilter.isActive) "می‌توانید فیلترها را حذف کنید تا همه تراکنش‌ها نمایش داده شوند." else "با دکمه‌های زیر اولین خرید یا تسویه خود را ثبت کنید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            if (currentFilter.isActive) {
                                OutlinedButton(onClick = onClearFilter) {
                                    Text("حذف فیلترها")
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = onAddPurchaseClick) {
                                        Text("ثبت خرید (+)")
                                    }
                                    OutlinedButton(onClick = onAddSettlementClick) {
                                        Text("ثبت تسویه (−)")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val isSelected = selectedIds.contains(tx.id)
                    val isPurchase = tx.type == TransactionType.PURCHASE
                    val typeColor = if (isPurchase) PurchaseRedColor else SettlementGreenColor
                    val matchedCard = cards.find { it.name == tx.cardName }
                    val cardColor = matchedCard?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("transaction_item_${tx.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            if (isSelected) selectedIds.remove(tx.id) else selectedIds.add(tx.id)
                                        } else {
                                            onEditTransaction(tx)
                                        }
                                    },
                                    onLongClick = {
                                        if (isSelected) selectedIds.remove(tx.id) else selectedIds.add(tx.id)
                                    }
                                )
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedIds.add(tx.id) else selectedIds.remove(tx.id)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            // آیکون تراکنش
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = typeColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = DateTimeUtils.getIconForName(tx.itemName),
                                        contentDescription = null,
                                        tint = typeColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // نام قلم، یادداشت، تاریخ و کارت
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tx.itemName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = typeColor.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (isPurchase) "خرید" else "تسویه",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = typeColor,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (tx.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Notes,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tx.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = DateTimeUtils.formatDateTime(tx.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(cardColor)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = tx.cardName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // مبلغ و دکمه‌های ویرایش / حذف
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isPurchase) "+" else "−"} ${DateTimeUtils.formatToman(tx.amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = typeColor
                                )

                                if (!isSelectionMode) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onEditTransaction(tx) },
                                            modifier = Modifier.size(32.dp).testTag("edit_tx_${tx.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "ویرایش تراکنش",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { transactionToDelete = tx },
                                            modifier = Modifier.size(32.dp).testTag("delete_tx_${tx.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "حذف تراکنش",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
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

    if (showFilterDialog) {
        HistoryFilterDialog(
            currentFilter = currentFilter,
            itemsList = itemsList,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { updatedFilter ->
                onUpdateFilter(updatedFilter)
            }
        )
    }

    transactionToDelete?.let { tx ->
        val isPurchase = tx.type == TransactionType.PURCHASE
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("حذف تراکنش؟") },
            text = {
                Text("آیا از حذف تراکنش «${tx.itemName}» به مبلغ ${DateTimeUtils.formatToman(tx.amount)} اطمینان دارید؟")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    if (showBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = { Text("حذف گروهی تراکنش‌ها") },
            text = {
                Text("آیا از حذف دائم ${DateTimeUtils.toPersianDigits(selectedIds.size.toString())} تراکنش انتخاب شده مطمئن هستید؟ این عملیات قابل بازگشت نیست.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idsToDelete = selectedIds.toList()
                        selectedIds.clear()
                        onBulkDeleteTransactions(idsToDelete)
                        showBulkDeleteConfirm = false
                    }
                ) {
                    Text("حذف همه", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
