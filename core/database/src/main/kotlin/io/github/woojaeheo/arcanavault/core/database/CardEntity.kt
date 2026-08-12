package io.github.woojaeheo.arcanavault.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 로컬 카드 캐시 */
@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val name: String,
    val supertype: String,
    val subtypes: String,
    val hp: Int?,
    val types: String,
    val description: String,
    val weakness: String?,
    val retreatCost: Int,
    val setName: String,
    val setSeries: String,
    val releaseDate: String,
    val number: String,
    val rarity: String?,
    val artist: String?,
    val imageUrl: String,
    val largeImageUrl: String,
    val price: Double?,
    val isFavorite: Boolean,
    val updatedAt: Long,
)

/** 덱 카드 수량 */
@Entity(tableName = "deck_cards")
data class DeckCardEntity(
    @PrimaryKey val cardId: String,
    val quantity: Int,
)
