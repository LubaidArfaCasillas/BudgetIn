package com.iyas.budgetin.presentation.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iyas.budgetin.data.model.Transaction
import com.iyas.budgetin.data.model.TransactionType
import com.iyas.budgetin.presentation.home.BottomNavigationBar
import com.iyas.budgetin.presentation.home.TransactionItem
import com.iyas.budgetin.ui.theme.*
import com.iyas.budgetin.ui.components.neoBrutalism
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
            containerColor = Color.White,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp),
            title = { Text("Hapus Transaksi", color = SolidBlack, fontWeight = FontWeight.Black) },
            text = { Text("Yakin ingin menghapus transaksi ini?", color = TextSecondary, fontWeight = FontWeight.Bold) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog?.let { onDeleteTransaction(it.id) }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.neoBrutalism(cornerRadius = 8.dp, shadowOffset = 2.dp)
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = null },
                    border = BorderStroke(2.dp, SolidBlack),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SolidBlack)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = NeoPink,
                contentColor = SolidBlack,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .size(64.dp)
                    .neoBrutalism(cornerRadius = 18.dp, shadowOffset = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(32.dp))
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
                        color = SolidBlack,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onSearchChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    placeholder = { Text("Cari transaksi...", color = TextSecondary, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SolidBlack, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = SolidBlack, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SolidBlack,
                        unfocusedBorderColor = SolidBlack,
                        focusedTextColor = SolidBlack,
                        unfocusedTextColor = SolidBlack,
                        cursorColor = SolidBlack,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    item {
                        FilterChipItem(
                            label = "Semua",
                            selected = uiState.filterType == FilterType.ALL,
                            onClick = { onFilterChange(FilterType.ALL) },
                            color = NeoTeal
                        )
                    }
                    item {
                        FilterChipItem(
                            label = "Pemasukan",
                            selected = uiState.filterType == FilterType.INCOME,
                            onClick = { onFilterChange(FilterType.INCOME) },
                            color = NeoYellow
                        )
                    }
                    item {
                        FilterChipItem(
                            label = "Pengeluaran",
                            selected = uiState.filterType == FilterType.EXPENSE,
                            onClick = { onFilterChange(FilterType.EXPENSE) },
                            color = NeoPurple
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MiniSummaryCard(label = "Pemasukan", amount = totalIn, color = NeoYellow, modifier = Modifier.weight(1f))
                    MiniSummaryCard(label = "Pengeluaran", amount = totalOut, color = NeoPurple, modifier = Modifier.weight(1f))
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeoPink, strokeWidth = 4.dp)
                    }
                }
            } else if (uiState.filteredTransactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(16.dp))
                        Text("Tidak ada transaksi", style = MaterialTheme.typography.titleLarge, color = SolidBlack, fontWeight = FontWeight.Black)
                        Text("Coba ubah filter atau kata kunci pencarian", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                // Group by month
                val grouped = groupTransactionsByMonth(uiState.filteredTransactions)
                grouped.forEach { (month, txList) ->
                    item {
                        Text(
                            month,
                            style = MaterialTheme.typography.titleMedium,
                            color = SolidBlack,
                            fontWeight = FontWeight.Black,
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
                                        .padding(horizontal = 20.dp, vertical = 6.dp)
                                        .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 4.dp)
                                        .background(ExpenseRed, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        modifier = Modifier.padding(end = 20.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(24.dp))
                                        Text("Hapus", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            modifier = Modifier.animateItem()
                        ) {
                            TransactionItem(
                                transaction = transaction,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
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
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) color else Color.White)
            .clickable(onClick = onClick)
            .border(2.dp, SolidBlack, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = SolidBlack,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun MiniSummaryCard(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 4.dp)
            .background(color, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = SolidBlack, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(formatCurrency(amount), style = MaterialTheme.typography.titleMedium, color = SolidBlack, fontWeight = FontWeight.Black)
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
