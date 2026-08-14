package com.iyas.budgetin.utils

import androidx.compose.ui.graphics.Color
import com.iyas.budgetin.ui.theme.*
import kotlin.math.absoluteValue

/**
 * Palet warna solid (Neo Brutalism) untuk grafik.
 * Terdiri dari warna-warna vibrant. 
 * Tidak mengandung warna merah atau hijau (dan warna yang menyerupainya).
 */
private val neoBrutalismPalette = listOf(
    NeoYellow,         // 0. Yellow
    NeoPurple,         // 1. Purple
    ChartColor8,       // 2. Amber / Orange
    Color(0xFF64B5F6), // 3. Sky Blue
    Color(0xFFBA68C8), // 4. Light Purple
    Color(0xFFFFB74D), // 5. Orange
    Color(0xFF4DD0E1), // 6. Cyan (Blue)
    Color(0xFF7986CB), // 7. Indigo
    Color(0xFFFFCA28), // 8. Light Yellow / Amber
    Color(0xFF9FA8DA), // 9. Light Indigo
    Color(0xFFCE93D8), // 10. Lavender
    Color(0xFF29B6F6), // 11. Light Blue
    Color(0xFFAB47BC), // 12. Medium Purple
    Color(0xFFFFA726), // 13. Orange
    Color(0xFF5C6BC0)  // 14. Deep Indigo
)

/**
 * Warna hardcoded untuk kategori default agar selalu konsisten.
 * Indeks merujuk ke neoBrutalismPalette.
 */
private val defaultCategoryColorIndex = mapOf(
    // Income
    "Gaji" to 6,          // Cyan
    "Freelance" to 0,     // NeoYellow
    "Investasi" to 1,     // NeoPurple
    "Bisnis" to 5,        // Orange
    "Hadiah" to 10,       // Lavender

    // Expense
    "Makan & Minum" to 2, // ChartColor8 (Amber)
    "Transportasi" to 3,  // Sky Blue
    "Belanja" to 13,      // Orange (Menghindari NeoPink/Merah)
    "Tagihan" to 7,       // Indigo
    "Hiburan" to 4,       // Light Purple
    "Kesehatan" to 11,    // Light Blue
    "Pendidikan" to 14,   // Deep Indigo
    "Tabungan" to 8,      // Light Yellow
)

/**
 * Mengembalikan warna dari palet Neo Brutalism berdasarkan nama kategori secara deterministik.
 */
fun getCategoryColor(categoryName: String): Color {
    val index = defaultCategoryColorIndex[categoryName]
        ?: (categoryName.hashCode().absoluteValue % neoBrutalismPalette.size)
    return neoBrutalismPalette[index]
}
