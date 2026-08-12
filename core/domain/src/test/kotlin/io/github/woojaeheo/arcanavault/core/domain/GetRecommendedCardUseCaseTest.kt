package io.github.woojaeheo.arcanavault.core.domain

import io.github.woojaeheo.arcanavault.core.model.Card
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRecommendedCardUseCaseTest {
    @Test
    fun `직전 카드 식별자를 저장소에 전달한다`() = kotlinx.coroutines.runBlocking {
        var receivedId: String? = null
        val repository = object : RecommendationRepository {
            override suspend fun randomCard(excludedId: String?): Card? {
                receivedId = excludedId
                return null
            }
        }
        GetRecommendedCardUseCase(repository)("base1-4")
        assertEquals("base1-4", receivedId)
    }
}
