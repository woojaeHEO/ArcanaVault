package io.github.woojaeheo.arcanavault.core.model

/** 포켓몬 TCG 덱 구성 범위 검증 */
object DeckRules {
    const val DECK_SIZE = 60
    const val MAX_COPIES = 4

    fun validate(cards: List<DeckCard>): DeckValidation {
        val total = cards.sumOf(DeckCard::quantity)
        val invalidCopies = cards
            .filter { it.card.supertype != "Energy" && it.quantity !in 1..MAX_COPIES }
            .map { it.card.id }
        return DeckValidation(
            isSizeValid = total == DECK_SIZE && invalidCopies.isEmpty(),
            invalidCopyCardIds = invalidCopies,
            totalCards = total,
        )
    }
}

data class DeckValidation(
    val isSizeValid: Boolean,
    val invalidCopyCardIds: List<String>,
    val totalCards: Int,
)
