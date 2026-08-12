package io.github.woojaeheo.arcanavault.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.core.graphics.scale
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.woojaeheo.arcanavault.core.domain.GetRecommendedCardUseCase
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/** 추천 카드 갱신 작업 */
@HiltWorker
class RecommendationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted parameters: WorkerParameters,
    private val getRecommendedCard: GetRecommendedCardUseCase,
) : CoroutineWorker(appContext, parameters) {
    override suspend fun doWork(): Result {
        if (!hasWidget(applicationContext)) return Result.success()

        val previous = RecommendationStore.read(applicationContext)
        val card = getRecommendedCard(previous?.cardId)

        if (card != null) {
            val image = cacheCardImage(applicationContext, card.id, card.imageUrl)
            RecommendationStore.write(applicationContext, card, System.currentTimeMillis(), image)
            ArcanaWidget().updateAll(applicationContext)
        }
        enqueueNext(applicationContext)
        return Result.success()
    }

    private fun cacheCardImage(context: Context, cardId: String, imageUrl: String) =
        if (imageUrl.isBlank()) null else runCatching {
            val connection = URL(imageUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = NETWORK_TIMEOUT_MILLIS
            connection.readTimeout = NETWORK_TIMEOUT_MILLIS
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "ArcanaVaultWidget")
            try {
                connection.inputStream.use { BitmapFactory.decodeStream(it) }
            } finally {
                connection.disconnect()
            }
        }.getOrNull()?.let { source ->
            val width = CARD_IMAGE_WIDTH
            val height = (source.height * width.toFloat() / source.width).toInt().coerceAtLeast(1)
            val scaled = source.scale(width, height)
            val target = RecommendationStore.imageFile(context, cardId)
            val temporary = java.io.File(context.filesDir, "${target.name}.tmp")
            temporary.outputStream().use {
                scaled.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, it)
            }
            if (!temporary.renameTo(target)) {
                temporary.copyTo(target, overwrite = true)
                temporary.delete()
            }
            if (scaled !== source) scaled.recycle()
            source.recycle()
            target
        }

    companion object {
        private const val WORK_NAME = "recommendation-widget-refresh"
        private const val NETWORK_TIMEOUT_MILLIS = 8_000
        private const val CARD_IMAGE_WIDTH = 180
        private const val IMAGE_QUALITY = 92
        private const val REFRESH_MINUTES = 5L

        /** 첫 갱신 예약 */
        fun enqueueInitial(context: Context) {
            if (!hasWidget(context)) return
            val request = OneTimeWorkRequestBuilder<RecommendationWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }

        /** 다음 갱신 예약 */
        private fun enqueueNext(context: Context) {
            if (!hasWidget(context)) return
            val request = OneTimeWorkRequestBuilder<RecommendationWorker>()
                .setInitialDelay(REFRESH_MINUTES, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request,
            )
        }

        /** 위젯 작업 취소 */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun hasWidget(context: Context): Boolean {
            val component = ComponentName(context, ArcanaWidgetReceiver::class.java)
            return AppWidgetManager.getInstance(context).getAppWidgetIds(component).isNotEmpty()
        }
    }
}
