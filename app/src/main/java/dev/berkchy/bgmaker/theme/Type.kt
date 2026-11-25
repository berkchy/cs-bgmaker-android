package dev.berkchy.bgmaker.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.berkchy.bgmaker.R

val GoogleSans = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal),
    Font(R.font.google_sans_medium, FontWeight.Medium),
    Font(R.font.google_sans_bold, FontWeight.Bold),
)

private val Base = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.2.sp),
)

private fun Typography.withGoogleSans(): Typography = Typography(
    displayLarge = displayLarge.copy(fontFamily = GoogleSans),
    displayMedium = displayMedium.copy(fontFamily = GoogleSans),
    displaySmall = displaySmall.copy(fontFamily = GoogleSans),
    headlineLarge = headlineLarge.copy(fontFamily = GoogleSans),
    headlineMedium = headlineMedium.copy(fontFamily = GoogleSans),
    headlineSmall = headlineSmall.copy(fontFamily = GoogleSans),
    titleLarge = titleLarge.copy(fontFamily = GoogleSans),
    titleMedium = titleMedium.copy(fontFamily = GoogleSans),
    titleSmall = titleSmall.copy(fontFamily = GoogleSans),
    bodyLarge = bodyLarge.copy(fontFamily = GoogleSans),
    bodyMedium = bodyMedium.copy(fontFamily = GoogleSans),
    bodySmall = bodySmall.copy(fontFamily = GoogleSans),
    labelLarge = labelLarge.copy(fontFamily = GoogleSans),
    labelMedium = labelMedium.copy(fontFamily = GoogleSans),
    labelSmall = labelSmall.copy(fontFamily = GoogleSans),
)

val BgmakerTypography = Base.withGoogleSans()
