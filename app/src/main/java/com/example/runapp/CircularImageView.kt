package com.example.runapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CircularImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val path: Path = Path()
    private val circlePaint: Paint = Paint()

    init {
        circlePaint.color = Color.parseColor("#689c9f")
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 10f
    }

    override fun onDraw(canvas: Canvas) {
        val radius = minOf(width, height) / 2.4f

        // Draw the circle
        canvas.drawCircle(width / 2f, height / 2f, radius, circlePaint)

        // Create a circular path to clip the image
        path.addCircle(width / 2f, height / 2f, radius - circlePaint.strokeWidth / 2, Path.Direction.CW)
        // Clip the canvas with the circular path
        canvas.clipPath(path)

        // Draw the image
        super.onDraw(canvas)
    }
}