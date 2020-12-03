package com.ukrainianboyz.nearly.utils.layout

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

import android.text.style.ReplacementSpan


class RoundedBackgroundSpan(private val _backgroundColor: Int, private val _textColor: Int) : ReplacementSpan() {
    private val _padding = 15

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        return ((_padding + paint.measureText(
            text.subSequence(start, end).toString()
        ) + _padding).toInt())
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val width: Float = paint.measureText(text.subSequence(start, end).toString())
        val rect = RectF(x - _padding, top.toFloat(), x + width + _padding, bottom.toFloat())
        paint.color = _backgroundColor
        canvas.drawRoundRect(rect, _padding.toFloat(), _padding.toFloat(), paint)
        paint.color = _textColor
        canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }

}