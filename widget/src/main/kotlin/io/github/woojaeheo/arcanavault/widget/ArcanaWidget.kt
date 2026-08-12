package io.github.woojaeheo.arcanavault.widget

import android.content.Context
import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import io.github.woojaeheo.arcanavault.core.database.ArcanaDatabase

class ArcanaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = Room.databaseBuilder(context, ArcanaDatabase::class.java, "arcana-vault.db").build()
        val favorite = runCatching { database.cardDao().latestFavorite() }.getOrNull()
        database.close()
        provideContent { ArcanaWidgetContent(favorite?.name, favorite?.setName) }
    }
}

@Composable
private fun ArcanaWidgetContent(cardName: String?, cardType: String?) {
    val size = LocalSize.current
    Column(
        modifier = GlanceModifier.fillMaxSize().background(ColorProvider(Color(0xEE101421))).padding(18.dp)
            .clickable(
                actionStartActivity(
                    ComponentName(
                        "io.github.woojaeheo.arcanavault",
                        "io.github.woojaeheo.arcanavault.MainActivity",
                    ),
                ),
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text("HOLO VAULT", style = TextStyle(ColorProvider(Color(0xFFB8C4FF)), fontWeight = FontWeight.Bold))
        Spacer(GlanceModifier.size(8.dp))
        Text(
            cardName ?: "즐겨찾는 카드를 담아보세요",
            style = TextStyle(ColorProvider(Color.White), fontWeight = FontWeight.Bold),
            maxLines = if (size.width > 240.dp) 2 else 1,
        )
        cardType?.let {
            Spacer(GlanceModifier.size(4.dp))
            Text(it, style = TextStyle(ColorProvider(Color(0xFFBFC5D9))))
        }
    }
}

class ArcanaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ArcanaWidget()
}
