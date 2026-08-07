package com.iyas.budgetin.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyas.budgetin.data.model.Category
import com.iyas.budgetin.data.model.Transaction
import com.iyas.budgetin.data.model.TransactionType
import com.iyas.budgetin.domain.repository.CategoryRepository
import com.iyas.budgetin.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FilterType { ALL, INCOME, EXPENSE }

data class HistoryUiState(
    val allTransactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val filterType: FilterType = FilterType.ALL,
    val selectedCategory: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    // For add/edit
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // Kategori kustom
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isSavingCategory = MutableStateFlow(false)
    val isSavingCategory: StateFlow<Boolean> = _isSavingCategory.asStateFlow()

    private val _categorySaveError = MutableStateFlow<String?>(null)
    val categorySaveError: StateFlow<String?> = _categorySaveError.asStateFlow()

    private val _categorySaveSuccess = MutableStateFlow(false)
    val categorySaveSuccess: StateFlow<Boolean> = _categorySaveSuccess.asStateFlow()

    private val _isDeletingCategory = MutableStateFlow(false)
    val isDeletingCategory: StateFlow<Boolean> = _isDeletingCategory.asStateFlow()

    private val _categoryDeleteError = MutableStateFlow<String?>(null)
    val categoryDeleteError: StateFlow<String?> = _categoryDeleteError.asStateFlow()

    private val _categoryDeleteSuccess = MutableStateFlow(false)
    val categoryDeleteSuccess: StateFlow<Boolean> = _categoryDeleteSuccess.asStateFlow()

    init {
        loadTransactions()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { list ->
                _categories.value = list
            }
        }
    }

    fun addCategory(name: String, type: TransactionType) {
        viewModelScope.launch {
            _isSavingCategory.value = true
            val result = categoryRepository.addCategory(Category(name = name, type = type))
            _isSavingCategory.value = false
            if (result.isSuccess) {
                _categorySaveSuccess.value = true
            } else {
                _categorySaveError.value = result.exceptionOrNull()?.message ?: "Gagal menambah kategori"
            }
        }
    }

    fun resetCategorySaveState() {
        _categorySaveSuccess.value = false
        _categorySaveError.value = null
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _isDeletingCategory.value = true
            val result = categoryRepository.deleteCategory(id)
            _isDeletingCategory.value = false
            if (result.isSuccess) {
                _categoryDeleteSuccess.value = true
            } else {
                _categoryDeleteError.value = result.exceptionOrNull()?.message ?: "Gagal menghapus kategori"
            }
        }
    }

    fun resetCategoryDeleteState() {
        _categoryDeleteSuccess.value = false
        _categoryDeleteError.value = null
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getTransactions()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { transactions ->
                    _uiState.update { state ->
                        val filtered = applyFilters(transactions, state.searchQuery, state.filterType, state.selectedCategory)
                        state.copy(allTransactions = transactions, filteredTransactions = filtered, isLoading = false)
                    }
                }
        }
    }

    fun setSearch(query: String) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allTransactions, query, state.filterType, state.selectedCategory)
            state.copy(searchQuery = query, filteredTransactions = filtered)
        }
    }

    fun setFilter(filterType: FilterType) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allTransactions, state.searchQuery, filterType, state.selectedCategory)
            state.copy(filterType = filterType, filteredTransactions = filtered)
        }
    }

    fun setCategory(category: String?) {
        _uiState.update { state ->
            val filtered = applyFilters(state.allTransactions, state.searchQuery, state.filterType, category)
            state.copy(selectedCategory = category, filteredTransactions = filtered)
        }
    }

    private fun applyFilters(
        transactions: List<Transaction>,
        query: String,
        filterType: FilterType,
        category: String?
    ): List<Transaction> {
        return transactions
            .filter { tx ->
                when (filterType) {
                    FilterType.INCOME -> tx.type == TransactionType.INCOME
                    FilterType.EXPENSE -> tx.type == TransactionType.EXPENSE
                    FilterType.ALL -> true
                }
            }
            .filter { tx -> category == null || tx.category == category }
            .filter { tx ->
                query.isBlank() ||
                        tx.category.contains(query, ignoreCase = true) ||
                        tx.note.contains(query, ignoreCase = true)
            }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.addTransaction(transaction)
            _isSaving.value = false
            if (result.isSuccess) {
                _saveSuccess.value = true
            } else {
                _saveError.value = result.exceptionOrNull()?.message ?: "Gagal menyimpan"
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _isSaving.value = true
            // Tipe transaksi tidak boleh berubah saat edit, selalu pakai tipe aslinya
            val original = _uiState.value.allTransactions.firstOrNull { it.id == transaction.id }
            val updated = if (original != null) transaction.copy(type = original.type) else transaction
            val result = repository.updateTransaction(updated)
            _isSaving.value = false
            if (result.isSuccess) {
                _saveSuccess.value = true
            } else {
                _saveError.value = result.exceptionOrNull()?.message ?: "Gagal memperbarui"
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.deleteTransaction(id)
            _isSaving.value = false
            if (result.isSuccess) {
                _saveSuccess.value = true
            } else {
                _saveError.value = result.exceptionOrNull()?.message ?: "Gagal menghapus"
            }
        }
    }

    fun resetSaveState() {
        _saveSuccess.value = false
        _saveError.value = null
    }
}
