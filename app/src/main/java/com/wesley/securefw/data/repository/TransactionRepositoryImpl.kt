package com.wesley.securefw.data.repository

import com.wesley.securefw.data.local.dao.TransactionDao
import com.wesley.securefw.data.local.entity.TransactionEntity
import com.wesley.securefw.data.remote.model.FirestoreService
import com.wesley.securefw.data.remote.model.mapper.toEntity
import com.wesley.securefw.data.remote.model.mapper.toModel
import com.wesley.securefw.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val firestoreService: FirestoreService
) : TransactionRepository {

    override fun getTransactions(userId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByUser(userId)
    }

    override suspend fun addTransaction(transaction: TransactionEntity): Result<String> {
        return try {
            val newId = transaction.id.ifEmpty { UUID.randomUUID().toString() }
            val entityToSave = transaction.copy(id = newId, isSynced = false)
            transactionDao.insertTransaction(entityToSave)
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(transaction: TransactionEntity): Result<Unit> {
        return try {
            // Sempre salva localmente com isSynced = false
            // A sincronização vai subir depois
            transactionDao.updateTransaction(transaction.copy(isSynced = false))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            // Deleta local
            transactionDao.deleteTransaction(transactionId)
            // Tenta deletar no Firestore (best effort)
            firestoreService.deleteTransaction(transactionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTransactions(userId: String): Result<Unit> {
        return try {
            // ---------- 1. SUBIR as transações locais não sincronizadas ----------
            val unsynced = transactionDao.getUnsyncedTransactions(userId)
            for (entity in unsynced) {
                val success = firestoreService.addTransaction(entity.toModel())
                if (success != null) {
                    transactionDao.updateTransaction(entity.copy(isSynced = true))
                }
            }

            // ---------- 2. BAIXAR as transações remotas que não temos localmente ----------
            val remote = firestoreService.getTransactionsByUser(userId)
            val localIds = transactionDao.getAllIdsByUser(userId)
            for (model in remote) {
                if (!localIds.contains(model.id)) {
                    transactionDao.insertTransaction(model.toEntity())
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}