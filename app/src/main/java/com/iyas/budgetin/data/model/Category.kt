package com.iyas.budgetin.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val createdAt: Long = System.currentTimeMillis()
)
