package io.github.woojaeheo.holobinder.widget

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
import io.github.woojaeheo.holobinder.core.domain.GetRecommendedCardUseCase
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
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
        return try {
            currentCoroutineContext().ensureActive()
            val previous = RecommendationStore.read(applicationContext)
            val card = getRecommendedCard(previous?.cardId)
            if (card != null) {
                currentCoroutineContext().ensureActive()
                val image = cacheCardImage(applicationContext, card.id, card.imageUrl)
                currentCoroutineContext().ensureActive()
                RecommendationStore.write(applicationContext, card, System.currentTimeMillis(), image)
                HoloBinderWidget().updateAll(applicationContext)
            }
            Result.success()
        } finally {
            if (!isStopped && currentCoroutineContext().isActive) enqueueNext(applicationContext)
        }
    }

    private fun cacheCardImage(context: Context, cardId: String, imageUrl: String) =
        if (imageUrl.isBlank()) null else runCatching {
            val connection = URL(imageUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = NETWORK_TIMEOUT_MILLIS
            connection.readTimeout = NETWORK_TIMEOUT_MILLIS
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "HoloBinderWidget")
            try {
                if (connection.responseCode !in 200..299) return@runCatching null
                val length = connection.contentLengthLong
                if (length > MAX_IMAGE_BYTES) return@runCatching null
                val bytes = connection.inputStream.use(::readBoundedImage)
                decodeSampledImage(bytes)
            } finally {
                connection.disconnect()
            }
        }.getOrNull()?.let { source ->
            val width = CARD_IMAGE_WIDTH
            val height = (source.height * width.toFloat() / source.width).toInt().coerceAtLeast(1)
            val scaled = source.scale(width, height)
            try {
                val target = RecommendationStore.imageFile(context, cardId)
                val temporary = java.io.File(context.filesDir, "${target.name}.tmp")
                temporary.outputStream().use {
                    check(scaled.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, it))
                }
                if (!temporary.renameTo(target)) {
                    temporary.copyTo(target, overwrite = true)
                    temporary.delete()
                }
                target
            } finally {
                if (scaled !== source) scaled.recycle()
                source.recycle()
            }
        }

    private fun readBoundedImage(input: java.io.InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            check(total <= MAX_IMAGE_BYTES) { "Widget image exceeds the size limit" }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun decodeSampledImage(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_DECODE_WIDTH * 2) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    companion object {
        private const val WORK_NAME = "recommendation-widget-refresh"
        private const val NETWORK_TIMEOUT_MILLIS = 8_000
        private const val MAX_IMAGE_BYTES = 8L * 1_024L * 1_024L
        private const val CARD_IMAGE_WIDTH = 180
        private const val MAX_DECODE_WIDTH = 720
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
            val component = ComponentName(context, HoloBinderWidgetReceiver::class.java)
            return AppWidgetManager.getInstance(context).getAppWidgetIds(component).isNotEmpty()
        }
    }
}
