package com.iyas.budgetin.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Menampilkan angka dengan pemisah ribuan titik (3000000 ditampilkan 3.000.000).
 * Nilai yang tersimpan di state tetap digit murni, jadi parsing angka tidak
 * perlu membersihkan titik terlebih dahulu.
 *
 * Input diasumsikan hanya berisi digit — pemanggil sudah memfilternya.
 */
object ThousandSeparatorTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                append(char)
                val remaining = digits.length - index - 1
                if (remaining > 0 && remaining % 3 == 0) append('.')
            }
        }

        val offsetMapping = object : OffsetMapping {
            // Posisi kursor pada digit murni -> posisi pada teks bertitik
            override fun originalToTransformed(offset: Int): Int {
                val safe = offset.coerceIn(0, digits.length)
                if (safe == 0) return 0
                var digitsSeen = 0
                formatted.forEachIndexed { index, char ->
                    if (char != '.') {
                        digitsSeen++
                        if (digitsSeen == safe) return index + 1
                    }
                }
                return formatted.length
            }

            // Posisi kursor pada teks bertitik -> posisi pada digit murni
            override fun transformedToOriginal(offset: Int): Int {
                val safe = offset.coerceIn(0, formatted.length)
                return safe - formatted.take(safe).count { it == '.' }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
