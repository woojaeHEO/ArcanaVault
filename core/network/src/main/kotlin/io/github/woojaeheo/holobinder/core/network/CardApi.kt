package io.github.woojaeheo.holobinder.core.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** TCGdex Pokémon TCG API v2 */
interface CardApi {
    @GET("v2/en/cards")
    suspend fun searchCards(
        @Query("name") name: String? = null,
        @Query("types") type: String? = null,
        @Query("category") category: String? = null,
        @Query("pagination:page") page: Int = 1,
        @Query("pagination:itemsPerPage") pageSize: Int = 72,
    ): List<CardSummaryDto>

    @GET("v2/en/cards/{id}")
    suspend fun card(@Path("id") id: String): CardDetailDto
}

@Serializable
data class CardSummaryDto(
    val id: String,
    val localId: String,
    val name: String,
    val image: String? = null,
)

@Serializable
data class CardDetailDto(
    val id: String,
    val localId: String,
    val name: String,
    val category: String,
    val illustrator: String? = null,
    val image: String? = null,
    val rarity: String? = null,
    val hp: Int? = null,
    val types: List<String> = emptyList(),
    val description: String? = null,
    val stage: String? = null,
    val abilities: List<AbilityDto> = emptyList(),
    val attacks: List<AttackDto> = emptyList(),
    val weaknesses: List<WeaknessDto> = emptyList(),
    val retreat: Int = 0,
    val set: SetDto,
)

@Serializable
data class AbilityDto(val name: String, val effect: String, val type: String)

@Serializable
data class AttackDto(
    val name: String,
    val cost: List<String> = emptyList(),
    @Serializable(with = FlexibleStringSerializer::class)
    val damage: String? = null,
    val effect: String? = null,
)

@Serializable
data class WeaknessDto(val type: String, val value: String)

@Serializable
data class SetDto(val id: String, val name: String)

/** 문자열과 숫자로 혼용되는 API 필드를 문자열로 정규화 */
object FlexibleStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "FlexibleString",
        PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: Decoder): String? {
        val element = (decoder as JsonDecoder).decodeJsonElement()
        return (element as? JsonPrimitive)?.contentOrNull
    }

    override fun serialize(encoder: Encoder, value: String?) {
        (encoder as JsonEncoder).encodeJsonElement(value?.let(::JsonPrimitive) ?: JsonNull)
    }
}
