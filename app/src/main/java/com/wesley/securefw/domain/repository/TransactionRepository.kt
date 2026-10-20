package com.wesley.securefw.domain.repository

import com.wesley.securefw.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // Retorna um Flow para observar as transações em tempo real
    fun getTransactions(userId: String): Flow<List<TransactionEntity>>

    suspend fun addTransaction(transaction: TransactionEntity): Result<String>

    suspend fun updateTransaction(transaction: TransactionEntity): Result<Unit>

    suspend fun deleteTransaction(transactionId: String): Result<Unit>

    // Sincroniza dados locais com o Firestore
    suspend fun syncTransactions(userId: String): Result<Unit>
}