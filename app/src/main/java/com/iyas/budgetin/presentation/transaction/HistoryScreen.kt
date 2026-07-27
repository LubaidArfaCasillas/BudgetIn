package com.iyas.budgetin.presentation.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iyas.budgetin.data.model.Transaction
import com.iyas.budgetin.data.model.TransactionType
import com.iyas.budgetin.presentation.home.BottomNavigationBar
import com.iyas.budgetin.presentation.home.TransactionItem
import com.iyas.budgetin.ui.theme.*
import com.iyas.budgetin.utils.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoryScreenContent(
        uiState = uiState,
        onNavigateToHome = onNavigateToHome,
        onNavigateToCharts = onNavigateToCharts,
        onNavigateToAdd = onNavigateToAdd,
        onSearchChange = viewModel::setSearch,
        onFilterChange = viewModel::setFilter,
        onDeleteTransaction = viewModel::deleteTransaction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreenContent(
    uiState: HistoryUiState,
    onNavigateToHome: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (FilterType) -> Unit,
    onDeleteTransaction: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<Transaction?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Hapus Transaksi", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Yakin ingin menghapus transaksi ini?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { onDeleteTransaction(it.id) }
                    showDeleteDialog = null
                }) {
                    Text("Hapus", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Batal", color = TextSecondary) }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(28.dp))
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "history",
                onHomeClick = onNavigateToHome,
                onHistoryClick = {},
                onChartsClick = onNavigateToCharts
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        "Riwayat Transaksi",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onSearchChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    placeholder = { Text("Cari transaksi...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PrimaryBlue,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
            }

            // Filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    item {
                        FilterChipItem(
                            label = "Semua",
                            selected = uiState.filterType == FilterType.ALL,
                            onClick = { onFilterChange(FilterType.ALL) }
                        )
                    }
                    item {
                        FilterChipItem(
                            label = "Pemasukan",
                            selected = uiState.filterType == FilterType.INCOME,
                            onClick = { onFilterChange(FilterType.INCOME) },
                            color = IncomeGreen
                        )
                    }
                    item {
                        FilterChipItem(
                            label = "Pengeluaran",
                            selected = uiState.filterType == FilterType.EXPENSE,
                            onClick = { onFilterChange(FilterType.EXPENSE) },
                            color = ExpenseRed
                        )
                    }
                }
            }

            // Summary
            item {
                val totalIn = uiState.filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val totalOut = uiState.filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniSummaryCard(label = "Pemasukan", amount = totalIn, color = IncomeGreen, modifier = Modifier.weight(1f))
                    MiniSummaryCard(label = "Pengeluaran", amount = totalOut, color = ExpenseRed, modifier = Modifier.weight(1f))
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            } else if (uiState.filteredTransactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(12.dp))
                        Text("Tidak ada transaksi", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Coba ubah filter atau kata kunci pencarian", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            } else {
                // Group by month
                val grouped = groupTransactionsByMonth(uiState.filteredTransactions)
                grouped.forEach { (month, txList) ->
                    item {
                        Text(
                            month,
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                    items(txList, key = { it.id }) { transaction ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    showDeleteDialog = transaction
                                }
                                false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                        .background(ExpenseRed.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        modifier = Modifier.padding(end = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(22.dp))
                                        Text("Hapus", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            modifier = Modifier.animateItem()
                        ) {
                            TransactionItem(
                                transaction = transaction,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color = PrimaryBlue
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color,
            selectedLeadingIconColor = color,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = DividerColor,
            selectedBorderColor = color,
            enabled = true,
            selected = selected
        )
    )
}

@Composable
fun MiniSummaryCard(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
            Spacer(Modifier.height(4.dp))
            Text(formatCurrency(amount), style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    val sampleTransactions = listOf(
        Transaction(id = "1", amount = 5000000.0, type = TransactionType.INCOME, category = "Gaji", date = System.currentTimeMillis(), note = "Gaji bulanan"),
        Transaction(id = "2", amount = 150000.0, type = TransactionType.EXPENSE, category = "Makan & Minum", date = System.currentTimeMillis(), note = "Makan siang"),
        Transaction(id = "3", amount = 500000.0, type = TransactionType.EXPENSE, category = "Transportasi", date = System.currentTimeMillis(), note = "Bensin"),
        Transaction(id = "4", amount = 200000.0, type = TransactionType.INCOME, category = "Freelance", date = System.currentTimeMillis(), note = "Project desain"),
        Transaction(id = "5", amount = 75000.0, type = TransactionType.EXPENSE, category = "Hiburan", date = System.currentTimeMillis(), note = "Nonton bioskop")
    )
    BudgetInTheme(darkTheme = false) {
        HistoryScreenContent(
            uiState = HistoryUiState(
                allTransactions = sampleTransactions,
                filteredTransactions = sampleTransactions,
                isLoading = false
            ),
            onNavigateToHome = {},
            onNavigateToCharts = {},
            onNavigateToAdd = {},
            onSearchChange = {},
            onFilterChange = {},
            onDeleteTransaction = {}
        )
    }
}
