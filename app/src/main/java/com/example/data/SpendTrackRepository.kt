package com.example.data

import com.example.data.dao.CardDao
import com.example.data.dao.ItemDao
import com.example.data.dao.TransactionDao
import com.example.data.model.CardEntity
import com.example.data.model.ItemEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SpendTrackRepository(
    private val itemDao: ItemDao,
    private val cardDao: CardDao,
    private val transactionDao: TransactionDao
) {
    val allItems: Flow<List<ItemEntity>> = itemDao.getAllItems()
    val allCards: Flow<List<CardEntity>> = cardDao.getAllCards()
    val defaultCard: Flow<CardEntity?> = cardDao.getDefaultCard()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun addItem(name: String, iconName: String = "shopping_bag"): Long =
        withContext(Dispatchers.IO) {
            itemDao.insertItem(ItemEntity(name = name.trim(), iconName = iconName))
        }

    suspend fun updateItem(item: ItemEntity) = withContext(Dispatchers.IO) {
        itemDao.updateItem(item)
    }

    suspend fun deleteItem(item: ItemEntity) = withContext(Dispatchers.IO) {
        itemDao.deleteItem(item)
    }

    suspend fun addCard(name: String, isDefault: Boolean, colorHex: Long): Long =
        withContext(Dispatchers.IO) {
            if (isDefault) {
                cardDao.clearAllDefaults()
            }
            cardDao.insertCard(CardEntity(name = name.trim(), isDefault = isDefault, colorHex = colorHex))
        }

    suspend fun updateCard(card: CardEntity) = withContext(Dispatchers.IO) {
        if (card.isDefault) {
            cardDao.clearAllDefaults()
        }
        cardDao.updateCard(card)
    }

    suspend fun deleteCard(card: CardEntity) = withContext(Dispatchers.IO) {
        cardDao.deleteCard(card)
    }

    suspend fun setDefaultCard(cardId: Long) = withContext(Dispatchers.IO) {
        cardDao.setDefaultCard(cardId)
    }

    suspend fun addTransaction(
        type: TransactionType,
        itemName: String,
        amount: Long,
        timestamp: Long,
        cardName: String,
        note: String = ""
    ): Long = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(
            TransactionEntity(
                type = type,
                itemName = itemName.trim(),
                amount = amount,
                timestamp = timestamp,
                cardName = cardName.trim(),
                note = note.trim()
            )
        )
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactions(ids: List<Long>) = withContext(Dispatchers.IO) {
        if (ids.isNotEmpty()) {
            transactionDao.deleteTransactionsByIds(ids)
        }
    }

    suspend fun ensureInitialSeedData() = withContext(Dispatchers.IO) {
        if (itemDao.countItems() == 0) {
            val defaultItems = listOf(
                ItemEntity(name = "بنزین", iconName = "local_gas_station"),
                ItemEntity(name = "وسایل پیتزا", iconName = "local_pizza"),
                ItemEntity(name = "هزینه‌های ماشین", iconName = "build")
            )
            itemDao.insertItems(defaultItems)
        }

        if (cardDao.countCards() == 0) {
            val defaultCards = listOf(
                CardEntity(name = "پول نقد", isDefault = true, colorHex = 0xFF0A6E51),
                CardEntity(name = "بانک ملت", isDefault = false, colorHex = 0xFFC2185B),
                CardEntity(name = "بانک ملی", isDefault = false, colorHex = 0xFF1976D2),
                CardEntity(name = "بانک سامان", isDefault = false, colorHex = 0xFF7B1FA2)
            )
            cardDao.insertCards(defaultCards)
        }

        if (transactionDao.countTransactions() == 0) {
            val now = System.currentTimeMillis()
            val initialTransactions = listOf(
                TransactionEntity(
                    type = TransactionType.PURCHASE,
                    itemName = "بنزین",
                    amount = 60000L,
                    timestamp = now - (1000 * 60 * 75),
                    cardName = "پول نقد",
                    note = "باک بنزین هفتگی"
                ),
                TransactionEntity(
                    type = TransactionType.PURCHASE,
                    itemName = "وسایل پیتزا",
                    amount = 285000L,
                    timestamp = now - (1000 * 60 * 60 * 8),
                    cardName = "بانک ملت",
                    note = "پنیر پیتزا و مخلفات"
                ),
                TransactionEntity(
                    type = TransactionType.PURCHASE,
                    itemName = "هزینه‌های ماشین",
                    amount = 450000L,
                    timestamp = now - (1000 * 60 * 60 * 24),
                    cardName = "بانک سامان",
                    note = "تعویض روغن و سرویس"
                ),
                TransactionEntity(
                    type = TransactionType.SETTLEMENT,
                    itemName = "هزینه‌های ماشین",
                    amount = 150000L,
                    timestamp = now - (1000 * 60 * 60 * 48),
                    cardName = "بانک ملی",
                    note = "تسویه سهم همسفر"
                )
            )
            for (tx in initialTransactions) {
                transactionDao.insertTransaction(tx)
            }
        }
    }
}
