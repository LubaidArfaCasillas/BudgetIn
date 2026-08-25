package com.iyas.budgetin.presentation.charts

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iyas.budgetin.R
import com.iyas.budgetin.presentation.home.BottomNavigationBar
import com.iyas.budgetin.ui.theme.*
import com.iyas.budgetin.ui.components.neoBrutalism
import com.iyas.budgetin.utils.formatCurrency
import com.iyas.budgetin.utils.getCategoryColor
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons

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
        onYearChange = viewModel::setYear,
        onMonthChange = viewModel::setMonth
    )
}

@Composable
fun ChartsScreenContent(
    uiState: ChartsUiState,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int?) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    var isExpenseCardsExpanded by remember { mutableStateOf(false) }
    var isIncomeCardsExpanded by remember { mutableStateOf(false) }

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        "Grafik Keuangan",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SolidBlack,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(14.dp))
                    
                    // Period Filter Dropdowns (Neo-Brutalism Selector)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Month selector
                        var monthExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            val monthText = if (uiState.selectedMonth == null) "Semua Bulan" else {
                                val fullMonths = listOf("Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember")
                                fullMonths[uiState.selectedMonth]
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 3.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .clickable { monthExpanded = true }
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
                                        monthText,
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
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false },
                                modifier = Modifier
                                    .background(Color.White)
                                    .border(2.dp, SolidBlack, RoundedCornerShape(12.dp))
                                    .heightIn(max = 320.dp)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Semua Bulan",
                                            color = if (uiState.selectedMonth == null) NeoTeal else SolidBlack,
                                            fontWeight = if (uiState.selectedMonth == null) FontWeight.Black else FontWeight.Bold
                                        )
                                    },
                                    onClick = { onMonthChange(null); monthExpanded = false }
                                )
                                val fullMonths = listOf("Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember")
                                fullMonths.forEachIndexed { index, m ->
                                    val isEnabled = uiState.selectedYear < currentYear || (uiState.selectedYear == currentYear && index <= currentMonth)
                                    if (isEnabled) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    m,
                                                    color = if (uiState.selectedMonth == index) NeoTeal else SolidBlack,
                                                    fontWeight = if (uiState.selectedMonth == index) FontWeight.Black else FontWeight.Bold
                                                )
                                            },
                                            onClick = { onMonthChange(index); monthExpanded = false }
                                        )
                                    }
                                }
                            }
                        }

                        // Year selector
                        var yearExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(0.7f)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 3.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .clickable { yearExpanded = true }
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
                                expanded = yearExpanded,
                                onDismissRequest = { yearExpanded = false },
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
                                                color = if (year == uiState.selectedYear) NeoTeal else SolidBlack,
                                                fontWeight = if (year == uiState.selectedYear) FontWeight.Black else FontWeight.Bold
                                            )
                                        },
                                        onClick = { onYearChange(year); yearExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeoTeal, strokeWidth = 4.dp)
                    }
                }
            } else {
                // Dynamic bar chart
                item {
                    DynamicBarChart(
                        chartData = uiState.chartData,
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
                    val expenseDisplay = if (isExpenseCardsExpanded) uiState.expenseByCategory else uiState.expenseByCategory.take(5)
                    items(
                        items = expenseDisplay,
                        key = { "expense_${it.category}" }
                    ) { share ->
                        Box(
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(300, easing = FastOutSlowInEasing),
                                placementSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                fadeOutSpec = tween(250, easing = FastOutSlowInEasing)
                            )
                        ) {
                            CategoryLegendItem(share, isIncome = false)
                        }
                    }
                    if (uiState.expenseByCategory.size > 5) {
                        item(key = "toggle_expense_legend") {
                            Box(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isExpenseCardsExpanded) "Sembunyikan" else "+ ${uiState.expenseByCategory.size - 5} lainnya",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeoTeal,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { isExpenseCardsExpanded = !isExpenseCardsExpanded }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
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
                    val incomeDisplay = if (isIncomeCardsExpanded) uiState.incomeByCategory else uiState.incomeByCategory.take(5)
                    items(
                        items = incomeDisplay,
                        key = { "income_${it.category}" }
                    ) { share ->
                        Box(
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(300, easing = FastOutSlowInEasing),
                                placementSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                fadeOutSpec = tween(250, easing = FastOutSlowInEasing)
                            )
                        ) {
                            CategoryLegendItem(share, isIncome = true)
                        }
                    }
                    if (uiState.incomeByCategory.size > 5) {
                        item(key = "toggle_income_legend") {
                            Box(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isIncomeCardsExpanded) "Sembunyikan" else "+ ${uiState.incomeByCategory.size - 5} lainnya",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeoTeal,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { isIncomeCardsExpanded = !isIncomeCardsExpanded }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.expenseByCategory.isEmpty() && uiState.incomeByCategory.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_empty_chart),
                                contentDescription = "Belum ada data",
                                modifier = Modifier.size(64.dp)
                            )
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
                    Text(centerText, style = MaterialTheme.typography.labelSmall, color = SolidBlack, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
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

            Column(modifier = Modifier.weight(1f).animateContentSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        color = NeoTeal,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicBarChart(
    chartData: List<ChartBarData>,
    modifier: Modifier = Modifier
) {
    val maxValue = chartData.maxOf { maxOf(it.income, it.expense) }.takeIf { it > 0 } ?: 1.0
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(chartData) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(800, easing = EaseOutCubic))
    }

    Box(
        modifier = modifier
            .neoBrutalism(cornerRadius = 24.dp, shadowOffset = 6.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(if (chartData.size > 12) "Tren Harian" else "Tren Bulanan", style = MaterialTheme.typography.titleLarge, color = SolidBlack, fontWeight = FontWeight.Black)
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
            val barWidth = if (chartData.size > 12) 6.dp else 10.dp
            val isScrollable = chartData.size > 12
            val scrollState = rememberScrollState()

            Box(modifier = Modifier.fillMaxWidth().then(if (isScrollable) Modifier.horizontalScroll(scrollState) else Modifier)) {
                Column(modifier = if (isScrollable) Modifier.padding(horizontal = 8.dp) else Modifier.fillMaxWidth()) {
                    Row(
                        modifier = if (isScrollable) Modifier.height(chartHeight) else Modifier.fillMaxWidth().height(chartHeight),
                        horizontalArrangement = if (isScrollable) Arrangement.spacedBy(6.dp) else Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        chartData.forEach { data ->
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = if (isScrollable) Modifier else Modifier.weight(1f)) {
                                Column(
                                    modifier = if (isScrollable) Modifier else Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    val incomeH = ((data.income / maxValue) * chartHeight.value * animProgress.value).dp
                                    Box(
                                        modifier = Modifier
                                            .width(barWidth)
                                            .height(incomeH.coerceAtLeast(2.dp))
                                            .border(1.dp, SolidBlack, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(IncomeGreen, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    )
                                }
                                Column(
                                    modifier = if (isScrollable) Modifier else Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    val expH = ((data.expense / maxValue) * chartHeight.value * animProgress.value).dp
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
                    }

                    HorizontalDivider(color = SolidBlack, thickness = 2.dp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))

                    Row(
                        modifier = if (isScrollable) Modifier.padding(top = 8.dp) else Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = if (isScrollable) Arrangement.spacedBy(6.dp) else Arrangement.SpaceEvenly
                    ) {
                        chartData.forEach { data ->
                            Text(
                                data.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = SolidBlack,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = if (isScrollable) Modifier.width((barWidth * 2) + 2.dp) else Modifier.weight(1f),
                                fontSize = if (isScrollable) 8.sp else 9.sp
                            )
                        }
                    }
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
        ChartBarData(
            label = months[i],
            index = i,
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
                chartData = sampleMonthlyData,
                expenseByCategory = sampleExpenseByCategory,
                incomeByCategory = sampleIncomeByCategory,
                selectedYear = 2026,
                selectedMonth = null,
                isLoading = false
            ),
            onNavigateToHome = {},
            onNavigateToHistory = {},
            onNavigateToAccount = {},
            onYearChange = {},
            onMonthChange = {}
        )
    }
}