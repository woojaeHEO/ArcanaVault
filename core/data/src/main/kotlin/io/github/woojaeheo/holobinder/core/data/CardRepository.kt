package io.github.woojaeheo.holobinder.core.data

import io.github.woojaeheo.holobinder.core.common.SyncResult
import io.github.woojaeheo.holobinder.core.common.runSuspendCatching
import io.github.woojaeheo.holobinder.core.database.CardDao
import io.github.woojaeheo.holobinder.core.database.CardEntity
import io.github.woojaeheo.holobinder.core.domain.RecommendationRepository
import io.github.woojaeheo.holobinder.core.model.Card
import io.github.woojaeheo.holobinder.core.model.CardFilter
import io.github.woojaeheo.holobinder.core.network.CardApi
import io.github.woojaeheo.holobinder.core.network.CardDetailDto
import io.github.woojaeheo.holobinder.core.network.CardSummaryDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

interface CardRepository {
    fun observeCards(filter: CardFilter): Flow<List<Card>>
    fun observeCard(id: String): Flow<Card?>
    fun observeFavorites(): Flow<List<Card>>
    suspend fun refresh(filter: CardFilter, force: Boolean = false): SyncResult
    suspend fun refreshCard(id: String): SyncResult
    suspend fun toggleFavorite(id: String)
    suspend fun latestFavorite(): Card?
}

@Singleton
class OfflineFirstCardRepository @Inject constructor(
    private val api: CardApi,
    private val cardDao: CardDao,
) : CardRepository, RecommendationRepository {
    private val refreshMutex = Mutex()
    private val refreshTimes = LinkedHashMap<CardFilter, Long>()

    override fun observeCards(filter: CardFilter): Flow<List<Card>> =
        cardDao.observeCards(filter.query.normalizedCardName(), filter.type, filter.supertype, filter.sort.name)
            .map { cards -> cards.map(CardEntity::asExternalModel) }

    override fun observeCard(id: String): Flow<Card?> =
        cardDao.observeCard(id).map { it?.asExternalModel() }

    override fun observeFavorites(): Flow<List<Card>> =
        cardDao.observeFavorites().map { cards -> cards.map(CardEntity::asExternalModel) }

    /** 목록 응답을 Room에 저장하고 상세 필드는 보존 */
    override suspend fun refresh(filter: CardFilter, force: Boolean): SyncResult = refreshMutex.withLock {
        val now = System.currentTimeMillis()
        val lastRefreshAt = refreshTimes[filter] ?: 0L
        if (!force && now - lastRefreshAt in 0 until REFRESH_WINDOW && cardDao.count() > 0) {
            return SyncResult.Success
        }
        return runSuspendCatching {
            val cards = api.searchCards(
                name = filter.query.normalizedCardName().takeIf(String::isNotEmpty),
                type = filter.type,
                category = filter.supertype,
            )
            cardDao.upsertCardsPreservingFavorites(
                cards.mapNotNull { summary ->
                    summary.asEntity(
                        existing = cardDao.card(summary.id),
                        updatedAt = now,
                        requestedType = filter.type,
                        requestedSupertype = filter.supertype,
                    ).takeIf { it.imageUrl.isNotBlank() }
                },
            )
            refreshTimes[filter] = now
            while (refreshTimes.size > REFRESH_KEY_LIMIT) {
                refreshTimes.remove(refreshTimes.keys.first())
            }
        }.fold(
            onSuccess = { SyncResult.Success },
            onFailure = { SyncResult.Error(it.message ?: "카드 정보를 불러오지 못했습니다.") },
        )
    }

    /** 선택된 카드만 고해상도 상세 정보로 갱신 */
    override suspend fun refreshCard(id: String): SyncResult = runSuspendCatching {
        val existing = cardDao.card(id)
        cardDao.upsertCardsPreservingFavorites(
            listOf(api.card(id).asEntity(existing, System.currentTimeMillis())),
        )
    }.fold(
        onSuccess = { SyncResult.Success },
        onFailure = { SyncResult.Error(it.message ?: "상세 정보를 불러오지 못했습니다.") },
    )

    override suspend fun toggleFavorite(id: String) = cardDao.toggleFavorite(id)

    override suspend fun latestFavorite(): Card? = cardDao.latestFavorite()?.asExternalModel()

    override suspend fun randomCard(excludedId: String?): Card? {
        val cached = excludedId?.let { cardDao.randomCardExcluding(it) } ?: cardDao.randomCard()
        cached?.let { return it.asExternalModel() }
        refresh(CardFilter(), force = true)
        val refreshed = excludedId?.let { cardDao.randomCardExcluding(it) } ?: cardDao.randomCard()
        if (refreshed != null) return refreshed.asExternalModel()
        return if (excludedId == null) null else cardDao.card(excludedId)?.asExternalModel()
    }

    private companion object {
        const val REFRESH_WINDOW = 30 * 60 * 1_000L
        const val REFRESH_KEY_LIMIT = 16
    }
}

private fun CardSummaryDto.asEntity(
    existing: CardEntity?,
    updatedAt: Long,
    requestedType: String?,
    requestedSupertype: String?,
) = CardEntity(
    id = id,
    name = name,
    supertype = existing?.supertype ?: requestedSupertype ?: "Pokemon",
    subtypes = existing?.subtypes.orEmpty(),
    hp = existing?.hp,
    types = existing?.types?.takeIf(String::isNotBlank) ?: requestedType.orEmpty(),
    description = existing?.description.orEmpty(),
    weakness = existing?.weakness,
    retreatCost = existing?.retreatCost ?: 0,
    setName = existing?.setName ?: id.substringBefore('-').uppercase(),
    setSeries = existing?.setSeries.orEmpty(),
    releaseDate = existing?.releaseDate.orEmpty(),
    number = localId,
    rarity = existing?.rarity,
    artist = existing?.artist,
    imageUrl = image?.let { "$it/low.webp" } ?: existing?.imageUrl.orEmpty(),
    largeImageUrl = image?.let { "$it/high.webp" } ?: existing?.largeImageUrl.orEmpty(),
    price = existing?.price,
    isFavorite = existing?.isFavorite ?: false,
    updatedAt = updatedAt,
)

private fun CardDetailDto.asEntity(existing: CardEntity?, updatedAt: Long): CardEntity {
    val abilityText = abilities.joinToString("\n\n") { "${it.name} · ${it.effect}" }
    val attackText = attacks.joinToString("\n\n") { attack ->
        buildString {
            append(attack.name)
        attack.damage?.takeIf(String::isNotBlank)?.let { append("  $it") }
            attack.effect?.takeIf(String::isNotBlank)?.let { append("\n$it") }
        }
    }
    return CardEntity(
        id = id,
        name = name,
        supertype = category,
        subtypes = listOfNotNull(stage).joinToString(FIELD_SEPARATOR),
        hp = hp,
        types = types.joinToString(FIELD_SEPARATOR),
        description = listOfNotNull(
            description?.takeIf(String::isNotBlank),
            abilityText.takeIf(String::isNotBlank),
            attackText.takeIf(String::isNotBlank),
        ).joinToString("\n\n"),
        weakness = weaknesses.firstOrNull()?.let { "${it.type} ${it.value}" },
        retreatCost = retreat,
        setName = set.name,
        setSeries = set.id.uppercase(),
        releaseDate = "",
        number = localId,
        rarity = rarity,
        artist = illustrator,
        imageUrl = image?.let { "$it/low.webp" } ?: existing?.imageUrl.orEmpty(),
        largeImageUrl = image?.let { "$it/high.webp" } ?: existing?.largeImageUrl.orEmpty(),
        price = null,
        isFavorite = existing?.isFavorite ?: false,
        updatedAt = updatedAt,
    )
}

fun CardEntity.asExternalModel() = Card(
    id = id,
    name = name,
    supertype = supertype,
    subtypes = subtypes.toFieldList(),
    hp = hp,
    types = types.toFieldList(),
    description = description,
    weakness = weakness,
    retreatCost = retreatCost,
    setName = setName,
    setSeries = setSeries,
    releaseDate = releaseDate,
    number = number,
    rarity = rarity,
    artist = artist,
    imageUrl = imageUrl,
    largeImageUrl = largeImageUrl,
    price = price,
    isFavorite = isFavorite,
)

private const val FIELD_SEPARATOR = "\u001F"
private fun String.toFieldList() = takeIf(String::isNotBlank)?.split(FIELD_SEPARATOR).orEmpty()

private fun String.normalizedCardName(): String {
    val query = trim()
    return KOREAN_CARD_NAMES[query] ?: query
}

private val KOREAN_CARD_NAMES = mapOf(
    "피카츄" to "Pikachu",
    "라이츄" to "Raichu",
    "리자몽" to "Charizard",
    "파이리" to "Charmander",
    "꼬부기" to "Squirtle",
    "거북왕" to "Blastoise",
    "이상해씨" to "Bulbasaur",
    "이상해꽃" to "Venusaur",
    "뮤" to "Mew",
    "뮤츠" to "Mewtwo",
    "이브이" to "Eevee",
)
