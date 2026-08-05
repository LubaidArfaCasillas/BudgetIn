package com.iyas.budgetin.utils

import com.iyas.budgetin.data.model.Transaction
import com.iyas.budgetin.data.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    format.minimumFractionDigits = 0
    return "Rp ${format.format(amount)}"
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

fun formatDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

fun formatMonthYear(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

fun formatMonth(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

fun getMonthName(monthIndex: Int): String {
    val months = listOf("Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des")
    return months.getOrElse(monthIndex) { "" }
}

fun groupTransactionsByMonth(transactions: List<Transaction>): Map<String, List<Transaction>> {
    return transactions.groupBy { formatMonthYear(it.date) }
}

val INCOME_CATEGORIES = listOf(
    "Gaji", "Freelance", "Investasi", "Bisnis", "Hadiah"
)

val EXPENSE_CATEGORIES = listOf(
    "Makan & Minum", "Transportasi", "Belanja", "Tagihan", "Hiburan",
    "Kesehatan", "Pendidikan", "Tabungan"
)

fun getTransactionIcon(type: TransactionType): String {
    return if (type == TransactionType.INCOME) "💰" else "💸"
}
