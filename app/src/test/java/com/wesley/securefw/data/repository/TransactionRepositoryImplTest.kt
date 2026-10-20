package com.wesley.securefw.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.wesley.securefw.data.local.dao.TransactionDao
import com.wesley.securefw.data.local.database.AppDatabase
import com.wesley.securefw.data.local.entity.TransactionEntity
import com.wesley.securefw.data.remote.model.FirestoreService
import com.wesley.securefw.data.remote.model.TransactionModel
import com.wesley.securefw.data.repository.TransactionRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class TransactionRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TransactionDao
    private lateinit var firestore: FirestoreService
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setup() {
        // Contexto real (Robolectric)
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Banco de dados em memória (não persiste entre testes)
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = database.transactionDao()
        firestore = mockk(relaxed = true)
        repository = TransactionRepositoryImpl(dao, firestore)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ---------- Teste 1: addTransaction salva localmente ----------
    @Test
    fun `addTransaction deve salvar localmente com isSynced false`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = "",
            title = "Almoço",
            amount = 25.0,
            category = "Alimentação",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            userId = "user-1",
            isSynced = false
        )

        // Act
        val result = repository.addTransaction(transaction)

        // Assert
        assertThat(result.isSuccess).isTrue()
        val saved = dao.getTransactionsByUser("user-1").first()
        assertThat(saved).hasSize(1)
        assertThat(saved[0].title).isEqualTo("Almoço")
        assertThat(saved[0].isSynced).isFalse()
    }

    // ---------- Teste 2: syncTransactions sobe as não sincronizadas ----------
    @Test
    fun `syncTransactions deve subir local e marcar como sincronizada`() = runTest {
        // Arrange
        val local = TransactionEntity(
            id = "tx-1",
            title = "Café",
            amount = 5.0,
            category = "Alimentação",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            userId = "user-1",
            isSynced = false
        )
        dao.insertTransaction(local)
        coEvery { firestore.addTransaction(any()) } returns "tx-1"
        coEvery { firestore.getTransactionsByUser("user-1") } returns emptyList()

        // Act
        val result = repository.syncTransactions("user-1")

        // Assert
        result.exceptionOrNull()?.printStackTrace()
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { firestore.addTransaction(any()) }
        val updated = dao.getTransactionsByUser("user-1").first()
        assertThat(updated[0].isSynced).isTrue()
    }

    // ---------- Teste 3: syncTransactions baixa do Firestore ----------
    @Test
    fun `syncTransactions deve baixar transacoes remotas`() = runTest {
        // Arrange
        val remoteModel = TransactionModel(
            id = "remote-1",
            title = "Salário",
            amount = 5000.0,
            category = "Salário",
            date = Date(),
            type = "INCOME",
            userId = "user-1"
        )
        coEvery { firestore.getTransactionsByUser("user-1") } returns listOf(remoteModel)

        // Act
        val result = repository.syncTransactions("user-1")

        // Assert
        result.exceptionOrNull()?.printStackTrace()
        assertThat(result.isSuccess).isTrue()
        val local = dao.getTransactionsByUser("user-1").first()
        assertThat(local).hasSize(1)
        assertThat(local[0].id).isEqualTo("remote-1")
        assertThat(local[0].isSynced).isTrue()
    }

    // ---------- Teste 4: syncTransactions não duplica ----------
    @Test
    fun `syncTransactions nao deve duplicar transacoes ja existentes`() = runTest {
        // Arrange
        val existing = TransactionEntity(
            id = "dup-1",
            title = "Já existe",
            amount = 10.0,
            category = "Outros",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            userId = "user-1",
            isSynced = true
        )
        dao.insertTransaction(existing)

        val remoteModel = TransactionModel(
            id = "dup-1",
            title = "Já existe",
            amount = 10.0,
            category = "Outros",
            date = Date(),
            type = "EXPENSE",
            userId = "user-1"
        )
        coEvery { firestore.getTransactionsByUser("user-1") } returns listOf(remoteModel)

        // Act
        repository.syncTransactions("user-1")

        // Assert
        val all = dao.getTransactionsByUser("user-1").first()
        assertThat(all).hasSize(1) // Continua com 1, não duplicou
    }

    // ---------- Teste 5: deleteTransaction remove do banco e do Firestore ----------
    @Test
    fun `deleteTransaction deve remover do banco e do Firestore`() = runTest {
        // Arrange
        val transaction = TransactionEntity(
            id = "del-1",
            title = "Deletar",
            amount = 15.0,
            category = "Outros",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            userId = "user-1",
            isSynced = true
        )
        dao.insertTransaction(transaction)
        coEvery { firestore.deleteTransaction("del-1") } returns true

        // Act
        val result = repository.deleteTransaction("del-1")

        // Assert
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { firestore.deleteTransaction("del-1") }
        val all = dao.getTransactionsByUser("user-1").first()
        assertThat(all).isEmpty()
    }

    // ---------- Teste 6: updateTransaction marca como não sincronizada ----------
    @Test
    fun `updateTransaction deve marcar isSynced como false`() = runTest {
        // Arrange
        val original = TransactionEntity(
            id = "upd-1",
            title = "Original",
            amount = 100.0,
            category = "Outros",
            date = System.currentTimeMillis(),
            type = "EXPENSE",
            userId = "user-1",
            isSynced = true
        )
        dao.insertTransaction(original)

        val updated = original.copy(title = "Atualizado", amount = 200.0)

        // Act
        repository.updateTransaction(updated)

        // Assert
        val all = dao.getTransactionsByUser("user-1").first()
        assertThat(all[0].title).isEqualTo("Atualizado")
        assertThat(all[0].isSynced).isFalse()
    }
}