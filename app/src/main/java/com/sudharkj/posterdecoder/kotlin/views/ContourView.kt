package com.sudharkj.posterdecoder.kotlin.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import java.util.*
import kotlin.collections.ArrayList


class ContourView : ImageView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    companion object {
        val path = Paint().also {
            it.setARGB(255, 0, 0, 0)
            it.strokeWidth = 5f
            it.style = Paint.Style.STROKE
        }
        val dot = Paint().also {
            it.setARGB(255, 0, 0, 0)
            it.strokeWidth = 15f
        }
        val selectedDot = Paint().also {
            it.setARGB(255, 0, 255, 0)
            it.strokeWidth = 20f
        }
        val points: Queue<Point> = LinkedList()
        var pathPoints = FloatArray(0)
        var selectedPoint: Point? = null
        var isShowContour = false
        var contourPath: Path = Path()
    }

    fun showContour() {
        isShowContour = !isShowContour
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // draw points
        canvas?.drawPoints(pathPoints, dot)!!

        // select point
        selectedPoint?.let {
            canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), selectedDot)
        }

        // draw path
        if (isShowContour) {
            contourPath.reset()
            contourPath.moveTo(pathPoints[0], pathPoints[1])
            contourPath.lineTo(pathPoints[2], pathPoints[3])
            contourPath.lineTo(pathPoints[4], pathPoints[5])
            contourPath.lineTo(pathPoints[0], pathPoints[1])
            canvas.drawPath(contourPath, path)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val point = event?.let { Point(event.x.toInt(), event.y.toInt()) }!!

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> { updatePath(point) }
            MotionEvent.ACTION_UP -> { updatePath(point) }
        }

        return true
    }

    private fun updatePath(point: Point) {
        if (!isPointInPath(point)) {
            while (points.size >= 3) {
                points.poll()
            }

            points.add(point)
            selectedPoint = point

            pathPoints = points.map {
                var list = ArrayList<Float>()
                list.add(it.x.toFloat())
                list.add(it.y.toFloat())
                list
            }.flatten().toFloatArray()

            invalidate()
        }
    }

    private fun isPointInPath(point: Point): Boolean {
        for (p in points) {
            if (p == point) return true
        }
        return false
    }
}