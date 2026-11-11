package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.AddEditTransactionBottomSheet
import com.example.ui.screens.CardsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ItemsScreen
import com.example.ui.viewmodel.SpendTrackViewModel
import kotlinx.coroutines.launch

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HISTORY("تاریخچه", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_tab_history"),
    ITEMS("اقلام", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, "nav_tab_items"),
    CARDS("کارت‌ها", Icons.Filled.CreditCard, Icons.Outlined.CreditCard, "nav_tab_cards")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendTrackApp(
    viewModel: SpendTrackViewModel,
    modifier: Modifier = Modifier
) {
    // Provide RTL layout direction for entire Persian UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var currentTab by remember { mutableStateOf(AppTab.HISTORY) }

        // State for Add / Edit bottom sheet
        var showSheet by remember { mutableStateOf(false) }
        var sheetInitialType by remember { mutableStateOf(TransactionType.PURCHASE) }
        var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

        val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
        val rawTransactions by viewModel.transactions.collectAsStateWithLifecycle()
        val items by viewModel.items.collectAsStateWithLifecycle()
        val cards by viewModel.cards.collectAsStateWithLifecycle()
        val defaultCard by viewModel.defaultCard.collectAsStateWithLifecycle()
        val filter by viewModel.filter.collectAsStateWithLifecycle()

        val totalPurchases by viewModel.totalPurchases.collectAsStateWithLifecycle()
        val totalSettlements by viewModel.totalSettlements.collectAsStateWithLifecycle()
        val netTotal by viewModel.netTotal.collectAsStateWithLifecycle()

        val filteredPurchasesTotal by viewModel.filteredPurchasesTotal.collectAsStateWithLifecycle()
        val filteredSettlementsTotal by viewModel.filteredSettlementsTotal.collectAsStateWithLifecycle()
        val filteredNetTotal by viewModel.filteredNetTotal.collectAsStateWithLifecycle()

        val spendingByCard by viewModel.spendingByCard.collectAsStateWithLifecycle()

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()

        if (currentTab != AppTab.HISTORY) {
            BackHandler {
                currentTab = AppTab.HISTORY
            }
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    AppTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.HISTORY -> {
                        HistoryScreen(
                            transactions = filteredTransactions,
                            cards = cards,
                            itemsList = items,
                            currentFilter = filter,
                            totalPurchases = totalPurchases,
                            totalSettlements = totalSettlements,
                            netTotal = netTotal,
                            filteredPurchasesTotal = filteredPurchasesTotal,
                            filteredSettlementsTotal = filteredSettlementsTotal,
                            filteredNetTotal = filteredNetTotal,
                            spendingByCard = spendingByCard,
                            onAddPurchaseClick = {
                                transactionToEdit = null
                                sheetInitialType = TransactionType.PURCHASE
                                showSheet = true
                            },
                            onAddSettlementClick = {
                                transactionToEdit = null
                                sheetInitialType = TransactionType.SETTLEMENT
                                showSheet = true
                            },
                            onEditTransaction = { tx ->
                                transactionToEdit = tx
                                sheetInitialType = tx.type
                                showSheet = true
                            },
                            onDeleteTransaction = { tx ->
                                viewModel.deleteTransaction(tx)
                            },
                            onBulkDeleteTransactions = { ids ->
                                viewModel.deleteTransactions(ids)
                            },
                            onUpdateFilter = { updatedFilter ->
                                viewModel.updateFilter(updatedFilter)
                            },
                            onClearFilter = {
                                viewModel.clearFilter()
                            }
                        )
                    }
                    AppTab.ITEMS -> {
                        ItemsScreen(
                            itemsList = items,
                            purchases = rawTransactions,
                            onAddItem = { name, icon -> viewModel.addItem(name, icon) },
                            onUpdateItem = { viewModel.updateItem(it) },
                            onDeleteItem = { viewModel.deleteItem(it) }
                        )
                    }
                    AppTab.CARDS -> {
                        CardsScreen(
                            cardsList = cards,
                            purchases = rawTransactions,
                            onAddCard = { name, isDefault, colorHex -> viewModel.addCard(name, isDefault, colorHex) },
                            onUpdateCard = { viewModel.updateCard(it) },
                            onDeleteCard = { viewModel.deleteCard(it) },
                            onSetDefaultCard = { viewModel.setDefaultCard(it) }
                        )
                    }
                }
            }
        }

        // Modal Bottom Sheet for Add or Edit Transaction
        if (showSheet) {
            AddEditTransactionBottomSheet(
                sheetState = sheetState,
                transactionToEdit = transactionToEdit,
                initialType = sheetInitialType,
                items = items,
                cards = cards,
                defaultCard = defaultCard,
                onDismissRequest = {
                    coroutineScope.launch {
                        sheetState.hide()
                        showSheet = false
                        transactionToEdit = null
                    }
                },
                onSaveTransaction = { type, itemName, amount, timestamp, cardName, note ->
                    val editing = transactionToEdit
                    if (editing != null) {
                        viewModel.updateTransaction(
                            editing.copy(
                                type = type,
                                itemName = itemName,
                                amount = amount,
                                timestamp = timestamp,
                                cardName = cardName,
                                note = note
                            )
                        )
                    } else {
                        viewModel.addTransaction(type, itemName, amount, timestamp, cardName, note)
                    }
                },
                onAddNewItem = { name ->
                    viewModel.addItem(name)
                },
                onSetDefaultCard = { cardId ->
                    viewModel.setDefaultCard(cardId)
                },
                onNavigateToItems = {
                    currentTab = AppTab.ITEMS
                },
                onNavigateToCards = {
                    currentTab = AppTab.CARDS
                }
            )
        }
    }
}
