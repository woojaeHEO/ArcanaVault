package io.github.woojaeheo.arcanavault.core.domain

import io.github.woojaeheo.arcanavault.core.model.Card
import javax.inject.Inject

/** 추천 카드 저장소 */
interface RecommendationRepository {
    suspend fun randomCard(excludedId: String?): Card?
}

/** 오늘의 추천 카드 선택 */
class GetRecommendedCardUseCase @Inject constructor(
    private val repository: RecommendationRepository,
) {
    suspend operator fun invoke(excludedId: String? = null): Card? = repository.randomCard(excludedId)
}

/** 덱 추가 결과 */
enum class DeckAddResult {
    Added,
    CopyLimitReached,
    DeckFull,
    CardNotFound,
}

/** 포켓몬 덱 규칙 */
object DeckRules {
    const val MAX_DECK_SIZE = 60
    const val MAX_CARD_COPIES = 4

    fun copyLimit(card: Card): Int =
        if (card.supertype == "Energy" && "Basic" in card.subtypes) Int.MAX_VALUE else MAX_CARD_COPIES
}
