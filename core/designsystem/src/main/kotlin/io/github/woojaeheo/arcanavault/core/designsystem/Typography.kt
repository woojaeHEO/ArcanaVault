package io.github.woojaeheo.arcanavault.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Pretendard = FontFamily(Font(R.font.pretendard_regular))

val ArcanaTypography = Typography(
    displayLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Black, fontSize = 52.sp, letterSpacing = (-1.5).sp),
    headlineLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = (-0.7).sp),
    titleLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
)
