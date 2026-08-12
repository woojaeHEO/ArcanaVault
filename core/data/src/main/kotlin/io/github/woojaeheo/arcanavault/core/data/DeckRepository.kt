package io.github.woojaeheo.arcanavault.core.data

import io.github.woojaeheo.arcanavault.core.database.CardDao
import io.github.woojaeheo.arcanavault.core.database.DeckCardEntity
import io.github.woojaeheo.arcanavault.core.database.DeckDao
import io.github.woojaeheo.arcanavault.core.model.DeckCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

interface DeckRepository {
    val deck: Flow<List<DeckCard>>
    suspend fun add(cardId: String)
    suspend fun remove(cardId: String)
}

@Singleton
class OfflineDeckRepository @Inject constructor(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
) : DeckRepository {
    override val deck: Flow<List<DeckCard>> = combine(
        deckDao.observeDeck(),
        cardDao.observeCards("", null, null, "Name"),
    ) { deck, cards ->
        val cardsById = cards.associateBy { it.id }
        deck.mapNotNull { item ->
            cardsById[item.cardId]?.let { DeckCard(it.asExternalModel(), item.quantity) }
        }
    }

    override suspend fun add(cardId: String) {
        val quantity = ((deckDao.quantity(cardId) ?: 0) + 1).coerceAtMost(4)
        deckDao.upsert(DeckCardEntity(cardId, quantity))
    }

    override suspend fun remove(cardId: String) {
        val quantity = (deckDao.quantity(cardId) ?: return) - 1
        if (quantity <= 0) deckDao.remove(cardId) else deckDao.upsert(DeckCardEntity(cardId, quantity))
    }
}
