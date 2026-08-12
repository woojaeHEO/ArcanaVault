package io.github.woojaeheo.arcanavault.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.woojaeheo.arcanavault.core.designsystem.glassSurface
import io.github.woojaeheo.arcanavault.core.model.ThemeMode
import io.github.woojaeheo.arcanavault.core.model.UserPreferences
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onTheme: (ThemeMode) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onReducedMotion: (Boolean) -> Unit,
    onGridDensity: (Int) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Settings", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().glassSurface().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(selected = preferences.themeMode == mode, onClick = { onTheme(mode) }, label = { Text(mode.name) })
                }
            }
            SettingSwitch("Dynamic color", preferences.dynamicColor, onDynamicColor)
            SettingSwitch("Reduce motion", preferences.reducedMotion, onReducedMotion)
            Text("Grid density · ${preferences.gridDensity}")
            Slider(
                value = preferences.gridDensity.toFloat(),
                onValueChange = { onGridDensity(it.roundToInt()) },
                valueRange = 2f..5f,
                steps = 2,
            )
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked, onChecked)
    }
}
