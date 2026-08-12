package io.github.woojaeheo.holobinder.core.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class CardDetailDtoTest {
    @Test
    fun `공격 피해량 숫자를 문자열 값으로 읽는다`() {
        val card = Json.decodeFromString<CardDetailDto>(CARD_JSON)

        assertEquals("30", card.attacks.single().damage)
    }

    private companion object {
        const val CARD_JSON = """
            {
              "id": "base1-1",
              "localId": "1",
              "name": "Alakazam",
              "category": "Pokemon",
              "hp": 80,
              "types": ["Psychic"],
              "retreat": 3,
              "attacks": [{"name": "Confuse Ray", "damage": 30}],
              "set": {"id": "base1", "name": "Base Set"}
            }
        """
    }
}
