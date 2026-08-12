package io.github.woojaeheo.holobinder.core.designsystem

import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8C4FF),
    secondary = Color(0xFFE9B5FF),
    tertiary = Color(0xFF72E6D1),
    background = Color(0xFF070912),
    surface = Color(0xFF111522),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4059B7),
    secondary = Color(0xFF7C3F91),
    tertiary = Color(0xFF006B5E),
    background = Color(0xFFF6F7FF),
    surface = Color(0xFFFFFFFF),
)

@Immutable
data class HoloBinderMotion(val reduced: Boolean) {
    val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    fun <T> springSpec() = spring<T>(
        dampingRatio = if (reduced) 1f else Spring.DampingRatioMediumBouncy,
        stiffness = if (reduced) Spring.StiffnessHigh else Spring.StiffnessMediumLow,
    )
}

val LocalHoloBinderMotion = compositionLocalOf { HoloBinderMotion(reduced = false) }

@Composable
fun HoloBinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalHoloBinderMotion provides HoloBinderMotion(reducedMotion)) {
        MaterialTheme(colorScheme = colors, typography = HoloBinderTypography, content = content)
    }
}

/** 반투명 유리 표면 */
fun Modifier.glassSurface(
    radius: Dp = 28.dp,
    tint: Color = Color.White,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .shadow(
            elevation = 18.dp,
            shape = shape,
            ambientColor = tint.copy(alpha = .18f),
            spotColor = Color.Black.copy(alpha = .28f),
        )
        .clip(shape)
        .background(
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = .24f),
                    tint.copy(alpha = .13f),
                    tint.copy(alpha = .055f),
                ),
            ),
        )
        .background(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = .16f), Color.Transparent),
                radius = 720f,
            ),
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = .58f),
                    tint.copy(alpha = .12f),
                    Color.White.copy(alpha = .22f),
                ),
            ),
            shape = shape,
        )
}
