package com.wesley.securefw.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wesley.securefw.data.local.entity.TransactionEntity
import com.wesley.securefw.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TransactionUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    // Guarda o userId atual. Quando ele muda, o Flow abaixo troca de fonte.
    private val _userId = MutableStateFlow<String?>(null)

    // Um único StateFlow que reage ao userId. Nunca é recriado.
    val transactions: StateFlow<List<TransactionEntity>> = _userId
        .flatMapLatest { userId ->
            if (userId.isNullOrBlank()) flowOf(emptyList())
            else repository.getTransactions(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Chamado pela tela quando o userId estiver disponível
    fun setUserId(userId: String) {
        if (_userId.value != userId) {
            _userId.value = userId
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSyncing = true)
                val result = repository.syncTransactions(userId)
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun addTransaction(
        userId: String,
        title: String,
        amount: Double,
        category: String,
        type: String,
        date: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val transaction = TransactionEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                amount = amount,
                category = category,
                date = date,
                type = type,
                userId = userId,
                isSynced = false
            )
            repository.addTransaction(transaction)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Transação adicionada!"
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Erro ao adicionar transação"
                    )
                }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.updateTransaction(transaction)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Transação atualizada!"
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Erro ao atualizar transação"
                    )
                }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Transação excluída!")
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message ?: "Erro ao excluir transação"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}