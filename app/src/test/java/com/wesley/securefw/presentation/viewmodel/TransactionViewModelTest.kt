package com.wesley.securefw.presentation.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.wesley.securefw.data.local.entity.TransactionEntity
import com.wesley.securefw.domain.repository.TransactionRepository
import com.wesley.securefw.presentation.viewmodel.TransactionViewModel
import com.wesley.securefw.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TransactionRepository
    private lateinit var viewModel: TransactionViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        viewModel = TransactionViewModel(repository)
    }

    // ---------- Teste 1: addTransaction chama o repositório ----------
    @Test
    fun `addTransaction deve chamar repository e emitir successMessage`() = runTest {
        // Arrange
        val userId = "user-123"
        coEvery { repository.addTransaction(any()) } returns Result.success("id-1")

        // Act
        viewModel.addTransaction(
            userId = userId,
            title = "Almoço",
            amount = 25.0,
            category = "Alimentação",
            type = "EXPENSE"
        )
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.addTransaction(any()) }
        assertThat(viewModel.uiState.value.successMessage).isEqualTo("Transação adicionada!")
        assertThat(viewModel.uiState.value.error).isNull()
    }

    // ---------- Teste 2: addTransaction com erro ----------
    @Test
    fun `addTransaction deve emitir erro quando repositório falha`() = runTest {
        // Arrange
        coEvery { repository.addTransaction(any()) } returns
                Result.failure(Exception("Falha na rede"))

        // Act
        viewModel.addTransaction(
            userId = "user-123",
            title = "Almoço",
            amount = 25.0,
            category = "Alimentação",
            type = "EXPENSE"
        )
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.value.error).isEqualTo("Falha na rede")
        assertThat(viewModel.uiState.value.successMessage).isNull()
    }

    // ---------- Teste 3: deleteTransaction ----------
    @Test
    fun `deleteTransaction deve chamar repository e emitir mensagem`() = runTest {
        // Arrange
        coEvery { repository.deleteTransaction(any()) } returns Result.success(Unit)

        // Act
        viewModel.deleteTransaction("transaction-1")
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.deleteTransaction("transaction-1") }
        assertThat(viewModel.uiState.value.successMessage).isEqualTo("Transação excluída!")
    }

    // ---------- Teste 4: setUserId dispara sincronização ----------
    @Test
    fun `setUserId deve chamar syncTransactions`() = runTest {
        // Arrange
        coEvery { repository.syncTransactions(any()) } returns Result.success(Unit)

        // Act
        viewModel.setUserId("user-123")
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.syncTransactions("user-123") }
    }

    // ---------- Teste 5: transactions emite lista vazia se userId é nulo ----------
    @Test
    fun `transactions deve emitir lista vazia quando userId eh nulo`() = runTest {
        viewModel.transactions.test {
            val first = awaitItem()
            assertThat(first).isEmpty()
        }
    }

    // ---------- Teste 6: clearMessages limpa os estados ----------
    @Test
    fun `clearMessages deve limpar error e successMessage`() = runTest {
        // Arrange
        coEvery { repository.addTransaction(any()) } returns
                Result.failure(Exception("Erro qualquer"))
        viewModel.addTransaction("user-1", "Teste", 10.0, "Outros", "EXPENSE")
        advanceUntilIdle()

        // Act
        viewModel.clearMessages()

        // Assert
        assertThat(viewModel.uiState.value.error).isNull()
        assertThat(viewModel.uiState.value.successMessage).isNull()
    }
}