package com.wesley.securefw.presentation.ui.auth.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wesley.securefw.data.local.entity.TransactionEntity
import com.wesley.securefw.presentation.viewmodel.TransactionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    userId: String,
    transaction: TransactionEntity? = null,
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf(transaction?.title ?: "") }
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(transaction?.category ?: "") }
    var type by remember { mutableStateOf(transaction?.type ?: "EXPENSE") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    val isEditing = transaction != null
    val categories = listOf("Alimentação", "Transporte", "Lazer", "Moradia", "Saúde", "Salário", "Outros")

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            viewModel.clearMessages()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Transação" else "Nova Transação") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tipo
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = type == "EXPENSE",
                    onClick = { type = "EXPENSE" },
                    label = { Text("Despesa") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = type == "INCOME",
                    onClick = { type = "INCOME" },
                    label = { Text("Receita") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null   // limpa o erro ao digitar
                },
                label = { Text("Título") },
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    // Aceita só números e um ponto decimal
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = it
                        amountError = null
                    }
                },
                label = { Text("Valor") },
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Categoria
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null   // limpa o erro ao digitar
                    },
                    label = { Text("Título") },
                    isError = titleError != null,
                    supportingText = titleError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                category = it
                                expanded = false
                            }
                        )
                        if (categoryError != null) {
                            Text(
                                text = categoryError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    // Resetar erros
                    titleError = null
                    amountError = null
                    categoryError = null

                    // Validar título
                    if (title.isBlank()) {
                        titleError = "O título é obrigatório"
                    } else if (title.length < 3) {
                        titleError = "O título deve ter pelo menos 3 caracteres"
                    }

                    // Validar valor
                    val value = amount.toDoubleOrNull()
                    if (value == null) {
                        amountError = "Digite um valor válido"
                    } else if (value <= 0) {
                        amountError = "O valor deve ser maior que zero"
                    }

                    // Validar categoria
                    if (category.isBlank()) {
                        categoryError = "Escolha uma categoria"
                    }

                    // Se tudo estiver OK, salva
                    if (titleError == null && amountError == null && categoryError == null) {
                        val finalValue = value!!
                        if (isEditing) {
                            viewModel.updateTransaction(
                                transaction!!.copy(
                                    title = title.trim(),
                                    amount = finalValue,
                                    category = category,
                                    type = type
                                )
                            )
                        } else {
                            viewModel.addTransaction(
                                userId = userId,
                                title = title.trim(),
                                amount = finalValue,
                                category = category,
                                type = type
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditing) "Salvar" else "Adicionar")
                }
            }
        }
    }
}