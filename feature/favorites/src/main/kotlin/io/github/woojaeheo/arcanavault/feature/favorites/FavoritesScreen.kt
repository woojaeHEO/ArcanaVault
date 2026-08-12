package io.github.woojaeheo.arcanavault.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.woojaeheo.arcanavault.core.model.Card
import io.github.woojaeheo.arcanavault.core.designsystem.glassSurface

@Composable
fun FavoritesScreen(cards: List<Card>, columns: Int) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("Favorites", style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(top = 14.dp))
        Text("오프라인에서도 바로 확인할 수 있는 카드", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(
                minSize = when (columns.coerceIn(2, 5)) {
                    2 -> 190.dp
                    3 -> 164.dp
                    4 -> 140.dp
                    else -> 120.dp
                },
            ),
            contentPadding = PaddingValues(vertical = 18.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(cards, key = Card::id) { card ->
                Column(Modifier.glassSurface(24.dp, MaterialTheme.colorScheme.surface).padding(9.dp)) {
                    AsyncImage(
                        card.imageUrl,
                        card.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.aspectRatio(.716f).clip(RoundedCornerShape(18.dp)),
                    )
                    Text(card.name, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = 7.dp))
                    Text(card.setName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                }
            }
        }
    }
}
