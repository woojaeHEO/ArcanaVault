package io.github.woojaeheo.holobinder.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckRulesTest {
    private val card = Card(
        id = "base1-4",
        name = "Charizard",
        supertype = "Pokémon",
        subtypes = listOf("Stage 2"),
        hp = 120,
        types = listOf("Fire"),
        description = "",
        weakness = "Water ×2",
        retreatCost = 3,
        setName = "Base",
        setSeries = "Base",
        releaseDate = "1999/01/09",
        number = "4",
        rarity = "Rare Holo",
        artist = "Mitsuhiro Arita",
        imageUrl = "",
        largeImageUrl = "",
        price = null,
    )

    @Test
    fun `sixty cards is a valid deck size`() {
        val cards = (1..60).map { DeckCard(card.copy(id = "base1-$it"), 1) }
        assertTrue(DeckRules.validate(cards).isSizeValid)
    }

    @Test
    fun `five copies reports card id`() {
        val result = DeckRules.validate(listOf(DeckCard(card, 5)))
        assertFalse(result.isSizeValid)
        assertEquals(listOf("base1-4"), result.invalidCopyCardIds)
    }
}
