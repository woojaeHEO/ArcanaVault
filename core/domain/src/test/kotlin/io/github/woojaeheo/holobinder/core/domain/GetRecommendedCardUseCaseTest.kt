package io.github.woojaeheo.holobinder.core.domain

import io.github.woojaeheo.holobinder.core.model.Card
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

    @Test
    fun `기본 에너지는 복사 제한이 없다`() {
        val card = testCard(supertype = "Energy", subtypes = listOf("Basic"))
        assertEquals(Int.MAX_VALUE, DeckRules.copyLimit(card))
    }

    @Test
    fun `일반 카드는 네 장으로 제한한다`() {
        assertEquals(4, DeckRules.copyLimit(testCard()))
    }

    private fun testCard(
        supertype: String = "Pokemon",
        subtypes: List<String> = emptyList(),
    ) = Card(
        id = "base1-4",
        name = "Charizard",
        supertype = supertype,
        subtypes = subtypes,
        hp = 120,
        types = listOf("Fire"),
        description = "",
        weakness = null,
        retreatCost = 3,
        setName = "Base Set",
        setSeries = "BASE1",
        releaseDate = "",
        number = "4",
        rarity = "Rare Holo",
        artist = null,
        imageUrl = "",
        largeImageUrl = "",
        price = null,
    )
}
