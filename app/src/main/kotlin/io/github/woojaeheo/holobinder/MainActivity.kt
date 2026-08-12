package io.github.woojaeheo.holobinder

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
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
import io.github.woojaeheo.holobinder.core.designsystem.HoloBinderTheme
import io.github.woojaeheo.holobinder.core.designsystem.AuroraBackground
import io.github.woojaeheo.holobinder.core.designsystem.LocalHoloBinderMotion
import io.github.woojaeheo.holobinder.core.designsystem.glassSurface
import io.github.woojaeheo.holobinder.core.model.ThemeMode
import io.github.woojaeheo.holobinder.feature.catalog.CatalogRoute
import io.github.woojaeheo.holobinder.feature.deck.DeckScreen
import io.github.woojaeheo.holobinder.feature.favorites.FavoritesScreen
import io.github.woojaeheo.holobinder.feature.settings.SettingsScreen
import io.github.woojaeheo.prismglass.PrismGlassNavigationBar
import io.github.woojaeheo.prismglass.PrismGlassBackdropHost
import io.github.woojaeheo.prismglass.PrismGlassBackdropSurface
import io.github.woojaeheo.prismglass.PrismGlassDefaults
import io.github.woojaeheo.prismglass.PrismGlassNavigationDefaults
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
            HoloBinderTheme(dark, state.preferences.dynamicColor, state.preferences.reducedMotion) {
                HoloBinderApp(
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

private data class DestinationItem(val destination: HoloBinderDestination, val label: String, val icon: ImageVector)

private val destinations = listOf(
    DestinationItem(HoloBinderDestination.Catalog, "Binder", Icons.Default.Search),
    DestinationItem(HoloBinderDestination.Deck, "Deck Lab", Icons.Default.Layers),
    DestinationItem(HoloBinderDestination.Favorites, "Vault", Icons.Default.Favorite),
    DestinationItem(HoloBinderDestination.Settings, "Settings", Icons.Default.Settings),
)

@Composable
private fun HoloBinderApp(
    state: MainState,
    foldingFeature: FoldingFeature?,
    onAction: (MainAction) -> Unit,
    onOpenExternalDisplay: () -> Unit,
) {
    BackHandler(enabled = state.destination != HoloBinderDestination.Catalog) {
        onAction(MainAction.SelectDestination(HoloBinderDestination.Catalog))
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp
        PrismGlassBackdropHost(
            modifier = Modifier.fillMaxSize(),
            background = {
                AuroraBackground()
                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    topBar = { HoloTopBar(state.destination) },
                    snackbarHost = { SnackbarHost(snackbar) },
                ) { contentPadding ->
                    Row(Modifier.fillMaxSize().padding(contentPadding)) {
                        if (useRail) HoloBinderNavigationRail(state.destination, onOpenExternalDisplay) {
                            onAction(MainAction.SelectDestination(it))
                        }
                        Box(Modifier.weight(1f).fillMaxSize()) {
                            AnimatedContent(
                                targetState = state.destination,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "main-destination",
                            ) { destination ->
                                when (destination) {
                                    HoloBinderDestination.Catalog -> CatalogRoute(
                                        columns = state.preferences.gridDensity,
                                        onMessage = { message -> scope.launch { snackbar.showSnackbar(message) } },
                                    )
                                    HoloBinderDestination.Deck -> DeckScreen(state.deck) { onAction(MainAction.RemoveFromDeck(it)) }
                                    HoloBinderDestination.Favorites -> FavoritesScreen(state.favorites, state.preferences.gridDensity)
                                    HoloBinderDestination.Settings -> SettingsScreen(
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
                                    modifier = Modifier.align(Alignment.TopCenter)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer).padding(6.dp),
                                )
                            }
                        }
                    }
                }
            },
        ) { backdrop ->
            if (!useRail) {
                val surfaceStyle = PrismGlassDefaults.surfaceStyle(
                    shape = RoundedCornerShape(30.dp),
                    tint = Color.Black,
                ).copy(
                    highlight = Color.White,
                    primaryEdge = Color.White.copy(alpha = .52f),
                    secondaryEdge = Color.Transparent,
                    borderWidth = .5.dp,
                    shadowElevation = 10.dp,
                )
                PrismGlassBackdropSurface(
                    state = backdrop,
                    style = surfaceStyle,
                    blurRadius = 18.dp,
                    refraction = .12f,
                    clipContent = false,
                    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp).fillMaxWidth().height(64.dp),
                ) {
                    HoloBinderNavigationBar(
                        selected = state.destination,
                        backdropState = backdrop,
                        modifier = Modifier.fillMaxSize(),
                        onSelect = { onAction(MainAction.SelectDestination(it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HoloTopBar(destination: HoloBinderDestination) {
    val title = when (destination) {
        HoloBinderDestination.Catalog -> "Holo Binder"
        HoloBinderDestination.Deck -> "Deck Laboratory"
        HoloBinderDestination.Favorites -> "Collector Vault"
        HoloBinderDestination.Settings -> "Studio Controls"
    }
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
    ) {
        val compact = maxWidth < 600.dp
        val showStatus = maxWidth >= 520.dp
        if (compact) {
            Row(
                modifier = Modifier.fillMaxWidth().height(34.dp).padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(
                    Modifier.background(
                        MaterialTheme.colorScheme.primary,
                        androidx.compose.foundation.shape.CircleShape,
                    ).padding(3.dp),
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "POKÉMON TCG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
            }
            return@BoxWithConstraints
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp)
                .height(58.dp)
                .glassSurface(24.dp, MaterialTheme.colorScheme.surface)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = .16f), androidx.compose.foundation.shape.CircleShape)
                    .padding(10.dp),
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f)) {
                Text("HOLO BINDER / POKÉMON TCG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
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
private fun HoloBinderNavigationBar(
    selected: HoloBinderDestination,
    backdropState: io.github.woojaeheo.prismglass.PrismGlassBackdropState,
    modifier: Modifier = Modifier,
    onSelect: (HoloBinderDestination) -> Unit,
) {
    val motion = LocalHoloBinderMotion.current
    val selectedItem = destinations.first { it.destination == selected }
    val defaultStyle = PrismGlassNavigationDefaults.style()
    val selectedColor = MaterialTheme.colorScheme.primary
    val transparentSurface = defaultStyle.surface.copy(
        tint = Color.Transparent,
        highlight = Color.Transparent,
        primaryEdge = Color.Transparent,
        secondaryEdge = Color.Transparent,
        shadowElevation = 0.dp,
        borderWidth = 0.dp,
    )
    val style = defaultStyle.copy(
        surface = transparentSurface,
        indicator = defaultStyle.indicator.copy(
            tint = Color.Black,
            highlight = Color.White,
            primaryEdge = Color.White.copy(alpha = .62f),
            secondaryEdge = selectedColor.copy(alpha = .48f),
            borderWidth = .75.dp,
            shadowElevation = 8.dp,
        ),
        surfaceFill = Color.Transparent,
        indicatorFill = Color.Black.copy(alpha = .22f),
        height = 64.dp,
    )
    PrismGlassNavigationBar(
        items = destinations,
        selectedItem = selectedItem,
        onItemSelected = { onSelect(it.destination) },
        itemLabel = DestinationItem::label,
        reducedMotion = motion.reduced,
        backdropState = backdropState,
        style = style,
        modifier = modifier,
    ) { item, isSelected ->
        val iconScale by animateFloatAsState(
            targetValue = if (isSelected) 1.12f else .94f,
            animationSpec = motion.springSpec(),
            label = "navigation-icon-scale",
        )
        Icon(
            item.icon,
            null,
            tint = if (isSelected) selectedColor else Color.White.copy(alpha = .54f),
            modifier = Modifier.graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
            },
        )
        Text(
            item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) selectedColor else Color.White.copy(alpha = .54f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HoloBinderNavigationRail(
    selected: HoloBinderDestination,
    onOpenExternalDisplay: () -> Unit,
    onSelect: (HoloBinderDestination) -> Unit,
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
