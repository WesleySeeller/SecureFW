package com.wesley.securefw.data.remote.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.wesley.securefw.data.remote.model.TransactionModel
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("transactions")

    // Buscar todas as transações de um usuário
    suspend fun getTransactionsByUser(userId: String): List<TransactionModel> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .orderBy("date")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject<TransactionModel>()?.apply {
                    id = doc.id // Garante que o ID do documento seja usado
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Adicionar uma transação
    suspend fun addTransaction(transaction: TransactionModel): String? {
        return try {
            // Usa o próprio ID da transação como ID do documento
            collection.document(transaction.id)
                .set(transaction)
                .await()
            transaction.id
        } catch (e: Exception) {
            null
        }
    }

    // Atualizar uma transação
    suspend fun updateTransaction(transaction: TransactionModel): Boolean {
        return try {
            collection.document(transaction.id)
                .set(transaction)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Deletar uma transação
    suspend fun deleteTransaction(transactionId: String): Boolean {
        return try {
            collection.document(transactionId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
