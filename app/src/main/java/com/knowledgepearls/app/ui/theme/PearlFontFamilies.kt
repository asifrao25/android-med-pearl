package com.knowledgepearls.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.knowledgepearls.app.R

private val InterFamily = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal),
    Font(R.font.inter_variable, FontWeight.SemiBold),
    Font(R.font.inter_variable, FontWeight.Bold),
)

private val SourceSans3Family = FontFamily(
    Font(R.font.source_sans_3_regular, FontWeight.Normal),
    Font(R.font.source_sans_3_semibold, FontWeight.SemiBold),
    Font(R.font.source_sans_3_bold, FontWeight.Bold),
)

fun fontFamilyFor(choice: AppFontChoice): FontFamily = when (choice) {
    AppFontChoice.Inter -> InterFamily
    AppFontChoice.SourceSans3 -> SourceSans3Family
    AppFontChoice.System -> FontFamily.SansSerif
}
