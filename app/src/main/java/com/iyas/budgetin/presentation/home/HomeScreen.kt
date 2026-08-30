package com.iyas.budgetin.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.clipRect
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
    val user = auth.currentUser
    val userName = user?.displayName?.takeIf { it.isNotBlank() }
        ?: user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }
        ?: "Pengguna"

    HomeScreenContent(
        uiState = uiState,
        userName = userName,
        onNavigateToAdd = onNavigateToAdd,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToCharts = onNavigateToCharts,
        onNavigateToAccount = onNavigateToAccount,
        onNavigateToEdit = onNavigateToEdit,
        onToggleShowAll = homeViewModel::toggleShowAll
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
    onNavigateToEdit: (String) -> Unit = {},
    onToggleShowAll: () -> Unit = {}
) {
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
        val listState = rememberLazyListState()
        val bottomNavPadding = padding.calculateBottomPadding()
        val backgroundColor = MaterialTheme.colorScheme.background

        val itemFadeInSpec = remember { tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing) }
        val itemPlacementSpec = remember { tween<androidx.compose.ui.unit.IntOffset>(durationMillis = 300, easing = FastOutSlowInEasing) }
        val itemFadeOutSpec = remember { tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomNavPadding + 24.dp)
            ) {
                item(key = "header") {
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

            item(key = "balance_card") {
                // Balance Card
                BalanceCard(
                    balance = uiState.balance,
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            item(key = "transactions_header") {
                // Recent transactions header
                Text(
                    "Transaksi Bulan Ini",
                    style = MaterialTheme.typography.titleLarge,
                    color = SolidBlack,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            if (uiState.isLoading) {
                item(key = "loading") {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeoPink, strokeWidth = 4.dp)
                    }
                }
            } else if (currentMonthTransactions.isEmpty()) {
                item(key = "empty") {
                    EmptyTransactionCard(onNavigateToAdd)
                }
            } else {
                val isExpanded = uiState.showAllThisMonth
                val displayedTransactions = if (isExpanded) currentMonthTransactions else currentMonthTransactions.take(5)
                val hasMoreTransactions = currentMonthTransactions.size > 5

                itemsIndexed(
                    items = displayedTransactions,
                    key = { _, transaction -> transaction.id }
                ) { index, transaction ->
                    val isLastTwoWhenCollapsed = !isExpanded && hasMoreTransactions && index >= displayedTransactions.size - 2
                    val fadeAlphaTarget = if (isLastTwoWhenCollapsed) {
                        if (index == displayedTransactions.size - 1) 0.85f else 0.4f
                    } else 0f

                    val fadeAlpha by animateFloatAsState(
                        targetValue = fadeAlphaTarget,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        label = "fade_alpha_${transaction.id}"
                    )

                    Box(
                        modifier = Modifier
                            .animateItem(
                                fadeInSpec = itemFadeInSpec,
                                placementSpec = itemPlacementSpec,
                                fadeOutSpec = itemFadeOutSpec
                            )
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onNavigateToEdit(transaction.id) }
                        )

                        if (fadeAlpha > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = if (index == displayedTransactions.size - 1) {
                                                listOf(
                                                    MaterialTheme.colorScheme.background.copy(alpha = fadeAlpha * 0.4f),
                                                    MaterialTheme.colorScheme.background.copy(alpha = fadeAlpha)
                                                )
                                            } else {
                                                listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.background.copy(alpha = fadeAlpha)
                                                )
                                            }
                                        ),
                                        RoundedCornerShape(16.dp)
                                    )
                            )
                        }
                    }
                }

                if (hasMoreTransactions) {
                    item(key = "toggle_expand_button") {
                        Box(
                            modifier = Modifier
                                .animateItem(
                                    fadeInSpec = itemFadeInSpec,
                                    placementSpec = itemPlacementSpec,
                                    fadeOutSpec = itemFadeOutSpec
                                )
                                .fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = onToggleShowAll,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    AnimatedContent(
                                        targetState = uiState.showAllThisMonth,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing))
                                                .togetherWith(fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)))
                                        },
                                        label = "toggle_text_anim"
                                    ) { expanded ->
                                        Text(
                                            text = if (expanded) "Lihat Lebih Sedikit" else "Lihat Semua",
                                            color = NeoPink,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    val arrowRotation by animateFloatAsState(
                                        targetValue = if (uiState.showAllThisMonth) 180f else 0f,
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                        label = "arrow_rotation"
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (uiState.showAllThisMonth) "Lihat Lebih Sedikit" else "Lihat Semua",
                                        tint = NeoPink,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .graphicsLayer { rotationZ = arrowRotation }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = itemFadeInSpec,
                            placementSpec = itemPlacementSpec,
                            fadeOutSpec = itemFadeOutSpec
                        )
                        .height(80.dp)
                )
            }
        }

        // Canvas overlay yang menggambar gradien melengkung persis mengikuti bentuk card navbar
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val bottomPaddingPx = bottomNavPadding.toPx()
            val fadePx = 52.dp.toPx()
            val padPx = 16.dp.toPx()
            val radiusPx = 20.dp.toPx()

            val cardTop = size.height - bottomPaddingPx
            val fadeTop = cardTop - fadePx
            val cxLeft = padPx + radiusPx
            val cxRight = size.width - padPx - radiusPx
            val cy = cardTop + radiusPx
            val outerRadius = radiusPx + fadePx

            // 1. Latar solid krem di bawah sudut lengkung navbar hingga dasar layar
            drawRect(
                color = backgroundColor,
                topLeft = Offset(0f, cy),
                size = Size(size.width, size.height - cy)
            )

            // 2. Latar solid krem di dalam badan navbar (antara x=pad dan x=size.width-pad dari cardTop ke cy)
            drawRect(
                color = backgroundColor,
                topLeft = Offset(padPx, cardTop),
                size = Size(size.width - 2 * padPx, radiusPx)
            )

            // 3. Sudut lengkung solid di kiri & kanan atas kartu navbar
            drawArc(
                color = backgroundColor,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = true,
                topLeft = Offset(padPx, cardTop),
                size = Size(radiusPx * 2, radiusPx * 2)
            )
            drawArc(
                color = backgroundColor,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = true,
                topLeft = Offset(size.width - padPx - radiusPx * 2, cardTop),
                size = Size(radiusPx * 2, radiusPx * 2)
            )

            // 4. Gradien vertikal lurus di atas bagian tengah kartu navbar
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, backgroundColor),
                    startY = fadeTop,
                    endY = cardTop
                ),
                topLeft = Offset(cxLeft, fadeTop),
                size = Size(cxRight - cxLeft, fadePx)
            )

            // 5. Gradien radial melengkung di sudut kiri atas kartu navbar
            val colorStops = arrayOf(
                0f to backgroundColor,
                (radiusPx / outerRadius) to backgroundColor,
                1f to Color.Transparent
            )

            clipRect(left = 0f, top = fadeTop, right = cxLeft, bottom = cy) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = colorStops,
                        center = Offset(cxLeft, cy),
                        radius = outerRadius
                    ),
                    radius = outerRadius,
                    center = Offset(cxLeft, cy)
                )
            }

            // 6. Gradien radial melengkung di sudut kanan atas kartu navbar
            clipRect(left = cxRight, top = fadeTop, right = size.width, bottom = cy) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = colorStops,
                        center = Offset(cxRight, cy),
                        radius = outerRadius
                    ),
                    radius = outerRadius,
                    center = Offset(cxRight, cy)
                )
            }
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
    val horizontalPadding = 16.dp
    val cornerRadius = 20.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // bottom = 6dp menyediakan ruang tepat untuk bayangan kartu
                .padding(start = horizontalPadding, end = horizontalPadding, bottom = 6.dp)
                .neoBrutalism(cornerRadius = cornerRadius, shadowOffset = 5.dp)
                .background(Color.White, RoundedCornerShape(cornerRadius))
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

        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
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
            .background(if (isIncome) IncomeGreen else ExpenseRed, RoundedCornerShape(16.dp))
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
                color = if (isIncome) IncomeGreen else ExpenseRed,
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

