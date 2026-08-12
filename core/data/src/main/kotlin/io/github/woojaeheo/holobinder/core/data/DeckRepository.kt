package io.github.woojaeheo.holobinder.core.data

import io.github.woojaeheo.holobinder.core.database.CardDao
import io.github.woojaeheo.holobinder.core.database.DeckCardEntity
import io.github.woojaeheo.holobinder.core.database.DeckDao
import io.github.woojaeheo.holobinder.core.model.DeckCard
import io.github.woojaeheo.holobinder.core.domain.DeckAddResult
import io.github.woojaeheo.holobinder.core.domain.DeckRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

interface DeckRepository {
    val deck: Flow<List<DeckCard>>
    suspend fun add(cardId: String): DeckAddResult
    suspend fun remove(cardId: String)
}

@Singleton
class OfflineDeckRepository @Inject constructor(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
) : DeckRepository {
    private val mutationMutex = Mutex()

    override val deck: Flow<List<DeckCard>> = combine(
        deckDao.observeDeck(),
        cardDao.observeCards("", null, null, "Name"),
    ) { deck, cards ->
        val cardsById = cards.associateBy { it.id }
        deck.mapNotNull { item ->
            cardsById[item.cardId]?.let { DeckCard(it.asExternalModel(), item.quantity) }
        }
    }

    override suspend fun add(cardId: String): DeckAddResult = mutationMutex.withLock {
        if (deckDao.totalQuantity() >= DeckRules.MAX_DECK_SIZE) return@withLock DeckAddResult.DeckFull
        val card = cardDao.card(cardId) ?: return@withLock DeckAddResult.CardNotFound
        val quantity = deckDao.quantity(cardId) ?: 0
        if (quantity >= DeckRules.copyLimit(card.asExternalModel())) return@withLock DeckAddResult.CopyLimitReached
        deckDao.upsert(DeckCardEntity(cardId, quantity + 1))
        DeckAddResult.Added
    }

    override suspend fun remove(cardId: String) = mutationMutex.withLock {
        val quantity = (deckDao.quantity(cardId) ?: return@withLock) - 1
        if (quantity <= 0) deckDao.remove(cardId) else deckDao.upsert(DeckCardEntity(cardId, quantity))
    }
}
