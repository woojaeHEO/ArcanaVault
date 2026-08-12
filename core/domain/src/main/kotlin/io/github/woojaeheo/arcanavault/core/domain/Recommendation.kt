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
