package com.iyas.budgetin.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.iyas.budgetin.data.model.Transaction
import com.iyas.budgetin.data.model.TransactionType
import com.iyas.budgetin.ui.theme.*
import com.iyas.budgetin.ui.components.neoBrutalism
import com.iyas.budgetin.utils.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: ""
    val displayName = auth.currentUser?.displayName
    val userName = (displayName.takeIf { !it.isNullOrBlank() } ?: userEmail.substringBefore("@"))
        .replaceFirstChar { it.uppercase() }

    HomeScreenContent(
        uiState = uiState,
        userName = userName,
        onNavigateToAdd = onNavigateToAdd,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToCharts = onNavigateToCharts,
        onNavigateToAccount = onNavigateToAccount,
        onNavigateToEdit = onNavigateToEdit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    userName: String,
    onNavigateToAdd: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = NeoPink,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .size(64.dp)
                    .neoBrutalism(cornerRadius = 18.dp, shadowOffset = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(32.dp), tint = SolidBlack)
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "home",
                onHomeClick = {},
                onHistoryClick = onNavigateToHistory,
                onChartsClick = onNavigateToCharts,
                onAccountClick = onNavigateToAccount
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Halo,",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            userName,
                            style = MaterialTheme.typography.titleLarge,
                            color = SolidBlack,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            item {
                // Balance Card
                BalanceCard(
                    balance = uiState.balance,
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            item {
                // Recent transactions header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transaksi Terbaru", style = MaterialTheme.typography.titleLarge, color = SolidBlack, fontWeight = FontWeight.Black)
                    TextButton(onClick = onNavigateToHistory) {
                        Text("See All", color = NeoPink, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeoPink, strokeWidth = 4.dp)
                    }
                }
            } else if (uiState.transactions.isEmpty()) {
                item {
                    EmptyTransactionCard(onNavigateToAdd)
                }
            } else {
                items(uiState.transactions.take(5)) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        onClick = { onNavigateToEdit(transaction.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun BalanceCard(
    balance: Double,
    totalIncome: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    val animBalance by animateFloatAsState(
        targetValue = balance.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "balance"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 8.dp)
            .background(NeoPink, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column {
            Text("TOTAL BALANCE", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text(
                formatCurrency(animBalance.toDouble()),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BalanceStatCard(
                    label = "Pemasukan",
                    amount = totalIncome,
                    icon = "↑",
                    modifier = Modifier.weight(1f),
                    isIncome = true
                )
                BalanceStatCard(
                    label = "Pengeluaran",
                    amount = totalExpense,
                    icon = "↓",
                    modifier = Modifier.weight(1f),
                    isIncome = false
                )
            }
        }
    }
}

@Composable
fun BalanceStatCard(
    label: String,
    amount: Double,
    icon: String,
    modifier: Modifier = Modifier,
    isIncome: Boolean
) {
    Box(
        modifier = modifier
            .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 4.dp)
            .background(if(isIncome) NeoYellow else Color.White, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(2.dp, SolidBlack, CircleShape)
                    .background(if (isIncome) Color.White else NeoTeal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp, color = SolidBlack, fontWeight = FontWeight.Black)
            }
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = SolidBlack, fontWeight = FontWeight.Bold)
                Text(
                    formatCurrency(amount),
                    style = MaterialTheme.typography.bodySmall,
                    color = SolidBlack,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val icon = CATEGORY_ICONS[transaction.category] ?: if (isIncome) "💰" else "💸"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 4.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(2.dp, SolidBlack, RoundedCornerShape(12.dp))
                    .background(
                        if (isIncome) NeoYellow else NeoPurple,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = SolidBlack,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                "${if (isIncome) "+" else "-"} ${formatCurrency(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = if (isIncome) SolidBlack else ExpenseRed,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun EmptyTransactionCard(onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .neoBrutalism(cornerRadius = 20.dp, shadowOffset = 6.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💸", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text("Belum ada transaksi", style = MaterialTheme.typography.titleLarge, color = SolidBlack, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text("Tambahkan transaksi pertama Anda", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 4.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoPink),
                modifier = Modifier.neoBrutalism(cornerRadius = 12.dp, shadowOffset = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = SolidBlack)
                Spacer(Modifier.width(8.dp))
                Text("Tambah Transaksi", color = SolidBlack, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onChartsClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 6.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentRoute == "home",
                onClick = onHomeClick,
                activeColor = NeoPink
            )
            NavItem(
                icon = Icons.AutoMirrored.Filled.List,
                label = "Riwayat",
                isSelected = currentRoute == "history",
                onClick = onHistoryClick,
                activeColor = NeoYellow
            )
            NavItem(
                icon = Icons.Default.PieChart,
                label = "Grafik",
                isSelected = currentRoute == "charts",
                onClick = onChartsClick,
                activeColor = NeoTeal
            )
            NavItem(
                icon = Icons.Default.Person,
                label = "Akun",
                isSelected = currentRoute == "account",
                onClick = onAccountClick,
                activeColor = NeoPurple
            )
        }
    }
}

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (isSelected) activeColor else Color.Transparent, RoundedCornerShape(12.dp))
                .then(if (isSelected) Modifier.border(2.dp, SolidBlack, RoundedCornerShape(12.dp)) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isSelected) SolidBlack else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = SolidBlack)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleTransactions = listOf(
        Transaction(id = "1", amount = 5000000.0, type = TransactionType.INCOME, category = "Gaji", date = System.currentTimeMillis(), note = "Gaji bulanan"),
        Transaction(id = "2", amount = 150000.0, type = TransactionType.EXPENSE, category = "Makan & Minum", date = System.currentTimeMillis(), note = "Makan siang"),
        Transaction(id = "3", amount = 500000.0, type = TransactionType.EXPENSE, category = "Transportasi", date = System.currentTimeMillis(), note = "Bensin")
    )
    BudgetInTheme(darkTheme = false) {
        HomeScreenContent(
            uiState = HomeUiState(
                transactions = sampleTransactions,
                balance = 4475000.0,
                totalIncome = 5200000.0,
                totalExpense = 725000.0,
                isLoading = false
            ),
            userName = "User",
            onNavigateToAdd = {},
            onNavigateToHistory = {},
            onNavigateToCharts = {},
            onNavigateToAccount = {}
        )
    }
}
