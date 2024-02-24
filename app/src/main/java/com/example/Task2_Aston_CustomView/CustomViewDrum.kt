package com.example.Task2_Aston_CustomView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception


class CustomViewDrum @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val colors = listOf(
        ContextCompat.getColor(context, R.color.red),
        ContextCompat.getColor(context, R.color.orange),
        ContextCompat.getColor(context, R.color.yellow),
        ContextCompat.getColor(context, R.color.green),
        ContextCompat.getColor(context, R.color.blue),
        ContextCompat.getColor(context, R.color.cyan),
        ContextCompat.getColor(context, R.color.purple)
    )
    private val texts =listOf("Красный", "Оранжевый", "Желтый", "Зеленый", "Синий", "Голубой", "Фиолетовый")
    private val imageUrls = listOf("","https://loremflickr.com/640/360","","https://loremflickr.com/640/360","https://loremflickr.com/640/360", "", "")
    private val bitmapCache = mutableMapOf<Int, Bitmap>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightRectF = RectF()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        textSize = resources.getDimension(R.dimen.text_size)
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.LTGRAY
    }
    private var isStopped = false
    private var isSpinning = false
    private var stopIndex = 0
    private var currentColorIndex = 0
    private var drawnBitmap: Bitmap? = null
    private var lastStopColorIndex: Int = -1

    init {
        setOnClickListener { if (!isSpinning) startSpinning() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, 2 * heightMeasureSpec)
    }

    fun startSpinning() {
        isSpinning = true
        stopIndex = (0 until colors.size).random()
        invalidate()
    }

    fun resetState() {
        currentColorIndex = 0
        isSpinning = false
        isStopped = false
        drawnBitmap = null
        lastStopColorIndex = -1
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDrum(canvas)
        if (isStopped) {
            val centerX = width / 2f
            val topY = height / 4f
            val stopColorIndex = stopIndex % colors.size
            if (imageUrls[stopColorIndex].isNotEmpty()) {
                drawImage(canvas, centerX, topY, stopColorIndex)
            } else {
                drawText(canvas, centerX, topY, stopColorIndex)
            }
        }
        if (isSpinning) {
            animateDrum()
        }
    }

    private fun drawDrum(canvas: Canvas) {
        val radius = width / 2f
        val startAngle = 360f / colors.size * currentColorIndex
        val sweepAngle = 360f / colors.size
        for (i in colors.indices) {
            paint.color = colors[i]
            canvas.drawArc(
                0f, height.toFloat() - radius * 2,
                width.toFloat(), height.toFloat(),
                startAngle + i * sweepAngle, sweepAngle, true, paint
            )
            if (i == currentColorIndex && isStopped) {
                highlightRectF.set(
                    0f, height.toFloat() - radius * 2,
                    width.toFloat(), height.toFloat()
                )
                canvas.drawArc(
                    highlightRectF,
                    startAngle + i * sweepAngle, sweepAngle, false, highlightPaint
                )
            }
        }
    }

    private fun animateDrum() {
        currentColorIndex += 1
        if (currentColorIndex >= colors.size) currentColorIndex = 0

        if (currentColorIndex == stopIndex) {
            isSpinning = false
            isStopped = true
            invalidate()
        } else {
            postInvalidateDelayed(100, 0, 0, width, height)
        }
    }

    private fun drawText(canvas: Canvas, centerX: Float, topY: Float, stopColorIndex: Int) {
        val text = texts[stopColorIndex]
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val x = centerX - textBounds.width() / 2f
        val y = topY + textBounds.height() / 2f
        canvas.drawText(text, x, y, textPaint)
    }

    private fun drawImage(canvas: Canvas, centerX: Float, topY: Float, stopColorIndex: Int) {
               if (stopColorIndex != lastStopColorIndex || drawnBitmap == null) {
            val bitmap = loadImage(stopColorIndex)
            if (bitmap != null) {
                val left = centerX - bitmap.width / 2f
                val top = topY - bitmap.height / 2f
                drawnBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height)
                lastStopColorIndex = stopColorIndex
            }
        }

        drawnBitmap?.let {
            val left = centerX - it.width / 2f
            val top = topY - it.height / 2f
            canvas.drawBitmap(it, left, top, paint)
        }
    }

    private fun loadImage(colorIndex: Int): Bitmap? {
        if (imageUrls[colorIndex].isEmpty()) return null
        Picasso.get()
            .load(imageUrls[colorIndex])
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .into(object : Target {
                override fun onBitmapLoaded(resource: Bitmap?, from: Picasso.LoadedFrom?) {
                    if (resource != null) {
                        bitmapCache[colorIndex] = resource
                        invalidate()
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
        return bitmapCache[colorIndex]
    }
}