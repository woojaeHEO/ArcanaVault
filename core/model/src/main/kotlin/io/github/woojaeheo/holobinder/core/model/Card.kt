package io.github.woojaeheo.holobinder.core.model

/** 포켓몬 TCG 카드 도메인 모델 */
data class Card(
    val id: String,
    val name: String,
    val supertype: String,
    val subtypes: List<String>,
    val hp: Int?,
    val types: List<String>,
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
    val isFavorite: Boolean = false,
)

/** 덱에 포함된 카드 */
data class DeckCard(
    val card: Card,
    val quantity: Int,
)

/** 카드 검색 필터 */
data class CardFilter(
    val query: String = "",
    val type: String? = null,
    val supertype: String? = null,
    val sort: CardSort = CardSort.RecentlyAdded,
)

enum class CardSort { Name, RecentlyAdded }

/** 화면 테마 설정 */
enum class ThemeMode { System, Light, Dark }

/** 사용자 환경 설정 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val reducedMotion: Boolean = false,
    val gridDensity: Int = 2,
)
