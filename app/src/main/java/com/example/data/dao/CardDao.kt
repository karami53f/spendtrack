package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY isDefault DESC, name ASC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isDefault = 1 LIMIT 1")
    fun getDefaultCard(): Flow<CardEntity?>

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun countCards(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("UPDATE cards SET isDefault = 0")
    suspend fun clearAllDefaults()

    @Query("UPDATE cards SET isDefault = 1 WHERE id = :cardId")
    suspend fun markCardAsDefault(cardId: Long)

    @Transaction
    suspend fun setDefaultCard(cardId: Long) {
        clearAllDefaults()
        markCardAsDefault(cardId)
    }
}
