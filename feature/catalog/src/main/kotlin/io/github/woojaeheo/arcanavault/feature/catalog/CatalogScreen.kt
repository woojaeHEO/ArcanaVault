package io.github.woojaeheo.arcanavault.feature.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.woojaeheo.arcanavault.core.designsystem.LocalArcanaMotion
import io.github.woojaeheo.arcanavault.core.designsystem.glassSurface
import io.github.woojaeheo.arcanavault.core.model.Card
import io.github.woojaeheo.arcanavault.core.model.CardSort
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CatalogRoute(
    columns: Int,
    onMessage: (String) -> Unit,
    viewModel: CatalogViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            if (effect is CatalogEffect.Message) onMessage(effect.text)
        }
    }
    CatalogScreen(state, columns, viewModel::onAction)
}

@Composable
private fun CatalogScreen(
    state: CatalogState,
    preferredDensity: Int,
    onAction: (CatalogAction) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val showSupportingPane = maxWidth >= 940.dp
        Row(Modifier.fillMaxSize()) {
            CatalogPane(
                state = state,
                density = preferredDensity,
                onAction = onAction,
                modifier = Modifier.weight(if (showSupportingPane) .64f else 1f),
            )
            AnimatedVisibility(
                visible = showSupportingPane && state.selectedCard != null,
                enter = fadeIn() + scaleIn(initialScale = .96f),
                exit = fadeOut(),
            ) {
                CardDetail(
                    card = state.selectedCard,
                    onClose = { onAction(CatalogAction.Select(null)) },
                    onFavorite = { state.selectedCard?.let { onAction(CatalogAction.ToggleFavorite(it.id)) } },
                    onAdd = { state.selectedCard?.let { onAction(CatalogAction.AddToDeck(it.id)) } },
                    modifier = Modifier.width(450.dp).fillMaxHeight().padding(12.dp),
                )
            }
        }
        AnimatedVisibility(
            visible = !showSupportingPane && state.selectedCard != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            CardDetail(
                card = state.selectedCard,
                onClose = { onAction(CatalogAction.Select(null)) },
                onFavorite = { state.selectedCard?.let { onAction(CatalogAction.ToggleFavorite(it.id)) } },
                onAdd = { state.selectedCard?.let { onAction(CatalogAction.AddToDeck(it.id)) } },
                modifier = Modifier.fillMaxWidth().fillMaxHeight(.94f).padding(10.dp),
            )
        }
    }
}

@Composable
private fun CatalogPane(
    state: CatalogState,
    density: Int,
    onAction: (CatalogAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 16.dp)) {
        DiscoveryHero(cardCount = state.cards.size, refreshing = state.isRefreshing)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.filter.query,
                onValueChange = { onAction(CatalogAction.Search(it)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("피카츄, 리자몽, 세트 검색") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onAction(CatalogAction.Refresh) }) {
                Icon(Icons.Default.Refresh, "새로고침")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                null to "ALL",
                "Fire" to "FIRE",
                "Water" to "WATER",
                "Lightning" to "LIGHTNING",
                "Psychic" to "PSYCHIC",
                "Dragon" to "DRAGON",
                "Darkness" to "DARK",
            ).forEach { (value, label) ->
                AssistChip(
                    onClick = { onAction(CatalogAction.FilterType(value)) },
                    label = { Text(label) },
                    leadingIcon = if (state.filter.type == value) {
                        { Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp)) }
                    } else null,
                )
            }
            CardSort.entries.forEach { sort ->
                AssistChip(
                    onClick = { onAction(CatalogAction.Sort(sort)) },
                    label = { Text(if (state.filter.sort == sort) "${sort.name} ✓" else sort.name) },
                )
            }
        }
        Box(Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(
                    minSize = when (density.coerceIn(2, 5)) {
                        2 -> 224.dp
                        3 -> 184.dp
                        4 -> 152.dp
                        else -> 132.dp
                    },
                ),
                contentPadding = PaddingValues(bottom = 112.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.cards, key = Card::id) { card ->
                    HoloCard(card = card, onClick = { onAction(CatalogAction.Select(card)) })
                }
            }
            if (state.isLoading || state.isRefreshing) {
                CircularProgressIndicator(Modifier.align(Alignment.Center).size(44.dp))
            }
            if (!state.isLoading && state.cards.isEmpty()) {
                Text("검색 결과가 없습니다.", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun DiscoveryHero(cardCount: Int, refreshing: Boolean) {
    val shape = RoundedCornerShape(28.dp)
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 14.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = .24f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = .16f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = .20f),
                    ),
                ),
                shape,
            )
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .30f), shape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("HOLO INDEX", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("오늘의 카드 아카이브", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(cardCount.toString().padStart(2, '0'), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(if (refreshing) "SYNCING" else "OFFLINE READY", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun HoloCard(card: Card, onClick: () -> Unit) {
    val motion = LocalArcanaMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .955f else 1f, motion.springSpec(), label = "card-press")
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier.scale(scale)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = .72f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = .42f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = .72f),
                    ),
                ),
                shape,
            )
            .padding(1.5.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = .88f))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(8.dp),
    ) {
        Box {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().aspectRatio(.716f).clip(RoundedCornerShape(18.dp)),
            )
            Text(
                card.rarity ?: card.supertype,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    .background(Color.Black.copy(alpha = .62f), CircleShape).padding(horizontal = 9.dp, vertical = 5.dp),
                color = Color.White,
            )
            if (card.isFavorite) {
                Icon(
                    Icons.Default.Favorite,
                    "즐겨찾기",
                    tint = Color(0xFFFF5C8A),
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        .background(Color.Black.copy(alpha = .62f), CircleShape).padding(6.dp),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(card.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Black)
                Text("${card.setName}  #${card.number}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            card.hp?.let { Text("HP $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
private fun CardDetail(
    card: Card?,
    onClose: () -> Unit,
    onFavorite: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (card == null) return
    Column(modifier.glassSurface(32.dp, MaterialTheme.colorScheme.surface).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(card.setSeries.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(card.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            }
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, "닫기") }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            AsyncImage(
                model = card.largeImageUrl,
                contentDescription = card.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().height(380.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 12.dp)) {
                card.types.forEach { StatPill(it.uppercase()) }
                card.hp?.let { StatPill("HP $it") }
                card.weakness?.let { StatPill("WEAK $it") }
            }
            Text("${card.setName} · ${card.rarity ?: card.supertype} · #${card.number}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (card.description.isNotBlank()) {
                Text(card.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 16.dp))
            }
            card.artist?.let { Text("Illustrated by $it", style = MaterialTheme.typography.labelMedium) }
            card.price?.let { Text("Market  $${"%.2f".format(it)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 10.dp)) {
            FilledTonalIconButton(onClick = onFavorite) {
                Icon(if (card.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "즐겨찾기")
            }
            Button(onClick = onAdd, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("덱에 추가")
            }
        }
    }
}

@Composable
private fun StatPill(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = .14f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 7.dp),
    )
}
