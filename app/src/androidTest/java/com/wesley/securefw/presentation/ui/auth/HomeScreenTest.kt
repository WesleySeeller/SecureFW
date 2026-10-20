package com.wesley.securefw.presentation.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.wesley.securefw.data.local.entity.TransactionEntity
import com.wesley.securefw.presentation.viewmodel.TransactionUiState
import com.wesley.securefw.presentation.viewmodel.TransactionViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var viewModel: TransactionViewModel
    private val transactionsFlow = MutableStateFlow<List<TransactionEntity>>(emptyList())
    private val uiStateFlow = MutableStateFlow(TransactionUiState())

    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)
        every { viewModel.transactions } returns transactionsFlow
        every { viewModel.uiState } returns uiStateFlow
    }

    // ---------- Teste 1: elementos principais ----------
    @Test
    fun homeScreen_exibeSaldoETransacoes() {
        composeRule.setContent {
            HomeScreen(
                userId = "user-1",
                onLogout = {},
                onAddTransaction = {},
                onEditTransaction = {},
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithText("Secure Finance").assertIsDisplayed()
        composeRule.onNodeWithText("Saldo atual").assertIsDisplayed()
        composeRule.onNodeWithText("Transações").assertIsDisplayed()
    }

    // ---------- Teste 2: lista vazia mostra mensagem ----------
    @Test
    fun homeScreen_listaVaziaMostraMensagem() {
        transactionsFlow.value = emptyList()

        composeRule.setContent {
            HomeScreen(
                userId = "user-1",
                onLogout = {},
                onAddTransaction = {},
                onEditTransaction = {},
                viewModel = viewModel
            )
        }

        composeRule
            .onNodeWithText("Nenhuma transação ainda. Toque no + para começar!")
            .assertIsDisplayed()
    }

    // ---------- Teste 3: com transações, exibe itens ----------
    @Test
    fun homeScreen_exibeTransacoesNaLista() {
        transactionsFlow.value = listOf(
            TransactionEntity(
                id = "1", title = "Almoço", amount = 25.0,
                category = "Alimentação", date = System.currentTimeMillis(),
                type = "EXPENSE", userId = "user-1", isSynced = true
            ),
            TransactionEntity(
                id = "2", title = "Salário", amount = 5000.0,
                category = "Salário", date = System.currentTimeMillis(),
                type = "INCOME", userId = "user-1", isSynced = true
            )
        )

        composeRule.setContent {
            HomeScreen(
                userId = "user-1",
                onLogout = {},
                onAddTransaction = {},
                onEditTransaction = {},
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithText("Almoço").assertIsDisplayed()
        composeRule.onNodeWithText("Salário").assertIsDisplayed()
    }

    // ---------- Teste 4: FAB chama onAddTransaction ----------
    @Test
    fun homeScreen_aoClicarFAB_chamaNavegacao() {
        var clicou = false
        composeRule.setContent {
            HomeScreen(
                userId = "user-1",
                onLogout = {},
                onAddTransaction = { clicou = true },
                onEditTransaction = {},
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithContentDescription("Adicionar").performClick()
        assert(clicou)
    }

    // ---------- Teste 5: ícone de logout chama onLogout ----------
    @Test
    fun homeScreen_aoClicarSair_chamaLogout() {
        var clicou = false
        composeRule.setContent {
            HomeScreen(
                userId = "user-1",
                onLogout = { clicou = true },
                onAddTransaction = {},
                onEditTransaction = {},
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithContentDescription("Sair").performClick()
        assert(clicou)
    }

    // ---------- Teste 6: excluir transação chama delete ----------
    @Test
    fun homeScreen_aoClicarExcluir_chamaDelete() {
        transactionsFlow.value = listOf(
            TransactionEntity(
                id = "tx-1", title = "Cinema", amount = 50.0,
                category = "Lazer", date = System.currentTimeMillis(),
                type = "EXPENSE", userId = "user-1", isSynced = true
            )
        )

        composeRule.setContent {
            HomeScreen(
                userId = "user-1",
                onLogout = {},
                onAddTransaction = {},
                onEditTransaction = {},
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithContentDescription("Excluir").performClick()
        verify(exactly = 1) { viewModel.deleteTransaction("tx-1") }
    }
}