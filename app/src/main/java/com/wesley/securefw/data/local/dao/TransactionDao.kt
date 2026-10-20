package com.wesley.securefw.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wesley.securefw.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: String): Unit

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsByUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedTransactions(userId: String): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun clearAllTransactions(userId: String)

    @Query("SELECT id FROM transactions WHERE userId = :userId")
    suspend fun getAllIdsByUser(userId: String): List<String>
}