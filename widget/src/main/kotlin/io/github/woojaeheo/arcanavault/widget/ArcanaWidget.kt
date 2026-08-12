package io.github.woojaeheo.arcanavault.widget

import android.content.ComponentName
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width

/** 오늘의 추천 카드 위젯 */
class ArcanaWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp),
            DpSize(260.dp, 150.dp),
            DpSize(360.dp, 220.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val recommendation = RecommendationStore.read(context)
        provideContent {
            ArcanaWidgetContent(context, recommendation)
        }
    }
}

@Composable
private fun ArcanaWidgetContent(
    context: Context,
    recommendation: WidgetRecommendation?,
) {
    val size = LocalSize.current
    val mode = size.layoutMode()
    val openApp = actionStartActivity(
        ComponentName(
            "io.github.woojaeheo.arcanavault",
            "io.github.woojaeheo.arcanavault.MainActivity",
        ),
    )
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(WidgetBitmapRenderer.background()))
            .clickable(openApp)
            .padding(mode.padding),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        if (mode != WidgetLayout.Compact) {
            RecommendationImage(recommendation, mode)
            Spacer(GlanceModifier.width(mode.gap))
        }
        RecommendationCopy(context, recommendation, size, mode)
    }
}

@Composable
private fun RecommendationImage(
    recommendation: WidgetRecommendation?,
    mode: WidgetLayout,
) {
    val bitmap = recommendation?.imageFile?.let { BitmapFactory.decodeFile(it.absolutePath) }
    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = recommendation.name,
            modifier = GlanceModifier.size(mode.imageWidth, mode.imageHeight),
            contentScale = ContentScale.Fit,
        )
    } else {
        Image(
            provider = ImageProvider(WidgetBitmapRenderer.cardPlaceholder()),
            contentDescription = "추천 카드 준비 중",
            modifier = GlanceModifier.size(mode.imageWidth, mode.imageHeight),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun RecommendationCopy(
    context: Context,
    recommendation: WidgetRecommendation?,
    size: DpSize,
    mode: WidgetLayout,
) {
    val name = recommendation?.name?.takeIf(String::isNotBlank) ?: "추천 카드를 준비하고 있어요"
    val availableWidth = when (mode) {
        WidgetLayout.Compact -> size.width.value - mode.padding.value * 2
        else -> size.width.value - mode.padding.value * 2 - mode.imageWidth.value - mode.gap.value
    }.coerceAtLeast(72f)

    Column(GlanceModifier.width(availableWidth.dp)) {
        BitmapText(
            context = context,
            text = "TODAY'S HOLO PICK",
            width = availableWidth,
            height = if (mode == WidgetLayout.Compact) 18f else 22f,
            textSize = if (mode == WidgetLayout.Expanded) 13f else 11f,
            color = Color.rgb(183, 198, 255),
            bold = true,
            maxLines = 1,
        )
        Spacer(GlanceModifier.height(if (mode == WidgetLayout.Compact) 4.dp else 7.dp))
        BitmapText(
            context = context,
            text = name,
            width = availableWidth,
            height = if (mode == WidgetLayout.Expanded) 62f else 44f,
            textSize = when (mode) {
                WidgetLayout.Compact -> 18f
                WidgetLayout.Medium -> 20f
                WidgetLayout.Expanded -> 25f
            },
            color = Color.WHITE,
            bold = true,
            maxLines = 2,
        )
        recommendation?.setName?.takeIf(String::isNotBlank)?.let { setName ->
            Spacer(GlanceModifier.height(4.dp))
            val detail = listOf(setName, recommendation.rarity)
                .filter(String::isNotBlank)
                .joinToString("  ")
            BitmapText(
                context = context,
                text = detail,
                width = availableWidth,
                height = 24f,
                textSize = if (mode == WidgetLayout.Expanded) 13f else 11f,
                color = Color.rgb(198, 204, 224),
                bold = false,
                maxLines = 1,
            )
        }
        if (mode == WidgetLayout.Expanded) {
            Spacer(GlanceModifier.defaultWeight())
            BitmapText(
                context = context,
                text = "5분마다 새로운 카드가 찾아옵니다",
                width = availableWidth,
                height = 22f,
                textSize = 11f,
                color = Color.rgb(128, 226, 209),
                bold = false,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BitmapText(
    context: Context,
    text: String,
    width: Float,
    height: Float,
    textSize: Float,
    color: Int,
    bold: Boolean,
    maxLines: Int,
) {
    Image(
        provider = ImageProvider(
            WidgetBitmapRenderer.text(
                context = context,
                text = text,
                widthDp = width,
                heightDp = height,
                textSizeSp = textSize,
                color = color,
                bold = bold,
                maxLines = maxLines,
            ),
        ),
        contentDescription = text,
        modifier = GlanceModifier.width(width.dp).height(height.dp),
        contentScale = ContentScale.Fit,
    )
}

private enum class WidgetLayout(
    val padding: androidx.compose.ui.unit.Dp,
    val gap: androidx.compose.ui.unit.Dp,
    val imageWidth: androidx.compose.ui.unit.Dp,
    val imageHeight: androidx.compose.ui.unit.Dp,
) {
    Compact(12.dp, 0.dp, 0.dp, 0.dp),
    Medium(14.dp, 12.dp, 72.dp, 100.dp),
    Expanded(18.dp, 18.dp, 108.dp, 150.dp),
}

private fun DpSize.layoutMode(): WidgetLayout = when {
    width < 220.dp || height < 130.dp -> WidgetLayout.Compact
    width >= 320.dp && height >= 180.dp -> WidgetLayout.Expanded
    else -> WidgetLayout.Medium
}

/** 위젯 수명 주기 연결 */
class ArcanaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ArcanaWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        RecommendationWorker.enqueueInitial(context)
    }

    override fun onDisabled(context: Context) {
        RecommendationWorker.cancel(context)
        super.onDisabled(context)
    }
}
