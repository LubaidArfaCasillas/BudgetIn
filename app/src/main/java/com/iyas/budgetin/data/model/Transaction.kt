package com.iyas.budgetin.data.model

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.INCOME,
    val category: String = "",
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)

enum class TransactionType {
    INCOME, EXPENSE
}
