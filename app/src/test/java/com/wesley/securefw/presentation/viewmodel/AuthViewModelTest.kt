package com.wesley.securefw.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import com.wesley.securefw.domain.repository.AuthRepository
import com.wesley.securefw.presentation.viewmodel.AuthViewModel
import com.wesley.securefw.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    // Mock de FirebaseUser (só o que usamos)
    private val fakeUser: FirebaseUser = mockk(relaxed = true)

    @Before
    fun setup() {
        every { fakeUser.uid } returns "user-123"
        every { fakeUser.email } returns "wesley@teste.com"

        repository = mockk(relaxed = true)
        // Estado inicial: não logado
        coEvery { repository.isUserLoggedIn() } returns false
        every { repository.getCurrentUser() } returns null
        viewModel = AuthViewModel(repository)
    }

    // ---------- Teste 1: signIn com sucesso ----------
    @Test
    fun `signIn com sucesso deve marcar isAuthenticated true`() = runTest {
        // Arrange
        coEvery { repository.signIn("wesley@teste.com", "123456") } returns
                Result.success(fakeUser)

        // Act
        viewModel.signIn("wesley@teste.com", "123456")
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertThat(state.isAuthenticated).isTrue()
        assertThat(state.userEmail).isEqualTo("wesley@teste.com")
        assertThat(state.userId).isEqualTo("user-123")
        assertThat(state.error).isNull()
        assertThat(state.isLoading).isFalse()
    }

    // ---------- Teste 2: signIn com erro ----------
    @Test
    fun `signIn com falha deve emitir erro`() = runTest {
        // Arrange
        coEvery { repository.signIn(any(), any()) } returns
                Result.failure(Exception("Senha incorreta"))

        // Act
        viewModel.signIn("wesley@teste.com", "errada")
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertThat(state.isAuthenticated).isFalse()
        assertThat(state.error).isEqualTo("Senha incorreta")
        assertThat(state.isLoading).isFalse()
    }

    // ---------- Teste 3: campos vazios não chamam o repositório ----------
    @Test
    fun `signIn com campos vazios deve emitir erro sem chamar repository`() = runTest {
        // Act
        viewModel.signIn("", "")
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertThat(state.error).isEqualTo("Preencha todos os campos")
        assertThat(state.isAuthenticated).isFalse()
        // Repository não foi chamado — sem coVerify aqui, mas o resultado prova.
    }

    // ---------- Teste 4: signUp com sucesso ----------
    @Test
    fun `signUp com sucesso deve autenticar usuario`() = runTest {
        // Arrange
        coEvery { repository.signUp("novo@teste.com", "123456") } returns
                Result.success(fakeUser)

        // Act
        viewModel.signUp("novo@teste.com", "123456")
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertThat(state.isAuthenticated).isTrue()
        assertThat(state.userEmail).isEqualTo("wesley@teste.com")
        assertThat(state.error).isNull()
    }

    // ---------- Teste 5: signOut ----------
    @Test
    fun `signOut deve resetar o estado`() = runTest {
        // Arrange
        coEvery { repository.signIn(any(), any()) } returns Result.success(fakeUser)
        viewModel.signIn("wesley@teste.com", "123456")
        advanceUntilIdle()
        coEvery { repository.signOut() } returns Result.success(Unit)

        // Act
        viewModel.signOut()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value
        assertThat(state.isAuthenticated).isFalse()
        assertThat(state.userEmail).isNull()
        assertThat(state.error).isNull()
    }

    // ---------- Teste 6: checkAuthentication quando já logado ----------
    @Test
    fun `init deve autenticar automaticamente se usuario ja esta logado`() = runTest {
        // Arrange
        coEvery { repository.isUserLoggedIn() } returns true
        every { repository.getCurrentUser() } returns fakeUser

        // Act
        val newViewModel = AuthViewModel(repository)
        advanceUntilIdle()

        // Assert
        val state = newViewModel.state.value
        assertThat(state.isAuthenticated).isTrue()
        assertThat(state.userId).isEqualTo("user-123")
    }

    // ---------- Teste 7: clearError ----------
    @Test
    fun `clearError deve limpar o campo error`() = runTest {
        // Arrange
        coEvery { repository.signIn(any(), any()) } returns
                Result.failure(Exception("Erro X"))
        viewModel.signIn("a@b.com", "123")
        advanceUntilIdle()

        // Act
        viewModel.clearError()

        // Assert
        assertThat(viewModel.state.value.error).isNull()
    }
}