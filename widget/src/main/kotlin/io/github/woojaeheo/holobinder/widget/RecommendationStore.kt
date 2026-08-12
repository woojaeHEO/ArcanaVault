package io.github.woojaeheo.holobinder.widget

import android.content.Context
import androidx.core.content.edit
import io.github.woojaeheo.holobinder.core.model.Card
import java.io.File

internal data class WidgetRecommendation(
    val cardId: String,
    val name: String,
    val setName: String,
    val rarity: String,
    val updatedAt: Long,
    val imageFile: File?,
)

/** 위젯 추천 카드 저장소 */
internal object RecommendationStore {
    private const val PREFERENCES = "widget_recommendation"
    private const val CARD_ID = "card_id"
    private const val NAME = "name"
    private const val SET_NAME = "set_name"
    private const val RARITY = "rarity"
    private const val UPDATED_AT = "updated_at"
    private const val IMAGE_NAME = "image_name"

    fun read(context: Context): WidgetRecommendation? {
        val values = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val cardId = values.getString(CARD_ID, null) ?: return null
        val image = values.getString(IMAGE_NAME, null)
            ?.let { File(context.filesDir, it) }
            ?.takeIf(File::isFile)
        return WidgetRecommendation(
            cardId = cardId,
            name = values.getString(NAME, null).orEmpty(),
            setName = values.getString(SET_NAME, null).orEmpty(),
            rarity = values.getString(RARITY, null).orEmpty(),
            updatedAt = values.getLong(UPDATED_AT, 0L),
            imageFile = image,
        )
    }

    fun write(context: Context, card: Card, updatedAt: Long, imageFile: File?) {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val previousImage = preferences.getString(IMAGE_NAME, null)
        preferences.edit {
            putString(CARD_ID, card.id)
            putString(NAME, card.name)
            putString(SET_NAME, card.setName)
            putString(RARITY, card.rarity.orEmpty())
            putLong(UPDATED_AT, updatedAt)
            putString(IMAGE_NAME, imageFile?.name)
        }
        if (previousImage != null && previousImage != imageFile?.name) {
            File(context.filesDir, previousImage).delete()
        }
    }

    fun imageFile(context: Context, cardId: String): File =
        File(context.filesDir, "recommendation_${cardId.hashCode()}.png")
}
