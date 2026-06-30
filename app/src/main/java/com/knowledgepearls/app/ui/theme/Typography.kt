package com.knowledgepearls.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val pearlTypeScale = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = (-0.35).sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        letterSpacing = 1.1.sp,
    ),
)

fun pearlTypography(fontChoice: AppFontChoice = AppFontChoice.Inter): Typography {
    val fontFamily = fontFamilyFor(fontChoice)
    return Typography(
        displayLarge = pearlTypeScale.displayLarge.withFontFamily(fontFamily),
        displayMedium = pearlTypeScale.displayMedium.withFontFamily(fontFamily),
        displaySmall = pearlTypeScale.displaySmall.withFontFamily(fontFamily),
        headlineLarge = pearlTypeScale.headlineLarge.withFontFamily(fontFamily),
        headlineMedium = pearlTypeScale.headlineMedium.withFontFamily(fontFamily),
        headlineSmall = pearlTypeScale.headlineSmall.withFontFamily(fontFamily),
        titleLarge = pearlTypeScale.titleLarge.withFontFamily(fontFamily),
        titleMedium = pearlTypeScale.titleMedium.withFontFamily(fontFamily),
        titleSmall = pearlTypeScale.titleSmall.withFontFamily(fontFamily),
        bodyLarge = pearlTypeScale.bodyLarge.withFontFamily(fontFamily),
        bodyMedium = pearlTypeScale.bodyMedium.withFontFamily(fontFamily),
        bodySmall = pearlTypeScale.bodySmall.withFontFamily(fontFamily),
        labelLarge = pearlTypeScale.labelLarge.withFontFamily(fontFamily),
        labelMedium = pearlTypeScale.labelMedium.withFontFamily(fontFamily),
        labelSmall = pearlTypeScale.labelSmall.withFontFamily(fontFamily),
    )
}

private fun TextStyle.withFontFamily(fontFamily: FontFamily): TextStyle = copy(fontFamily = fontFamily)
