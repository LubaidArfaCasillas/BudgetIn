package com.iyas.budgetin.presentation.transaction

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iyas.budgetin.R
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
    onNavigateToAccount: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoryScreenContent(
        uiState = uiState,
        onNavigateToHome = onNavigateToHome,
        onNavigateToCharts = onNavigateToCharts,
        onNavigateToAccount = onNavigateToAccount,
        onSearchChange = viewModel::setSearch,
        onFilterChange = viewModel::setFilter,
        onMonthYearChange = viewModel::setMonthYear,
        onDeleteTransaction = viewModel::deleteTransaction,
        onNavigateToEdit = onNavigateToEdit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreenContent(
    uiState: HistoryUiState,
    onNavigateToHome: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (FilterType) -> Unit,
    onMonthYearChange: (Int, Int) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit = {}
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
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "history",
                onHomeClick = onNavigateToHome,
                onHistoryClick = {},
                onChartsClick = onNavigateToCharts,
                onAccountClick = onNavigateToAccount
            )
        }
    ) { padding ->
        LazyColumn(
            // Bagian bawah tidak di-padding agar konten menggulir di belakang navbar
            modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 24.dp)
        ) {
            item {
                Column(
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
                            color = NeoYellow
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MiniSummaryCard(label = "Pemasukan", amount = totalIn, color = IncomeGreen, modifier = Modifier.weight(1f))
                    MiniSummaryCard(label = "Pengeluaran", amount = totalOut, color = ExpenseRed, modifier = Modifier.weight(1f))
                }
            }

            // Month and Year Dropdowns
            item {
                val cal = java.util.Calendar.getInstance()
                val currentYear = cal.get(java.util.Calendar.YEAR)
                val currentMonth = cal.get(java.util.Calendar.MONTH)
                val fullMonths = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Month Selector
                    var expandedMonth by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 3.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clickable { expandedMonth = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = SolidBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    fullMonths[uiState.selectedMonth],
                                    color = SolidBlack,
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = SolidBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expandedMonth,
                            onDismissRequest = { expandedMonth = false },
                            modifier = Modifier
                                .background(Color.White)
                                .border(2.dp, SolidBlack, RoundedCornerShape(12.dp))
                                .heightIn(max = 320.dp)
                        ) {
                            fullMonths.forEachIndexed { index, monthName ->
                                val isEnabled = uiState.selectedYear < currentYear || (uiState.selectedYear == currentYear && index <= currentMonth)
                                if (isEnabled) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                monthName,
                                                color = if (index == uiState.selectedMonth) NeoPink else SolidBlack,
                                                fontWeight = if (index == uiState.selectedMonth) FontWeight.Black else FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            onMonthYearChange(index, uiState.selectedYear)
                                            expandedMonth = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Year Selector
                    var expandedYear by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(0.7f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 3.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clickable { expandedYear = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                uiState.selectedYear.toString(),
                                color = SolidBlack,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = SolidBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expandedYear,
                            onDismissRequest = { expandedYear = false },
                            modifier = Modifier
                                .background(Color.White)
                                .border(2.dp, SolidBlack, RoundedCornerShape(12.dp))
                                .heightIn(max = 320.dp)
                        ) {
                            for (year in currentYear downTo 2000) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            year.toString(),
                                            color = if (year == uiState.selectedYear) NeoPink else SolidBlack,
                                            fontWeight = if (year == uiState.selectedYear) FontWeight.Black else FontWeight.Bold
                                        )
                                    },
                                    onClick = {
                                        onMonthYearChange(uiState.selectedMonth, year)
                                        expandedYear = false
                                    }
                                )
                            }
                        }
                    }
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
                        Image(
                            painter = painterResource(id = R.drawable.ic_nothing_found),
                            contentDescription = "Tidak ada transaksi",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Tidak ada transaksi", style = MaterialTheme.typography.titleLarge, color = SolidBlack, fontWeight = FontWeight.Black)
                        Text("Coba ubah filter atau kata kunci pencarian", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                items(uiState.filteredTransactions, key = { it.id }) { transaction ->
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
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(24.dp))
                                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }
                            }
                        },
                        enableDismissFromStartToEnd = false,
                        modifier = Modifier.animateItem()
                    ) {
                        TransactionItem(
                            transaction = transaction,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                            onClick = { onNavigateToEdit(transaction.id) }
                        )
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
    val textColor = if (selected) {
        if (color == NeoYellow) SolidBlack else Color.White
    } else {
        SolidBlack
    }

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
            color = textColor,
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
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(formatCurrency(amount), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Black)
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
                selectedMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
                selectedYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                isLoading = false
            ),
            onNavigateToHome = {},
            onNavigateToCharts = {},
            onNavigateToAccount = {},
            onSearchChange = {},
            onFilterChange = {},
            onMonthYearChange = { _, _ -> },
            onDeleteTransaction = {}
        )
    }
}