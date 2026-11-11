package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class TransactionType {
    PURCHASE,
    SETTLEMENT
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType = TransactionType.PURCHASE,
    val itemName: String,
    val amount: Long, // Amounts in Iranian Toman as whole numbers
    val timestamp: Long = System.currentTimeMillis(),
    val cardName: String,
    val note: String = "" // Optional note / description
)

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = try {
        TransactionType.valueOf(value)
    } catch (e: Exception) {
        TransactionType.PURCHASE
    }
}
