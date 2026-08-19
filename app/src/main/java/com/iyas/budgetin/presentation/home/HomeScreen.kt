package com.iyas.budgetin.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.iyas.budgetin.R
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
    val showAllThisMonth = remember { mutableStateOf(false) }

    val currentMonthTransactions = remember(uiState.transactions) {
        val currentCalendar = java.util.Calendar.getInstance()
        val currentMonth = currentCalendar.get(java.util.Calendar.MONTH)
        val currentYear = currentCalendar.get(java.util.Calendar.YEAR)
        
        uiState.transactions.filter {
            val itemCalendar = java.util.Calendar.getInstance()
            itemCalendar.timeInMillis = it.date
            itemCalendar.get(java.util.Calendar.MONTH) == currentMonth && itemCalendar.get(java.util.Calendar.YEAR) == currentYear
        }
    }

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
                // Hanya inset atas yang dipakai; bagian bawah dibiarkan agar
                // konten menggulir di belakang navbar
                .padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 16.dp)
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
                Text(
                    "Transaksi Terbaru",
                    style = MaterialTheme.typography.titleLarge,
                    color = SolidBlack,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
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
                val displayedTransactions = if (showAllThisMonth.value) currentMonthTransactions else uiState.transactions.take(5)
                val hasMoreTransactions = if (showAllThisMonth.value) false else uiState.transactions.size > 5
                
                // Jumlah kartu terakhir yang ikut memudar, supaya transisinya terasa lebih panjang
                val fadeCount = if (hasMoreTransactions) minOf(2, displayedTransactions.size) else 0
                val normalTransactions = displayedTransactions.dropLast(fadeCount)

                items(normalTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        onClick = { onNavigateToEdit(transaction.id) }
                    )
                }

                if (fadeCount > 0) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                displayedTransactions.takeLast(fadeCount).forEach { transaction ->
                                    TransactionItem(
                                        transaction = transaction,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                        onClick = { onNavigateToEdit(transaction.id) }
                                    )
                                }
                            }
                            // Beri efek fade di beberapa transaksi terakhir yang tampil agar
                            // pengguna baru sadar masih ada transaksi lain di bawahnya
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                                            )
                                        ),
                                        RoundedCornerShape(16.dp)
                                    )
                            )
                        }
                    }
                }

                item {
                    TextButton(
                        onClick = { showAllThisMonth.value = !showAllThisMonth.value },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (showAllThisMonth.value) "Tampilkan Sedikit" else "Lihat Semua",
                                color = NeoPink,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = if (showAllThisMonth.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = NeoPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
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
            .background(NeoBlue, RoundedCornerShape(24.dp))
    ) {
        // Watermark logo "₿" dimiringkan 45°
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                "₿",
                fontSize = 160.sp,
                color = SolidBlack.copy(alpha = 0.07f),
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .offset(x = 20.dp, y = (-10).dp)
                    .graphicsLayer { rotationZ = -45f }
            )
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text("TOTAL SALDO", style = MaterialTheme.typography.labelLarge, color = SolidBlack, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text(
                formatCurrency(animBalance.toDouble()),
                style = MaterialTheme.typography.displayMedium,
                color = SolidBlack,
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
            .background(if(isIncome) IncomeGreen else ExpenseRed, RoundedCornerShape(16.dp))
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
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp, color = if (isIncome) IncomeGreen else ExpenseRed, fontWeight = FontWeight.Black)
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
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = if (isIncome) "Pemasukan" else "Pengeluaran",
                    tint = if (isIncome) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(28.dp)
                )
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
            Image(
                painter = painterResource(id = R.drawable.ic_empty_wallet),
                contentDescription = "Belum ada transaksi",
                modifier = Modifier.size(64.dp)
            )
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
    // Area di sekeliling kartu sengaja dibiarkan transparan agar konten yang
    // menggulir di belakangnya terpotong mengikuti lengkung kartu. Krem hanya
    // diisikan pada strip di bawah kartu supaya konten tidak menyembul ke
    // area tombol navigasi HP.
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // bottom = 5dp menyediakan ruang tepat untuk bayangan kartu
                .padding(start = 16.dp, end = 16.dp, bottom = 5.dp)
                .neoBrutalism(cornerRadius = 20.dp, shadowOffset = 5.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
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
                    icon = Icons.Default.History,
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

        // Penutup krem di bawah kartu, sekaligus pengganjal tombol navigasi HP
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(Modifier.height(10.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
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
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(if (isSelected) activeColor else Color.Transparent, RoundedCornerShape(12.dp))
                .then(if (isSelected) Modifier.border(2.dp, SolidBlack, RoundedCornerShape(12.dp)) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isSelected) SolidBlack else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
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

