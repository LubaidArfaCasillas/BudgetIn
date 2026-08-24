package com.iyas.budgetin.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iyas.budgetin.ui.theme.SolidBlack

/**
 * Applies a neo-brutalism border and solid drop shadow to an element.
 * Dioptimalkan tanpa `composed` untuk performa rendering tinggi dan animasi yang mulus.
 */
fun Modifier.neoBrutalism(
    cornerRadius: Dp = 0.dp,
    shadowOffset: Dp = 4.dp,
    shadowColor: Color = SolidBlack,
    borderColor: Color = SolidBlack,
    borderWidth: Dp = 2.dp
): Modifier = this
    .drawBehind {
        if (shadowOffset > 0.dp) {
            drawRoundRect(
                color = shadowColor,
                topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        }
    }
    .border(
        width = borderWidth,
        color = borderColor,
        shape = RoundedCornerShape(cornerRadius)
    )