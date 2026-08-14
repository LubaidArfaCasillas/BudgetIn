package com.iyas.budgetin.presentation.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iyas.budgetin.presentation.home.BottomNavigationBar
import com.iyas.budgetin.ui.theme.*
import com.iyas.budgetin.ui.components.neoBrutalism
import com.iyas.budgetin.utils.formatCurrency
import com.iyas.budgetin.utils.getCategoryColor
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

@Composable
fun ChartsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAccount: () -> Unit,
    viewModel: ChartsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ChartsScreenContent(
        uiState = uiState,
        onNavigateToHome = onNavigateToHome,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToAccount = onNavigateToAccount,
        onYearChange = viewModel::setYear
    )
}

@Composable
fun ChartsScreenContent(
    uiState: ChartsUiState,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onYearChange: (Int) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "charts",
                onHomeClick = onNavigateToHome,
                onHistoryClick = onNavigateToHistory,
                onChartsClick = {},
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grafik Keuangan", style = MaterialTheme.typography.headlineMedium, color = SolidBlack, fontWeight = FontWeight.Black)
                        // Year selector
                        var expanded by remember { mutableStateOf(false) }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.selectedYear > 2000) {
                                IconButton(onClick = { onYearChange(uiState.selectedYear - 1) }) {
                                    Text("<", color = SolidBlack, fontWeight = FontWeight.Black, fontSize = 24.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                            
                            Box {
                                Row(
                                    modifier = Modifier.clickable { expanded = true }.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        uiState.selectedYear.toString(),
                                        color = SolidBlack,
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(Color.White).border(2.dp, SolidBlack, RoundedCornerShape(8.dp))
                                ) {
                                    for (year in currentYear downTo 2000) {
                                        DropdownMenuItem(
                                            text = { Text(year.toString(), color = SolidBlack, fontWeight = if (year == uiState.selectedYear) FontWeight.Black else FontWeight.Normal) },
                                            onClick = {
                                                onYearChange(year)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            if (uiState.selectedYear < currentYear) {
                                IconButton(
                                    onClick = { onYearChange(uiState.selectedYear + 1) }
                                ) {
                                    Text(">", color = SolidBlack, fontWeight = FontWeight.Black, fontSize = 24.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeoPink, strokeWidth = 4.dp)
                    }
                }
            } else {
                // Monthly bar chart
                item {
                    MonthlyBarChart(
                        monthlyData = uiState.monthlyData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // Expense pie chart
                if (uiState.expenseByCategory.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Pengeluaran per Kategori",
                            style = MaterialTheme.typography.titleLarge,
                            color = SolidBlack,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        DonutChart(
                            data = uiState.expenseByCategory,
                            colors = uiState.expenseByCategory.map { getCategoryColor(it.category) },
                            centerText = "Pengeluaran",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                    items(uiState.expenseByCategory.take(6)) { share ->
                        CategoryLegendItem(share, isIncome = false)
                    }
                }

                // Income pie chart
                if (uiState.incomeByCategory.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Pemasukan per Kategori",
                            style = MaterialTheme.typography.titleLarge,
                            color = SolidBlack,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        DonutChart(
                            data = uiState.incomeByCategory,
                            colors = uiState.incomeByCategory.map { getCategoryColor(it.category) },
                            centerText = "Pemasukan",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                    items(uiState.incomeByCategory.take(6)) { share ->
                        CategoryLegendItem(share, isIncome = true)
                    }
                }

                if (uiState.expenseByCategory.isEmpty() && uiState.incomeByCategory.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📊", style = MaterialTheme.typography.displayMedium)
                            Spacer(Modifier.height(12.dp))
                            Text("Belum ada data", style = MaterialTheme.typography.titleLarge, color = SolidBlack, fontWeight = FontWeight.Black)
                            Text("Tambahkan transaksi untuk melihat grafik", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    data: List<CategoryShare>,
    colors: List<Color>,
    centerText: String,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(900, easing = EaseOutCubic))
    }

    Box(
        modifier = modifier
            .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 6.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 40.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(radius * 2, radius * 2)
                    var startAngle = -90f
                    val totalSweep = 360f * animProgress.value

                    data.forEachIndexed { index, share ->
                        val sweep = (share.percentage / 100f) * totalSweep
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweep - 2f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(centerText, style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    Text(
                        formatCurrency(data.sumOf { it.amount }),
                        style = MaterialTheme.typography.labelLarge,
                        color = SolidBlack,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val displayData = if (isExpanded) data else data.take(5)
                
                displayData.forEachIndexed { index, share ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .border(1.dp, SolidBlack, CircleShape)
                                .background(colors[index % colors.size], CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(share.category, style = MaterialTheme.typography.labelSmall, color = SolidBlack, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                            Text("${share.percentage.toInt()}%", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (data.size > 5) {
                    Text(
                        text = if (isExpanded) "Sembunyikan" else "+ ${data.size - 5} lainnya",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyBarChart(
    monthlyData: List<MonthlyData>,
    modifier: Modifier = Modifier
) {
    val maxValue = monthlyData.maxOf { maxOf(it.income, it.expense) }.takeIf { it > 0 } ?: 1.0
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(monthlyData) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(800, easing = EaseOutCubic))
    }

    Box(
        modifier = modifier
            .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 6.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Tren Bulanan", style = MaterialTheme.typography.titleLarge, color = SolidBlack, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(10.dp).border(1.dp, SolidBlack, CircleShape).background(IncomeGreen, CircleShape))
                    Text("Pemasukan", style = MaterialTheme.typography.labelSmall, color = SolidBlack, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(10.dp).border(1.dp, SolidBlack, CircleShape).background(ExpenseRed, CircleShape))
                    Text("Pengeluaran", style = MaterialTheme.typography.labelSmall, color = SolidBlack, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            val chartHeight = 140.dp
            val barWidth = 10.dp

            Row(
                modifier = Modifier.fillMaxWidth().height(chartHeight),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                monthlyData.forEach { monthData ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // income bar
                        val incomeH = ((monthData.income / maxValue) * chartHeight.value * animProgress.value).dp
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height(incomeH.coerceAtLeast(2.dp))
                                .border(1.dp, SolidBlack, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(IncomeGreen, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // expense bar
                        val expH = ((monthData.expense / maxValue) * chartHeight.value * animProgress.value).dp
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height(expH.coerceAtLeast(2.dp))
                                .border(1.dp, SolidBlack, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(ExpenseRed, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                }
            }

            HorizontalDivider(color = SolidBlack, thickness = 2.dp, modifier = Modifier.padding(top = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                monthlyData.forEach { data ->
                    Text(
                        data.month,
                        style = MaterialTheme.typography.labelSmall,
                        color = SolidBlack,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(2f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryLegendItem(share: CategoryShare, isIncome: Boolean = false) {
    val cardColor = getCategoryColor(share.category)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 4.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .border(1.dp, SolidBlack, CircleShape)
                        .background(cardColor, CircleShape)
                )
                Text(share.category, style = MaterialTheme.typography.titleMedium, color = SolidBlack, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrency(share.amount), style = MaterialTheme.typography.titleSmall, color = SolidBlack, fontWeight = FontWeight.Black)
                Text("${share.percentage.toInt()}%", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChartsScreenPreview() {
    val sampleMonthlyData = (0..11).map { i ->
        val months = listOf("Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des")
        MonthlyData(
            month = months[i],
            monthIndex = i,
            income = listOf(5000000.0, 4500000.0, 6000000.0, 5500000.0, 7000000.0, 4000000.0, 5200000.0, 6500000.0, 5800000.0, 4800000.0, 5100000.0, 7500000.0)[i],
            expense = listOf(3000000.0, 3500000.0, 4000000.0, 2800000.0, 4500000.0, 3200000.0, 3800000.0, 4200000.0, 3600000.0, 3100000.0, 3900000.0, 5000000.0)[i]
        )
    }
    val sampleExpenseByCategory = listOf(
        CategoryShare("Makan & Minum", 1500000.0, 35f),
        CategoryShare("Transportasi", 800000.0, 19f),
        CategoryShare("Belanja", 650000.0, 15f),
        CategoryShare("Tagihan", 550000.0, 13f),
        CategoryShare("Hiburan", 400000.0, 9f),
        CategoryShare("Lainnya", 380000.0, 9f)
    )
    val sampleIncomeByCategory = listOf(
        CategoryShare("Gaji", 5000000.0, 70f),
        CategoryShare("Freelance", 1500000.0, 21f),
        CategoryShare("Investasi", 650000.0, 9f)
    )
    BudgetInTheme(darkTheme = false) {
        ChartsScreenContent(
            uiState = ChartsUiState(
                monthlyData = sampleMonthlyData,
                expenseByCategory = sampleExpenseByCategory,
                incomeByCategory = sampleIncomeByCategory,
                selectedYear = 2026,
                isLoading = false
            ),
            onNavigateToHome = {},
            onNavigateToHistory = {},
            onNavigateToAccount = {},
            onYearChange = {}
        )
    }
}
