package com.iyas.budgetin.presentation.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyas.budgetin.data.model.Transaction
import com.iyas.budgetin.data.model.TransactionType
import com.iyas.budgetin.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class CategoryShare(val category: String, val amount: Double, val percentage: Float)
data class MonthlyData(val month: String, val monthIndex: Int, val income: Double, val expense: Double)

data class ChartsUiState(
    val transactions: List<Transaction> = emptyList(),
    val expenseByCategory: List<CategoryShare> = emptyList(),
    val incomeByCategory: List<CategoryShare> = emptyList(),
    val monthlyData: List<MonthlyData> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isLoading: Boolean = true
)

class ChartsViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartsUiState())
    val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getTransactions()
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { transactions ->
                    _uiState.update { state ->
                        computeCharts(transactions, state.selectedYear)
                    }
                }
        }
    }

    fun setYear(year: Int) {
        _uiState.update { state ->
            computeCharts(state.transactions, year)
        }
    }

    private fun computeCharts(transactions: List<Transaction>, year: Int): ChartsUiState {
        val calendar = Calendar.getInstance()
        val yearlyTx = transactions.filter {
            calendar.time = Date(it.date)
            calendar.get(Calendar.YEAR) == year
        }

        // Expense by category
        val expGroups = yearlyTx
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
        val totalExp = expGroups.values.sum()
        val expenseByCategory = expGroups.map { (cat, amt) ->
            CategoryShare(cat, amt, if (totalExp > 0) (amt / totalExp * 100).toFloat() else 0f)
        }.sortedByDescending { it.amount }

        // Income by category
        val incGroups = yearlyTx
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
        val totalInc = incGroups.values.sum()
        val incomeByCategory = incGroups.map { (cat, amt) ->
            CategoryShare(cat, amt, if (totalInc > 0) (amt / totalInc * 100).toFloat() else 0f)
        }.sortedByDescending { it.amount }

        // Monthly data
        val monthlyData = (0..11).map { monthIndex ->
            val monthTx = yearlyTx.filter {
                calendar.time = Date(it.date)
                calendar.get(Calendar.MONTH) == monthIndex
            }
            val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            MonthlyData(getMonthName(monthIndex), monthIndex, income, expense)
        }

        return ChartsUiState(
            transactions = transactions,
            expenseByCategory = expenseByCategory,
            incomeByCategory = incomeByCategory,
            monthlyData = monthlyData,
            selectedYear = year,
            isLoading = false
        )
    }

    private fun getMonthName(idx: Int): String {
        val months = listOf("Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des")
        return months[idx]
    }
}
