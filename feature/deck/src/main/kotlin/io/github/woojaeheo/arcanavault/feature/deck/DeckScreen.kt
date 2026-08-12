package io.github.woojaeheo.arcanavault.feature.deck

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.woojaeheo.arcanavault.core.designsystem.glassSurface
import io.github.woojaeheo.arcanavault.core.model.DeckCard

@Composable
fun DeckScreen(deck: List<DeckCard>, onRemove: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("My Deck", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            Text("${deck.sumOf { it.quantity }} / 60", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        AnimatedContent(deck.isEmpty(), label = "deck-content") { empty ->
            if (empty) EmptyDeck() else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(deck, key = { it.card.id }) { item ->
                    Row(
                        Modifier.fillMaxWidth().animateContentSize().clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = .65f)).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            item.card.imageUrl,
                            item.card.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(58.dp, 84.dp).clip(RoundedCornerShape(10.dp)),
                        )
                        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text(item.card.name, fontWeight = FontWeight.Bold)
                            Text(
                                listOf(item.card.setName, item.card.types.joinToString(" · ")).filter(String::isNotBlank).joinToString("  /  "),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        Text("×${item.quantity}", modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(.15f), CircleShape).padding(9.dp))
                        IconButton(onClick = { onRemove(item.card.id) }) { Icon(Icons.Default.Delete, "한 장 제거") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDeck() {
    Box(Modifier.fillMaxSize().glassSurface(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Layers, null, modifier = Modifier.size(58.dp))
            Text("아직 덱이 비어 있습니다", style = MaterialTheme.typography.titleLarge)
            Text("카탈로그에서 카드를 추가해 보세요")
        }
    }
}
