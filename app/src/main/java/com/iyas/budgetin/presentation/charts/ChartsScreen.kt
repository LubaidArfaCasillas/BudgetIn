package com.iyas.budgetin.presentation.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iyas.budgetin.presentation.home.BottomNavigationBar
import com.iyas.budgetin.ui.theme.*
import com.iyas.budgetin.utils.formatCurrency
import com.iyas.budgetin.utils.CATEGORY_ICONS
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

val CHART_COLORS = listOf(
    ChartColor1, ChartColor2, ChartColor3, ChartColor4,
    ChartColor5, ChartColor6, ChartColor7, ChartColor8
)

@Composable
fun ChartsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: ChartsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(28.dp))
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "charts",
                onHomeClick = onNavigateToHome,
                onHistoryClick = onNavigateToHistory,
                onChartsClick = {}
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0D2137), BackgroundDark),
                                start = Offset(0f, 0f),
                                end = Offset(0f, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grafik Keuangan", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        // Year selector
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.setYear(uiState.selectedYear - 1) }) {
                                Text("<", color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Text(
                                uiState.selectedYear.toString(),
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(
                                onClick = { viewModel.setYear(uiState.selectedYear + 1) },
                                enabled = uiState.selectedYear < currentYear
                            ) {
                                Text(">", color = if (uiState.selectedYear < currentYear) GreenPrimary else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
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
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        DonutChart(
                            data = uiState.expenseByCategory,
                            colors = CHART_COLORS,
                            centerText = "Pengeluaran",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        )
                    }
                    items(uiState.expenseByCategory.take(6)) { share ->
                        CategoryLegendItem(share, CHART_COLORS[uiState.expenseByCategory.indexOf(share) % CHART_COLORS.size])
                    }
                }

                // Income pie chart
                if (uiState.incomeByCategory.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Pemasukan per Kategori",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        DonutChart(
                            data = uiState.incomeByCategory,
                            colors = CHART_COLORS,
                            centerText = "Pemasukan",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        )
                    }
                    items(uiState.incomeByCategory.take(6)) { share ->
                        CategoryLegendItem(share, CHART_COLORS[uiState.incomeByCategory.indexOf(share) % CHART_COLORS.size])
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
                            Text("Belum ada data", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Tambahkan transaksi untuk melihat grafik", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(900, easing = EaseOutCubic))
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
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
                    Text(centerText, style = MaterialTheme.typography.labelSmall, color = TextSecondary, textAlign = TextAlign.Center)
                    Text(
                        formatCurrency(data.sumOf { it.amount }),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.take(5).forEachIndexed { index, share ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(colors[index % colors.size], CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(share.category, style = MaterialTheme.typography.labelSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${share.percentage.toInt()}%", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
                if (data.size > 5) {
                    Text("+ ${data.size - 5} lainnya", style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
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

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Tren Bulanan", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(8.dp).background(IncomeGreen, CircleShape))
                    Text("Pemasukan", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(8.dp).background(ExpenseRed, CircleShape))
                    Text("Pengeluaran", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))

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
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(IncomeGreen)
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
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(ExpenseRed)
                        )
                    }
                }
            }

            HorizontalDivider(color = Divider, modifier = Modifier.padding(top = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                monthlyData.forEach { data ->
                    Text(
                        data.month,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
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
fun CategoryLegendItem(share: CategoryShare, color: Color) {
    val icon = CATEGORY_ICONS[share.category] ?: "📌"
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }
            Text(share.category, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrency(share.amount), style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${share.percentage.toInt()}%", style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}
