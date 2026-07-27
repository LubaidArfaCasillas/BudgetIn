package com.iyas.budgetin.presentation.transaction

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iyas.budgetin.data.model.Transaction
import com.iyas.budgetin.data.model.TransactionType
import com.iyas.budgetin.ui.theme.*
import com.iyas.budgetin.utils.*
import org.koin.androidx.compose.koinViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveState()
            onNavigateBack()
        }
    }

    AddTransactionScreenContent(
        onNavigateBack = onNavigateBack,
        onSaveTransaction = viewModel::addTransaction,
        saveError = saveError,
        isSaving = isSaving
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreenContent(
    onNavigateBack: () -> Unit,
    onSaveTransaction: (Transaction) -> Unit,
    saveError: String?,
    isSaving: Boolean
) {
    val context = LocalContext.current

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var amountError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            selectedDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val categories = if (selectedType == TransactionType.INCOME) INCOME_CATEGORIES else EXPENSE_CATEGORIES

    // Reset category when type changes
    LaunchedEffect(selectedType) {
        selectedCategory = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0D2137), BackgroundDark),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    "Tambah Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Type Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TypeButton(
                        text = "Pengeluaran",
                        emoji = "↓",
                        selected = selectedType == TransactionType.EXPENSE,
                        isExpense = true,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = TransactionType.EXPENSE }
                    )
                    TypeButton(
                        text = "Pemasukan",
                        emoji = "↑",
                        selected = selectedType == TransactionType.INCOME,
                        isExpense = false,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = TransactionType.INCOME }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Amount Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Nominal", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Rp",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { v ->
                                amount = v.filter { it.isDigit() }
                                amountError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("0", style = MaterialTheme.typography.headlineMedium, color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = amountError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                                unfocusedBorderColor = Divider,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = GreenPrimary,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                errorBorderColor = ExpenseRed
                            ),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    if (amountError) {
                        Text("Masukkan nominal transaksi", color = ExpenseRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Category Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Kategori", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    if (categoryError) {
                        Text("Pilih kategori", color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(categories) { cat ->
                            CategoryChip(
                                category = cat,
                                selected = selectedCategory == cat,
                                type = selectedType,
                                onClick = { selectedCategory = cat; categoryError = false }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Date & Note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Date picker row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardLighter)
                            .clickable { datePicker.show() }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                            Text("Tanggal", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(formatDate(selectedDate), color = TextPrimary, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                    }

                    // Note
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Catatan (opsional)", color = TextSecondary) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp)) },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Divider,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = GreenPrimary,
                            focusedContainerColor = CardLighter,
                            unfocusedContainerColor = CardLighter,
                            focusedLabelColor = GreenPrimary,
                            unfocusedLabelColor = TextSecondary,
                        )
                    )
                }
            }

            // Error
            AnimatedVisibility(visible = saveError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(saveError ?: "", color = ExpenseRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt == null || amt <= 0) { amountError = true; return@Button }
                    if (selectedCategory.isBlank()) { categoryError = true; return@Button }
                    onSaveTransaction(
                        Transaction(
                            amount = amt,
                            type = selectedType,
                            category = selectedCategory,
                            date = selectedDate,
                            note = note.trim()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                enabled = !isSaving
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(GreenPrimary, GreenSecondary)),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("Simpan Transaksi", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun TypeButton(
    text: String,
    emoji: String,
    selected: Boolean,
    isExpense: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = if (isExpense) ExpenseRed else IncomeGreen
    val bgColor = if (selected) color else Color.Transparent
    val textColor = if (selected) Color.White else TextSecondary

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 16.sp, color = textColor, modifier = Modifier.padding(end = 6.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = textColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    selected: Boolean,
    type: TransactionType,
    onClick: () -> Unit
) {
    val color = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
    val icon = CATEGORY_ICONS[category] ?: "📌"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.border(2.dp, color, RoundedCornerShape(14.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.15f) else CardLighter
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 22.sp, textAlign = TextAlign.Center)
            Text(
                category,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) color else TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    BudgetInTheme(darkTheme = true) {
        AddTransactionScreenContent(
            onNavigateBack = {},
            onSaveTransaction = {},
            saveError = null,
            isSaving = false
        )
    }
}
