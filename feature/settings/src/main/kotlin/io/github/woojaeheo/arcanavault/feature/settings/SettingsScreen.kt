package io.github.woojaeheo.arcanavault.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var gridDensity by remember(preferences.gridDensity) {
        mutableFloatStateOf(preferences.gridDensity.toFloat())
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))
        Column(Modifier.fillMaxWidth().glassSurface().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(selected = preferences.themeMode == mode, onClick = { onTheme(mode) }, label = { Text(mode.name) })
                }
            }
            SettingSwitch("Dynamic color", preferences.dynamicColor, onDynamicColor)
            SettingSwitch("Reduce motion", preferences.reducedMotion, onReducedMotion)
            Text("Grid density · ${gridDensity.roundToInt()}")
            Slider(
                value = gridDensity,
                onValueChange = { gridDensity = it },
                onValueChangeFinished = { onGridDensity(gridDensity.roundToInt()) },
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
