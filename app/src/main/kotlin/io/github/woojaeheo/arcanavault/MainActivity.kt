package io.github.woojaeheo.arcanavault

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import dagger.hilt.android.AndroidEntryPoint
import io.github.woojaeheo.arcanavault.core.designsystem.ArcanaTheme
import io.github.woojaeheo.arcanavault.core.designsystem.AuroraBackground
import io.github.woojaeheo.arcanavault.core.designsystem.LocalArcanaMotion
import io.github.woojaeheo.arcanavault.core.designsystem.glassSurface
import io.github.woojaeheo.arcanavault.core.model.ThemeMode
import io.github.woojaeheo.arcanavault.feature.catalog.CatalogRoute
import io.github.woojaeheo.arcanavault.feature.deck.DeckScreen
import io.github.woojaeheo.arcanavault.feature.favorites.FavoritesScreen
import io.github.woojaeheo.arcanavault.feature.settings.SettingsScreen
import io.github.woojaeheo.prismglass.PrismGlassNavigationBar
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            var layoutInfo by remember { mutableStateOf<WindowLayoutInfo?>(null) }
            LaunchedEffect(Unit) {
                WindowInfoTracker.getOrCreate(this@MainActivity).windowLayoutInfo(this@MainActivity).collect {
                    layoutInfo = it
                }
            }
            val dark = when (state.preferences.themeMode) {
                ThemeMode.Dark -> true
                ThemeMode.Light -> false
                ThemeMode.System -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            ArcanaTheme(dark, state.preferences.dynamicColor, state.preferences.reducedMotion) {
                ArcanaApp(
                    state = state,
                    foldingFeature = layoutInfo?.displayFeatures?.filterIsInstance<FoldingFeature>()?.firstOrNull(),
                    onAction = viewModel::onAction,
                    onOpenExternalDisplay = ::openExternalDisplay,
                )
            }
        }
    }

    /** 연결된 프레젠테이션 디스플레이에 보조 화면 실행 */
    private fun openExternalDisplay() {
        val manager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = manager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION).firstOrNull() ?: return
        val options = ActivityOptions.makeBasic().setLaunchDisplayId(display.displayId)
        startActivity(Intent(this, ExternalDisplayActivity::class.java), options.toBundle())
    }
}

private data class DestinationItem(val destination: ArcanaDestination, val label: String, val icon: ImageVector)

private val destinations = listOf(
    DestinationItem(ArcanaDestination.Catalog, "Binder", Icons.Default.Search),
    DestinationItem(ArcanaDestination.Deck, "Deck Lab", Icons.Default.Layers),
    DestinationItem(ArcanaDestination.Favorites, "Vault", Icons.Default.Favorite),
    DestinationItem(ArcanaDestination.Settings, "Settings", Icons.Default.Settings),
)

@Composable
private fun ArcanaApp(
    state: MainState,
    foldingFeature: FoldingFeature?,
    onAction: (MainAction) -> Unit,
    onOpenExternalDisplay: () -> Unit,
) {
    BackHandler(enabled = state.destination != ArcanaDestination.Catalog) {
        onAction(MainAction.SelectDestination(ArcanaDestination.Catalog))
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp
        AuroraBackground()
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = { HoloTopBar(state.destination) },
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (!useRail) ArcanaNavigationBar(state.destination) {
                    onAction(MainAction.SelectDestination(it))
                }
            },
        ) { contentPadding ->
            Row(Modifier.fillMaxSize().padding(contentPadding)) {
                if (useRail) ArcanaNavigationRail(state.destination, onOpenExternalDisplay) {
                    onAction(MainAction.SelectDestination(it))
                }
                Box(Modifier.weight(1f).fillMaxSize()) {
                    AnimatedContent(
                        targetState = state.destination,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "main-destination",
                    ) { destination ->
                        when (destination) {
                            ArcanaDestination.Catalog -> CatalogRoute(
                                columns = state.preferences.gridDensity,
                                onMessage = { message -> scope.launch { snackbar.showSnackbar(message) } },
                            )
                            ArcanaDestination.Deck -> DeckScreen(state.deck) { onAction(MainAction.RemoveFromDeck(it)) }
                            ArcanaDestination.Favorites -> FavoritesScreen(state.favorites, state.preferences.gridDensity)
                            ArcanaDestination.Settings -> SettingsScreen(
                                preferences = state.preferences,
                                onTheme = { onAction(MainAction.Theme(it)) },
                                onDynamicColor = { onAction(MainAction.DynamicColor(it)) },
                                onReducedMotion = { onAction(MainAction.ReducedMotion(it)) },
                                onGridDensity = { onAction(MainAction.GridDensity(it)) },
                            )
                        }
                    }
                    if (foldingFeature?.isSeparating == true) {
                        Text(
                            "Fold-aware layout",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.TopCenter).background(MaterialTheme.colorScheme.tertiaryContainer).padding(6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HoloTopBar(destination: ArcanaDestination) {
    val title = when (destination) {
        ArcanaDestination.Catalog -> "Holo Binder"
        ArcanaDestination.Deck -> "Deck Laboratory"
        ArcanaDestination.Favorites -> "Collector Vault"
        ArcanaDestination.Settings -> "Studio Controls"
    }
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        val compact = maxWidth < 600.dp
        val showStatus = maxWidth >= 520.dp
        Row(
            modifier = Modifier.fillMaxWidth()
                .height(if (compact) 48.dp else 58.dp)
                .glassSurface(if (compact) 20.dp else 24.dp, MaterialTheme.colorScheme.surface)
                .padding(horizontal = if (compact) 12.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = .16f), androidx.compose.foundation.shape.CircleShape)
                    .padding(if (compact) 7.dp else 10.dp),
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f)) {
                if (!compact) {
                    Text("ARCANA / POKÉMON TCG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    title,
                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                )
            }
            if (showStatus) {
                Text(
                    "LOCAL FIRST",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.background(MaterialTheme.colorScheme.tertiary.copy(alpha = .14f), androidx.compose.foundation.shape.CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ArcanaNavigationBar(selected: ArcanaDestination, onSelect: (ArcanaDestination) -> Unit) {
    val motion = LocalArcanaMotion.current
    val selectedItem = destinations.first { it.destination == selected }
    PrismGlassNavigationBar(
        items = destinations,
        selectedItem = selectedItem,
        onItemSelected = { onSelect(it.destination) },
        itemLabel = DestinationItem::label,
        reducedMotion = motion.reduced,
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 6.dp),
    ) { item, isSelected ->
        val iconScale by animateFloatAsState(
            targetValue = if (isSelected) 1.12f else .94f,
            animationSpec = motion.springSpec(),
            label = "navigation-icon-scale",
        )
        Icon(
            item.icon,
            null,
            tint = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
            },
        )
        Text(
            item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ArcanaNavigationRail(
    selected: ArcanaDestination,
    onOpenExternalDisplay: () -> Unit,
    onSelect: (ArcanaDestination) -> Unit,
) {
    NavigationRail(
        modifier = Modifier.padding(10.dp).glassSurface(30.dp, MaterialTheme.colorScheme.surface),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        destinations.forEach { item ->
            NavigationRailItem(
                selected = selected == item.destination,
                onClick = { onSelect(item.destination) },
                icon = { Icon(item.icon, item.label) },
                label = { Text(item.label) },
            )
        }
        NavigationRailItem(
            selected = false,
            onClick = onOpenExternalDisplay,
            icon = { Icon(Icons.Default.Cast, "외부 화면") },
            label = { Text("Display") },
        )
    }
}
