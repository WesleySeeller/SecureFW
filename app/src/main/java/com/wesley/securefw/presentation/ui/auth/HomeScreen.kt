package com.wesley.securefw.presentation.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wesley.securefw.data.local.entity.TransactionEntity
import com.wesley.securefw.presentation.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String,
    onLogout: () -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    // Avisa o ViewModel qual é o usuário atual (só muda se o ID mudar)
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

// Coleta o StateFlow único do ViewModel
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Finance") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 👇 COLOQUE AQUI 👇
            if (uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.error?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("OK")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Card de saldo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Saldo atual", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = formatCurrency(balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Receitas: ${formatCurrency(totalIncome)}", color = Color(0xFF2E7D32))
                        Text("Despesas: ${formatCurrency(totalExpense)}", color = Color(0xFFC62828))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Transações", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma transação ainda. Toque no + para começar!")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(transactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onEditTransaction(transaction) },
                            onDelete = { viewModel.deleteTransaction(transaction.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, fontWeight = FontWeight.SemiBold)
                Text(transaction.category, style = MaterialTheme.typography.bodySmall)
                Text(
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(transaction.amount),
                    color = if (transaction.type == "INCOME") Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                }
            }
        }
    }
}

fun formatCurrency(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(value)
}