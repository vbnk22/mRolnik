package com.example.mrolnik.ui.theme// Zastąp com.twojaprojekt swoją nazwą pakietu

import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

val typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    /* Zdefiniuj inne style tekstowe, których potrzebujesz:
    displayLarge, displayMedium, displaySmall,
    headlineLarge, headlineMedium, headlineSmall,
    titleMedium, titleSmall,
    bodyMedium, bodySmall,
    labelLarge, labelMedium
    */
)

// Shapes (Przykładowe - dostosuj do swoich potrzeb)
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)