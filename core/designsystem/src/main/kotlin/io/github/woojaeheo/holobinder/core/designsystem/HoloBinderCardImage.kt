package io.github.woojaeheo.holobinder.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.size.Precision

/** 카드 이미지 용도 */
enum class HoloBinderCardImageSize(val width: Int, val height: Int) {
    Deck(180, 248),
    Grid(245, 337),
    Detail(600, 825),
}

/** 캐시와 요청 크기를 공유하는 카드 이미지 */
@Composable
fun HoloBinderCardImage(
    imageUrl: String,
    contentDescription: String?,
    previewImageUrl: String? = null,
    modifier: Modifier = Modifier,
    size: HoloBinderCardImageSize = HoloBinderCardImageSize.Grid,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: androidx.compose.ui.Alignment = androidx.compose.ui.Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    val context = LocalContext.current
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)
    val errorColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = .40f)
    val placeholder = remember(placeholderColor) { ColorPainter(placeholderColor) }
    val error = remember(errorColor) { ColorPainter(errorColor) }
    val request: ImageRequest = remember(context, imageUrl, previewImageUrl, size) {
        cardImageRequest(context, imageUrl, previewImageUrl, size)
    }
    val previewUrl = previewImageUrl?.takeIf { it.isNotBlank() && it != imageUrl }
    if (previewUrl == null) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            modifier = modifier,
            placeholder = placeholder,
            error = error,
            fallback = error,
            contentScale = contentScale,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = FilterQuality.Low,
        )
        return
    }

    var fullImageReady by remember(imageUrl) { mutableStateOf(false) }
    val previewRequest = remember(context, previewUrl) {
        cardImageRequest(context, previewUrl, null, HoloBinderCardImageSize.Grid)
    }
    Box(modifier) {
        if (!fullImageReady) {
            AsyncImage(
                model = previewRequest,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                placeholder = placeholder,
                error = error,
                fallback = error,
                contentScale = contentScale,
                alignment = alignment,
                alpha = alpha,
                colorFilter = colorFilter,
                filterQuality = FilterQuality.Low,
            )
        }
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            onSuccess = { fullImageReady = true },
            contentScale = contentScale,
            alignment = alignment,
            alpha = if (fullImageReady) alpha else 0f,
            colorFilter = colorFilter,
            filterQuality = FilterQuality.Low,
        )
    }
}

/** 화면에 곧 진입할 카드 이미지를 메모리와 디스크 캐시에 미리 적재 */
@Composable
fun HoloBinderCardImagePrefetch(
    imageUrls: List<String>,
    size: HoloBinderCardImageSize = HoloBinderCardImageSize.Grid,
) {
    val context = LocalContext.current
    val imageLoader = remember(context) { SingletonImageLoader.get(context) }
    DisposableEffect(imageLoader, imageUrls, size) {
        val requests = imageUrls.asSequence()
            .filter(String::isNotBlank)
            .distinct()
            .take(PREFETCH_LIMIT)
            .map { imageLoader.enqueue(cardImageRequest(context, it, null, size)) }
            .toList()
        onDispose { requests.forEach { it.dispose() } }
    }
}

private fun cardImageRequest(
    context: android.content.Context,
    imageUrl: String,
    previewImageUrl: String?,
    size: HoloBinderCardImageSize,
): ImageRequest = ImageRequest.Builder(context)
    .data(imageUrl.takeIf(String::isNotBlank))
    .size(size.width, size.height)
    .precision(Precision.INEXACT)
    .allowHardware(true)
    .memoryCacheKey(imageUrl.takeIf(String::isNotBlank))
    .apply {
        previewImageUrl?.takeIf(String::isNotBlank)?.let(::placeholderMemoryCacheKey)
    }
    .diskCacheKey(imageUrl.takeIf(String::isNotBlank))
    .crossfade(0)
    .build()

private const val PREFETCH_LIMIT = 12
