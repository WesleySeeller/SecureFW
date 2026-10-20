package com.wesley.securefw.presentation.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.wesley.securefw.presentation.viewmodel.AuthState
import com.wesley.securefw.presentation.viewmodel.AuthViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.wesley.securefw.presentation.ui.auth.auth.LoginScreen


class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var viewModel: AuthViewModel
    private val stateFlow = MutableStateFlow(AuthState())

    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)
        every { viewModel.state } returns stateFlow
    }

    // ---------- Teste 1: elementos da tela são exibidos ----------
    @Test
    fun loginScreen_exibeTituloCampoEmailSenhaEBotao() {
        composeRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {},
                onNavigateToHome = {}
            )
        }

        composeRule.onNodeWithText("Secure Finance").assertIsDisplayed()
        composeRule.onNodeWithText("E-mail").assertIsDisplayed()
        composeRule.onNodeWithText("Senha").assertIsDisplayed()
        composeRule.onNodeWithText("Entrar").assertIsDisplayed()
    }

    // ---------- Teste 2: digitação nos campos ----------
    @Test
    fun loginScreen_permiteDigitarEmailESenha() {
        composeRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {},
                onNavigateToHome = {}
            )
        }

        composeRule.onNodeWithText("E-mail").performTextInput("wesley@teste.com")
        composeRule.onNodeWithText("Senha").performTextInput("123456")

        composeRule.onNodeWithText("wesley@teste.com").assertIsDisplayed()
        composeRule.onNodeWithText("123456").assertIsDisplayed()
    }

    // ---------- Teste 3: clicar em Entrar chama signIn ----------
    @Test
    fun loginScreen_aoClicarEntrar_chamaSignIn() {
        composeRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {},
                onNavigateToHome = {}
            )
        }

        composeRule.onNodeWithText("E-mail").performTextInput("a@b.com")
        composeRule.onNodeWithText("Senha").performTextInput("senha123")
        composeRule.onNodeWithText("Entrar").performClick()

        verify(exactly = 1) { viewModel.signIn("a@b.com", "senha123") }
    }

    // ---------- Teste 4: erro é exibido quando state.error != null ----------
    @Test
    fun loginScreen_exibeMensagemDeErro() {
        stateFlow.value = AuthState(error = "Senha incorreta")

        composeRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {},
                onNavigateToHome = {}
            )
        }

        composeRule.onNodeWithText("Senha incorreta").assertIsDisplayed()
    }

    // ---------- Teste 5: clicar em "Cadastre-se" navega ----------
    @Test
    fun loginScreen_aoClicarCadastreSe_chamaNavegacao() {
        var navegou = false
        composeRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navegou = true },
                onNavigateToHome = {}
            )
        }

        composeRule.onNodeWithText("Não tem uma conta? Cadastre-se").performClick()

        assert(navegou)
    }
}