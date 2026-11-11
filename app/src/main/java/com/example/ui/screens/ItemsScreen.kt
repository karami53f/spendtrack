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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ItemEntity
import com.example.data.model.TransactionEntity
import com.example.ui.util.DateTimeUtils

@Composable
fun ItemsScreen(
    itemsList: List<ItemEntity>,
    purchases: List<TransactionEntity>,
    onAddItem: (name: String, iconName: String) -> Unit,
    onUpdateItem: (ItemEntity) -> Unit,
    onDeleteItem: (ItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<ItemEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("افزودن قلم", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_item_button")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            text = "مدیریت اقلام",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "فهرست اقلام جهت ثبت سریع خرید و تسویه",
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
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Info Card
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
                            text = "اقلام پیش‌فرض شامل بنزین، وسایل پیتزا و هزینه‌های ماشین هستند. این اقلام در لیست انتخابی ثبت تراکنش نمایش داده می‌شوند و می‌توانید اقلام جدید را نیز اضافه کنید.",
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
                        text = "اقلام تعریف‌شده",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${DateTimeUtils.toPersianDigits(itemsList.size.toString())} قلم",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty state
            if (itemsList.isEmpty()) {
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
                                text = "هنوز قلمی ثبت نشده است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "برای دسترسی سریع در فرم ثبت تراکنش، اقلامی مانند بنزین، وسایل پیتزا یا تعمیرات ماشین را اضافه کنید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(onClick = { showAddDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("افزودن اولین قلم")
                            }
                        }
                    }
                }
            } else {
                items(itemsList, key = { it.id }) { item ->
                    val usageCount = purchases.count { it.itemName.equals(item.name, ignoreCase = true) }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                RoundedCornerShape(16.dp)
                            )
                            .testTag("catalog_item_${item.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = DateTimeUtils.getIconForName(item.iconName),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (usageCount == 0) "هنوز در تراکنشی استفاده نشده" else "استفاده شده در ${DateTimeUtils.toPersianDigits(usageCount.toString())} تراکنش",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Edit Button
                            IconButton(
                                onClick = { itemToEdit = item },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "ویرایش قلم",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete Button
                            IconButton(
                                onClick = { itemToDelete = item },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف قلم",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
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

    // Add Item Dialog
    if (showAddDialog) {
        ItemFormDialog(
            title = "افزودن قلم جدید",
            initialName = "",
            initialIcon = "shopping_bag",
            confirmButtonText = "افزودن قلم",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, icon ->
                onAddItem(name, icon)
                showAddDialog = false
            }
        )
    }

    // Edit Item Dialog
    itemToEdit?.let { item ->
        ItemFormDialog(
            title = "ویرایش قلم",
            initialName = item.name,
            initialIcon = item.iconName,
            confirmButtonText = "ذخیره تغییرات",
            onDismiss = { itemToEdit = null },
            onConfirm = { updatedName, updatedIcon ->
                onUpdateItem(item.copy(name = updatedName, iconName = updatedIcon))
                itemToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        val usageCount = purchases.count { it.itemName.equals(item.name, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("حذف قلم «${item.name}»؟") },
            text = {
                if (usageCount > 0) {
                    Text("این قلم در ${DateTimeUtils.toPersianDigits(usageCount.toString())} تراکنش استفاده شده است. سوابق قبلی حفظ خواهند شد اما این قلم از فهرست انتخابی حذف می‌گردد.")
                } else {
                    Text("آیا از حذف قلم «${item.name}» از فهرست مطمئن هستید؟")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteItem(item)
                        itemToDelete = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun ItemFormDialog(
    title: String,
    initialName: String,
    initialIcon: String,
    confirmButtonText: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconName: String) -> Unit
) {
    var itemName by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }

    val iconOptions = listOf(
        "local_gas_station" to "بنزین/سوخت",
        "local_pizza" to "وسایل پیتزا",
        "build" to "تعمیرات ماشین",
        "shopping_cart" to "سوپرمارکت",
        "restaurant" to "غذا/نان",
        "local_cafe" to "کافه/لبنیات",
        "fastfood" to "ناهار/فست‌فود",
        "wifi" to "اینترنت",
        "payments" to "تسویه/مالی",
        "shopping_bag" to "خرید عمومی"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("نام قلم") },
                    placeholder = { Text("مثلاً: بنزین، وسایل پیتزا، هزینه‌های ماشین") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("item_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "انتخاب آیکون مرتبط",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconOptions) { (iconKey, label) ->
                        val isSelected = selectedIcon == iconKey
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .clickable { selectedIcon = iconKey }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = DateTimeUtils.getIconForName(iconKey),
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isNotBlank()) {
                        onConfirm(itemName.trim(), selectedIcon)
                    }
                },
                enabled = itemName.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_item_dialog_button")
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
