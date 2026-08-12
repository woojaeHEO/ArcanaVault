package io.github.woojaeheo.arcanavault.core.designsystem

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** 천천히 이동하는 오로라 배경 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    val motion = LocalArcanaMotion.current
    val background = MaterialTheme.colorScheme.background
    val primaryGlow = MaterialTheme.colorScheme.primary.copy(alpha = .30f)
    val secondaryGlow = MaterialTheme.colorScheme.secondary.copy(alpha = .24f)
    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (motion.reduced) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(12_000), RepeatMode.Reverse),
        label = "aurora-phase",
    )
    Canvas(modifier.fillMaxSize()) {
        drawRect(background)
        drawCircle(
            brush = Brush.radialGradient(listOf(primaryGlow, Color.Transparent)),
            radius = size.minDimension * .72f,
            center = Offset(size.width * (.18f + phase * .18f), size.height * .18f),
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(secondaryGlow, Color.Transparent)),
            radius = size.minDimension * .68f,
            center = Offset(size.width * (.84f - phase * .22f), size.height * .72f),
        )
    }
}
