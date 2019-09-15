package com.svobnick.thisorthat.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.svobnick.thisorthat.R


class PieChart(context: Context, attr: AttributeSet) : View(context, attr) {
    companion object {
        private lateinit var oval: RectF
        private var landscape: Boolean = false
        private val paintDown = Paint()
        private val paintUp = Paint()
        private var percentDown: Int = 75
    }

    init {
        paintUp.isAntiAlias = true;
        paintUp.color = resources.getColor(R.color.first_dark)
        paintDown.isAntiAlias = true;
        paintDown.color = resources.getColor(R.color.last_dark)
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val viewSize: Int
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (landscape) {
            viewSize = View.MeasureSpec.getSize(widthMeasureSpec)
        } else {
            viewSize = View.MeasureSpec.getSize(heightMeasureSpec)
        }
        // todo move initialization in constructor
        oval = RectF(0.0f, 0.0f, viewSize.toFloat(), viewSize.toFloat())
        setMeasuredDimension(viewSize, viewSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val sweepAngle = percentDown * 360 / 100
        val halfOfDiff = (180 - sweepAngle) / 2
        if (landscape) {
            canvas.drawArc(
                oval,
                (halfOfDiff - 90).toFloat(),
                (-(360 - sweepAngle)).toFloat(),
                true,
                paintUp
            )
            canvas.drawArc(
                oval,
                (halfOfDiff - 90).toFloat(),
                sweepAngle.toFloat(),
                true,
                paintDown
            )
            return
        }
        canvas.drawArc(
            oval,
            halfOfDiff.toFloat(),
            (-(360 - sweepAngle)).toFloat(),
            true,
            paintUp
        )
        canvas.drawArc(
            oval,
            halfOfDiff.toFloat(),
            sweepAngle.toFloat(),
            true,
            paintDown
        )
    }


    fun setUpPercents(value: Int) {
        percentDown = value
        invalidate()
    }
}
