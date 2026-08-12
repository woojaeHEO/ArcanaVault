package io.github.woojaeheo.holobinder.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.woojaeheo.holobinder.core.designsystem.HoloBinderCardImage
import io.github.woojaeheo.holobinder.core.designsystem.HoloBinderCardImageSize
import io.github.woojaeheo.holobinder.core.model.Card
import io.github.woojaeheo.holobinder.core.designsystem.glassSurface

@Composable
fun FavoritesScreen(cards: List<Card>, columns: Int) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val expandedHeader = maxWidth >= 600.dp
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
            if (expandedHeader) {
                Text("Favorites", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp))
            }
            Text(
                "저장한 카드 ${cards.size}장",
                style = if (expandedHeader) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = if (expandedHeader) 0.dp else 4.dp),
            )
            if (cards.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("아직 저장한 카드가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                return@Column
            }
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(
                    minSize = when (columns.coerceIn(2, 5)) {
                        2 -> 178.dp
                        3 -> 146.dp
                        4 -> 126.dp
                        else -> 112.dp
                    },
                ),
                contentPadding = PaddingValues(top = 10.dp, bottom = 18.dp, start = 2.dp, end = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(cards, key = Card::id) { card ->
                    Column(Modifier.glassSurface(24.dp, MaterialTheme.colorScheme.surface).padding(9.dp)) {
                        HoloBinderCardImage(
                            imageUrl = card.imageUrl,
                            contentDescription = card.name,
                            size = HoloBinderCardImageSize.Grid,
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
}
