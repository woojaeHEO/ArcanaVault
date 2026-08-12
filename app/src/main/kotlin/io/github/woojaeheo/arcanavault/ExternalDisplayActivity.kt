package io.github.woojaeheo.arcanavault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.woojaeheo.arcanavault.core.designsystem.ArcanaTheme
import io.github.woojaeheo.arcanavault.core.designsystem.AuroraBackground

/** 외부 디스플레이 전용 관전 화면 */
class ExternalDisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcanaTheme(darkTheme = true, dynamicColor = false) {
                val pulse by rememberInfiniteTransition(label = "external-pulse").animateFloat(
                    initialValue = .96f,
                    targetValue = 1.04f,
                    animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
                    label = "external-logo",
                )
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        AuroraBackground()
                        Column(
                            Modifier.align(Alignment.Center).padding(48.dp).scale(pulse),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text("ARCANA", style = MaterialTheme.typography.displayLarge)
                            Text("VAULT", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                            Text("Build your collection. Find your spark.", textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
