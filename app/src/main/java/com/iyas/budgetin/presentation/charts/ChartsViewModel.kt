package com.iyas.budgetin.presentation.charts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyas.budgetin.data.preferences.AppPreferenceManager
import com.iyas.budgetin.data.model.Transaction
import com.iyas.budgetin.data.model.TransactionType
import com.iyas.budgetin.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class CategoryShare(val category: String, val amount: Double, val percentage: Float)
data class ChartBarData(val label: String, val index: Int, val income: Double, val expense: Double)

data class ChartsUiState(
    val transactions: List<Transaction> = emptyList(),
    val expenseByCategory: List<CategoryShare> = emptyList(),
    val incomeByCategory: List<CategoryShare> = emptyList(),
    val chartData: List<ChartBarData> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int? = Calendar.getInstance().get(Calendar.MONTH),
    val isLoading: Boolean = true
)

class ChartsViewModel(
    private val repository: TransactionRepository,
    private val preferenceManager: AppPreferenceManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialYear = savedStateHandle.get<Int>("selectedYear") ?: preferenceManager.getChartsSelectedYear()
    private val initialMonth = if (savedStateHandle.contains("selectedMonth")) {
        savedStateHandle.get<Int?>("selectedMonth")
    } else {
        preferenceManager.getChartsSelectedMonth()
    }

    private val _uiState = MutableStateFlow(
        ChartsUiState(
            selectedYear = initialYear,
            selectedMonth = initialMonth
        )
    )
    val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()

    init {
        checkMonthRollover()
        loadData()
    }

    fun checkMonthRollover() {
        if (preferenceManager.checkAndSyncMonthRollover()) {
            val newMonth = preferenceManager.getChartsSelectedMonth()
            val newYear = preferenceManager.getChartsSelectedYear()
            savedStateHandle["selectedMonth"] = newMonth
            savedStateHandle["selectedYear"] = newYear
            _uiState.update { state ->
                computeCharts(state.transactions, newYear, newMonth)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getTransactions()
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { transactions ->
                    _uiState.update { state ->
                        computeCharts(transactions, state.selectedYear, state.selectedMonth)
                    }
                }
        }
    }

    fun setYear(year: Int) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val validMonth = if (year == currentYear && _uiState.value.selectedMonth != null && _uiState.value.selectedMonth!! > currentMonth) {
            currentMonth
        } else {
            _uiState.value.selectedMonth
        }

        savedStateHandle["selectedYear"] = year
        savedStateHandle["selectedMonth"] = validMonth
        preferenceManager.setChartsSelectedYear(year)
        preferenceManager.setChartsSelectedMonth(validMonth)
        _uiState.update { state ->
            computeCharts(state.transactions, year, validMonth)
        }
    }

    fun setMonth(month: Int?) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val validMonth = if (_uiState.value.selectedYear == currentYear && month != null && month > currentMonth) {
            currentMonth
        } else {
            month
        }

        savedStateHandle["selectedMonth"] = validMonth
        preferenceManager.setChartsSelectedMonth(validMonth)
        _uiState.update { state ->
            computeCharts(state.transactions, state.selectedYear, validMonth)
        }
    }

    private fun computeCharts(transactions: List<Transaction>, year: Int, month: Int?): ChartsUiState {
        val calendar = Calendar.getInstance()
        val periodTx = transactions.filter {
            calendar.time = Date(it.date)
            val txYear = calendar.get(Calendar.YEAR)
            val txMonth = calendar.get(Calendar.MONTH)
            txYear == year && (month == null || txMonth == month)
        }

        // Expense by category
        val expGroups = periodTx
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
        val totalExp = expGroups.values.sum()
        val expenseByCategory = expGroups.map { (cat, amt) ->
            CategoryShare(cat, amt, if (totalExp > 0) (amt / totalExp * 100).toFloat() else 0f)
        }.sortedByDescending { it.amount }

        // Income by category
        val incGroups = periodTx
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
        val totalInc = incGroups.values.sum()
        val incomeByCategory = incGroups.map { (cat, amt) ->
            CategoryShare(cat, amt, if (totalInc > 0) (amt / totalInc * 100).toFloat() else 0f)
        }.sortedByDescending { it.amount }

        // Dynamic chart data
        val chartData = if (month == null) {
            (0..11).map { monthIndex ->
                val monthTx = periodTx.filter {
                    calendar.time = Date(it.date)
                    calendar.get(Calendar.MONTH) == monthIndex
                }
                val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                ChartBarData(getMonthName(monthIndex), monthIndex, income, expense)
            }
        } else {
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            (1..daysInMonth).map { day ->
                val dayTx = periodTx.filter {
                    calendar.time = Date(it.date)
                    calendar.get(Calendar.DAY_OF_MONTH) == day
                }
                val income = dayTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = dayTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                ChartBarData(day.toString(), day, income, expense)
            }
        }

        return ChartsUiState(
            transactions = transactions,
            expenseByCategory = expenseByCategory,
            incomeByCategory = incomeByCategory,
            chartData = chartData,
            selectedYear = year,
            selectedMonth = month,
            isLoading = false
        )
    }

    private fun getMonthName(idx: Int): String {
        val months = listOf("Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des")
        return months[idx]
    }
}
