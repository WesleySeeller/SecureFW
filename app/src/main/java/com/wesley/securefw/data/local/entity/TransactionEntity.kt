package com.wesley.securefw.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String = "", // Usaremos o ID do Firestore ou UUID
    val title: String,
    val amount: Double,
    val category: String, // Ex: "Alimentação", "Transporte"
    val date: Long, // Timestamp em milissegundos
    val type: String, // "INCOME" ou "EXPENSE"
    val userId: String, // Para associar ao usuário logado
    val isSynced: Boolean = false // Marcador para saber se já subiu pro Firestore
)