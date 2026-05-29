package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Centralized type styles for the dramatic Bold Bold Typography theme
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Italic,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 2.sp
    )
)

/**
 * A sleek, high-efficiency Markdown parser that transforms bold asterisks (**) and
 * underscores (__) into crisp bold text styling for a premium startup feel.
 */
fun parseMarkdown(text: String?): androidx.compose.ui.text.AnnotatedString {
    if (text == null) return androidx.compose.ui.text.AnnotatedString("")
    val normalized = text.replace("__", "**")
    return androidx.compose.ui.text.buildAnnotatedString {
        var cursor = 0
        while (cursor < normalized.length) {
            val boldStart = normalized.indexOf("**", cursor)
            if (boldStart == -1) {
                append(normalized.substring(cursor))
                break
            }
            if (boldStart > cursor) {
                append(normalized.substring(cursor, boldStart))
            }
            val boldEnd = normalized.indexOf("**", boldStart + 2)
            if (boldEnd == -1) {
                append("**")
                cursor = boldStart + 2
            } else {
                pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                append(normalized.substring(boldStart + 2, boldEnd))
                pop()
                cursor = boldEnd + 2
            }
        }
    }
}


