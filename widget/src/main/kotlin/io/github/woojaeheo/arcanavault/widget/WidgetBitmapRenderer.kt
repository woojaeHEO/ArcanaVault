package io.github.woojaeheo.arcanavault.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import io.github.woojaeheo.arcanavault.core.designsystem.R
import kotlin.math.roundToInt

/** 위젯 Bitmap 렌더러 */
internal object WidgetBitmapRenderer {
    fun text(
        context: Context,
        text: String,
        widthDp: Float,
        heightDp: Float,
        textSizeSp: Float,
        color: Int,
        bold: Boolean,
        maxLines: Int,
    ): Bitmap {
        val density = context.resources.displayMetrics.density.coerceAtMost(2f)
        val fontScale = context.resources.configuration.fontScale.coerceIn(.85f, 1.4f)
        val scaledDensity = density * fontScale
        val width = (widthDp * density).roundToInt().coerceAtLeast(1)
        val height = (heightDp * density).roundToInt().coerceAtLeast(1)
        val font = ResourcesCompat.getFont(context, R.font.pretendard_regular)
            ?: Typeface.create("sans-serif", Typeface.NORMAL)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = textSizeSp * scaledDensity
            typeface = Typeface.create(font, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setIncludePad(false)
            .setLineSpacing(0f, 1.02f)
            .setMaxLines(maxLines)
            .build()
        return createBitmap(width, height).also { bitmap ->
            val canvas = Canvas(bitmap)
            canvas.translate(0f, ((height - layout.height) / 2f).coerceAtLeast(0f))
            layout.draw(canvas)
        }
    }

    fun background(): Bitmap {
        val width = 480
        val height = 240
        return createBitmap(width, height, Bitmap.Config.RGB_565).also { bitmap ->
            val canvas = Canvas(bitmap)
            val base = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    intArrayOf(Color.rgb(10, 12, 26), Color.rgb(29, 20, 58), Color.rgb(8, 35, 48)),
                    null,
                    Shader.TileMode.CLAMP,
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), base)
            val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    width * .82f,
                    height * .18f,
                    width * .48f,
                    intArrayOf(Color.rgb(83, 99, 201), Color.TRANSPARENT),
                    null,
                    Shader.TileMode.CLAMP,
                )
            }
            canvas.drawCircle(width * .82f, height * .18f, width * .48f, glow)
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.argb(90, 255, 255, 255)
            }.also { border ->
                canvas.drawRoundRect(2f, 2f, width - 2f, height - 2f, 38f, 38f, border)
            }
        }
    }

    fun cardPlaceholder(): Bitmap {
        val width = 180
        val height = 252
        return createBitmap(width, height, Bitmap.Config.RGB_565).also { bitmap ->
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    intArrayOf(Color.rgb(48, 53, 102), Color.rgb(25, 94, 104)),
                    null,
                    Shader.TileMode.CLAMP,
                )
            }
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 18f, 18f, paint)
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(190, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }.also { ring ->
                canvas.drawCircle(width / 2f, height / 2f, 34f, ring)
                canvas.drawCircle(width / 2f, height / 2f, 9f, ring)
            }
        }
    }
}
