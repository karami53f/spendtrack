package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SpendTrackRepository
import com.example.data.model.CardEntity
import com.example.data.model.ItemEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HistoryFilter(
    val startDate: Long? = null,
    val endDate: Long? = null,
    val minAmount: Long? = null,
    val maxAmount: Long? = null,
    val itemName: String? = null,
    val type: TransactionType? = null
) {
    val isActive: Boolean
        get() = startDate != null || endDate != null || minAmount != null || maxAmount != null || !itemName.isNullOrBlank() || type != null

    val activeCount: Int
        get() {
            var count = 0
            if (startDate != null || endDate != null) count++
            if (minAmount != null || maxAmount != null) count++
            if (!itemName.isNullOrBlank()) count++
            if (type != null) count++
            return count
        }
}

class SpendTrackViewModel(
    application: Application,
    private val repository: SpendTrackRepository
) : AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            repository.ensureInitialSeedData()
        }
    }

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val items: StateFlow<List<ItemEntity>> = repository.allItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cards: StateFlow<List<CardEntity>> = repository.allCards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val defaultCard: StateFlow<CardEntity?> = repository.defaultCard
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _filter = MutableStateFlow(HistoryFilter())
    val filter: StateFlow<HistoryFilter> = _filter.asStateFlow()

    // Filtered transactions stream
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        _filter
    ) { txList, currentFilter ->
        txList.filter { tx ->
            val matchesStart = currentFilter.startDate?.let { start ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = start
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                tx.timestamp >= cal.timeInMillis
            } ?: true

            val matchesEnd = currentFilter.endDate?.let { end ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = end
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                tx.timestamp <= cal.timeInMillis
            } ?: true

            val matchesMin = currentFilter.minAmount?.let { tx.amount >= it } ?: true
            val matchesMax = currentFilter.maxAmount?.let { tx.amount <= it } ?: true
            val matchesItem = currentFilter.itemName?.let {
                it.isBlank() || tx.itemName.equals(it, ignoreCase = true)
            } ?: true
            val matchesType = currentFilter.type?.let { tx.type == it } ?: true

            matchesStart && matchesEnd && matchesMin && matchesMax && matchesItem && matchesType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered totals
    val filteredPurchasesTotal: StateFlow<Long> = filteredTransactions
        .map { list -> list.filter { it.type == TransactionType.PURCHASE }.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val filteredSettlementsTotal: StateFlow<Long> = filteredTransactions
        .map { list -> list.filter { it.type == TransactionType.SETTLEMENT }.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val filteredNetTotal: StateFlow<Long> = combine(filteredPurchasesTotal, filteredSettlementsTotal) { purchases, settlements ->
        purchases - settlements
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // Global summary metrics
    val totalPurchases: StateFlow<Long> = transactions
        .map { list -> list.filter { it.type == TransactionType.PURCHASE }.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val totalSettlements: StateFlow<Long> = transactions
        .map { list -> list.filter { it.type == TransactionType.SETTLEMENT }.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val netTotal: StateFlow<Long> = combine(totalPurchases, totalSettlements) { purchases, settlements ->
        purchases - settlements
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val spendingByCard: StateFlow<Map<String, Long>> = transactions
        .map { list ->
            list.groupBy { it.cardName }
                .mapValues { entry ->
                    entry.value.sumOf { tx ->
                        if (tx.type == TransactionType.PURCHASE) tx.amount else -tx.amount
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun updateFilter(filter: HistoryFilter) {
        _filter.value = filter
    }

    fun clearFilter() {
        _filter.value = HistoryFilter()
    }

    fun addTransaction(
        type: TransactionType,
        itemName: String,
        amount: Long,
        timestamp: Long,
        cardName: String,
        note: String = "",
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.addTransaction(type, itemName, amount, timestamp, cardName, note)
            onComplete()
        }
    }

    fun updateTransaction(transaction: TransactionEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            onComplete()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactions(ids: List<Long>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTransactions(ids)
            onComplete()
        }
    }

    fun addItem(name: String, iconName: String = "shopping_bag", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addItem(name, iconName)
            onComplete()
        }
    }

    fun updateItem(item: ItemEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateItem(item)
            onComplete()
        }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun addCard(name: String, isDefault: Boolean, colorHex: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addCard(name, isDefault, colorHex)
            onComplete()
        }
    }

    fun updateCard(card: CardEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCard(card)
            onComplete()
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    fun setDefaultCard(cardId: Long) {
        viewModelScope.launch {
            repository.setDefaultCard(cardId)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpendTrackViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val repository = SpendTrackRepository(
                    db.itemDao(),
                    db.cardDao(),
                    db.transactionDao()
                )
                return SpendTrackViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
