package io.github.woojaeheo.arcanavault.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

/** 카드 이미지 용도 */
enum class ArcanaCardImageSize(val width: Int, val height: Int) {
    Deck(180, 260),
    Grid(600, 840),
    Detail(900, 1_260),
}

/** 캐시와 요청 크기를 공유하는 카드 이미지 */
@Composable
fun ArcanaCardImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: ArcanaCardImageSize = ArcanaCardImageSize.Grid,
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
    val request: ImageRequest = remember(context, imageUrl, size) {
        ImageRequest.Builder(context)
            .data(imageUrl.takeIf(String::isNotBlank))
            .size(size.width, size.height)
            .diskCacheKey(imageUrl.takeIf(String::isNotBlank))
            .crossfade(140)
            .build()
    }
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
        filterQuality = FilterQuality.Medium,
    )
}
