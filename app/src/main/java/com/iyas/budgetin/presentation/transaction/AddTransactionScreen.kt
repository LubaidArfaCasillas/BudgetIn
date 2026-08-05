package com.iyas.budgetin.presentation.transaction

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import com.iyas.budgetin.ui.components.ThousandSeparatorTransformation
import com.iyas.budgetin.ui.components.neoBrutalism
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
fun EditTransactionScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val transaction = uiState.allTransactions.firstOrNull { it.id == transactionId }

    // Simpan dan hapus sama-sama memicu kembali ke layar sebelumnya, dan penghapusan
    // juga membuat transaksi hilang dari list. Pastikan popBackStack hanya sekali.
    var hasLeft by remember { mutableStateOf(false) }
    val leaveOnce = {
        if (!hasLeft) {
            hasLeft = true
            onNavigateBack()
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveState()
            leaveOnce()
        }
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeoPink, strokeWidth = 4.dp)
            }
        }
        transaction == null -> {
            // Transaksi sudah terhapus atau id tidak valid
            LaunchedEffect(Unit) { leaveOnce() }
        }
        else -> {
            AddTransactionScreenContent(
                onNavigateBack = onNavigateBack,
                onSaveTransaction = viewModel::updateTransaction,
                saveError = saveError,
                isSaving = isSaving,
                initialTransaction = transaction,
                title = "Edit Transaksi",
                saveButtonText = "Simpan Perubahan",
                onDeleteTransaction = { viewModel.deleteTransaction(transaction.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreenContent(
    onNavigateBack: () -> Unit,
    onSaveTransaction: (Transaction) -> Unit,
    saveError: String?,
    isSaving: Boolean,
    initialTransaction: Transaction? = null,
    title: String = "Tambah Transaksi",
    saveButtonText: String = "Simpan Transaksi",
    onDeleteTransaction: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isEditing = initialTransaction != null
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp),
            title = { Text("Hapus Transaksi", color = ExpenseRed, fontWeight = FontWeight.Black) },
            text = {
                Text(
                    "Transaksi ini akan dihapus permanen. Lanjutkan?",
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteTransaction?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.neoBrutalism(cornerRadius = 8.dp, shadowOffset = 2.dp)
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    border = BorderStroke(2.dp, SolidBlack),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SolidBlack)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Nilai awal dipisah agar bisa dipakai dua kali: mengisi form, dan menjadi
    // pembanding untuk mendeteksi perubahan yang belum disimpan
    val initialAmount = remember(initialTransaction) { initialTransaction?.amount?.toLong()?.toString() ?: "" }
    val initialNote = remember(initialTransaction) { initialTransaction?.note ?: "" }
    val initialCategory = remember(initialTransaction) { initialTransaction?.category ?: "" }
    val initialType = remember(initialTransaction) { initialTransaction?.type ?: TransactionType.INCOME }
    // Transaksi tidak boleh bertanggal di masa depan. coerceAtMost juga menjaga
    // data lama yang terlanjur bertanggal maju agar tetap valid saat diedit.
    val initialDate = remember(initialTransaction) {
        (initialTransaction?.date ?: System.currentTimeMillis())
            .coerceAtMost(System.currentTimeMillis())
    }

    var amount by remember { mutableStateOf(initialAmount) }
    var note by remember { mutableStateOf(initialNote) }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var amountError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }

    // Berlaku untuk kedua mode: saat tambah, pembandingnya adalah form kosong,
    // jadi isian apa pun sudah dianggap perubahan yang belum tersimpan
    val hasUnsavedChanges =
        amount != initialAmount ||
                note != initialNote ||
                selectedType != initialType ||
                selectedCategory != initialCategory ||
                selectedDate != initialDate

    var showExitDialog by remember { mutableStateOf(false) }
    val requestExit = {
        if (hasUnsavedChanges) showExitDialog = true else onNavigateBack()
    }

    // Tombol back bawaan HP dicegat hanya bila ada perubahan; selain itu
    // dibiarkan berperilaku normal
    BackHandler(enabled = hasUnsavedChanges) { showExitDialog = true }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = Color.White,
            modifier = Modifier.neoBrutalism(cornerRadius = 16.dp, shadowOffset = 6.dp),
            title = { Text("Belum Disimpan", color = SolidBlack, fontWeight = FontWeight.Black) },
            text = {
                Text(
                    if (isEditing) {
                        "Perubahan pada transaksi ini belum disimpan dan akan hilang. Yakin ingin keluar?"
                    } else {
                        "Transaksi yang kamu isi belum disimpan dan akan hilang. Yakin ingin keluar?"
                    },
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.neoBrutalism(cornerRadius = 8.dp, shadowOffset = 2.dp)
                ) {
                    Text("Keluar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitDialog = false },
                    border = BorderStroke(2.dp, SolidBlack),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SolidBlack)
                ) {
                    Text(if (isEditing) "Lanjut Edit" else "Lanjut Isi", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Buka date picker pada tanggal yang sedang dipilih, bukan selalu hari ini
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = Calendar.getInstance().apply {
                timeInMillis = selectedDate
                set(year, month, day)
            }.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        // Tanggal setelah hari ini dinonaktifkan langsung di kalendernya
        this.datePicker.maxDate = System.currentTimeMillis()
    }

    val categories = if (selectedType == TransactionType.INCOME) INCOME_CATEGORIES else EXPENSE_CATEGORIES

    // Reset kategori saat tipe diubah, tapi jangan hapus kategori awal saat edit
    var lastType by remember { mutableStateOf(selectedType) }
    LaunchedEffect(selectedType) {
        if (selectedType != lastType) {
            selectedCategory = ""
            lastType = selectedType
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Layar ini tanpa Scaffold, jadi inset status bar & navigation bar
            // harus ditangani sendiri agar konten tidak terpotong
            .systemBarsPadding()
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = requestExit,
                    modifier = Modifier
                        .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 2.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SolidBlack)
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = SolidBlack,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 16.dp)
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

            // Type Selector — dikunci saat edit, tipe transaksi tidak boleh dibalik
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(cornerRadius = 20.dp, shadowOffset = 4.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
            ) {
                if (isEditing) {
                    Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        TypeButton(
                            text = if (selectedType == TransactionType.INCOME) "Pemasukan" else "Pengeluaran",
                            emoji = if (selectedType == TransactionType.INCOME) "↑" else "↓",
                            selected = true,
                            isExpense = selectedType == TransactionType.EXPENSE,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            onClick = {}
                        )
                        Text(
                            "Tipe transaksi tidak dapat diubah",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TypeButton(
                            text = "Pemasukan",
                            emoji = "↑",
                            selected = selectedType == TransactionType.INCOME,
                            isExpense = false,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = TransactionType.INCOME }
                        )
                        TypeButton(
                            text = "Pengeluaran",
                            emoji = "↓",
                            selected = selectedType == TransactionType.EXPENSE,
                            isExpense = true,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = TransactionType.EXPENSE }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Amount Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(cornerRadius = 20.dp, shadowOffset = 6.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Nominal", style = MaterialTheme.typography.labelLarge, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Rp",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (selectedType == TransactionType.INCOME) NeoTeal else ExpenseRed,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { v ->
                                amount = v.filter { it.isDigit() }
                                amountError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("0", style = MaterialTheme.typography.headlineMedium, color = TextSecondary, fontWeight = FontWeight.Black) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = ThousandSeparatorTransformation,
                            singleLine = true,
                            isError = amountError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (selectedType == TransactionType.INCOME) NeoTeal else ExpenseRed,
                                unfocusedBorderColor = SolidBlack,
                                focusedTextColor = SolidBlack,
                                unfocusedTextColor = SolidBlack,
                                cursorColor = SolidBlack,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorBorderColor = ExpenseRed
                            ),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    if (amountError) {
                        Text("Masukkan nominal transaksi", color = ExpenseRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Category Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(cornerRadius = 20.dp, shadowOffset = 6.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Kategori", style = MaterialTheme.typography.labelLarge, color = TextSecondary, fontWeight = FontWeight.Bold)
                    if (categoryError) {
                        Text("Pilih kategori", color = ExpenseRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalism(cornerRadius = 20.dp, shadowOffset = 6.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Date picker row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, SolidBlack, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable { datePicker.show() }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SolidBlack, modifier = Modifier.size(20.dp))
                            Text("Tanggal", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(formatDate(selectedDate), color = SolidBlack, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium)
                    }

                    // Note
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Catatan (opsional)", color = TextSecondary, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = SolidBlack, modifier = Modifier.size(20.dp)) },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SolidBlack,
                            unfocusedBorderColor = SolidBlack,
                            focusedTextColor = SolidBlack,
                            unfocusedTextColor = SolidBlack,
                            cursorColor = SolidBlack,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedLabelColor = SolidBlack,
                            unfocusedLabelColor = TextSecondary,
                        )
                    )
                }
            }

            // Error
            AnimatedVisibility(visible = saveError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .neoBrutalism(cornerRadius = 12.dp, shadowOffset = 2.dp)
                        .background(ExpenseRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Text(saveError ?: "", color = ExpenseRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
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
                            id = initialTransaction?.id ?: "",
                            amount = amt,
                            type = selectedType,
                            category = selectedCategory,
                            date = selectedDate.coerceAtMost(System.currentTimeMillis()),
                            note = note.trim()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .neoBrutalism(cornerRadius = 16.dp, shadowOffset = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoTeal),
                contentPadding = PaddingValues(0.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SolidBlack, strokeWidth = 3.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = SolidBlack, modifier = Modifier.size(20.dp))
                        Text(saveButtonText, fontWeight = FontWeight.Black, color = SolidBlack, fontSize = 16.sp)
                    }
                }
            }

            // Delete button — hanya muncul saat mengedit transaksi yang sudah ada
            if (onDeleteTransaction != null) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, ExpenseRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                    contentPadding = PaddingValues(0.dp),
                    enabled = !isSaving
                ) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ExpenseRed
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Hapus Transaksi", fontWeight = FontWeight.Black, color = ExpenseRed, fontSize = 16.sp)
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val color = if (isExpense) ExpenseRed else NeoTeal
    val bgColor = if (selected) color else Color.Transparent
    val textColor = if (selected) if(isExpense) Color.White else SolidBlack else TextSecondary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .then(if (selected) Modifier.border(2.dp, SolidBlack, RoundedCornerShape(14.dp)) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 16.sp, color = textColor, modifier = Modifier.padding(end = 6.dp), fontWeight = FontWeight.Black)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = textColor, fontWeight = if (selected) FontWeight.Black else FontWeight.Bold)
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
    val color = if (type == TransactionType.INCOME) NeoTeal else NeoPink
    val icon = CATEGORY_ICONS[category] ?: "📌"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) color else MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .border(2.dp, if(selected) SolidBlack else DividerColor, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 22.sp, textAlign = TextAlign.Center)
            Text(
                category,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) SolidBlack else TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    BudgetInTheme(darkTheme = false) {
        AddTransactionScreenContent(
            onNavigateBack = {},
            onSaveTransaction = {},
            saveError = null,
            isSaving = false
        )
    }
}
