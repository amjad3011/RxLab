package com.github.anastr.rxlab.objects.drawing

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.github.anastr.rxlab.objects.emits.EmitObject
import com.github.anastr.rxlab.objects.time.TimeObject
import com.github.anastr.rxlab.util.Point
import com.github.anastr.rxlab.util.Utils
import com.github.anastr.rxlab.util.dpToPx

/**
 * Created by Anas Altair on 3/20/2020.
 */
class ObserverObject(name:String): DrawingObject(name) {

    private val arrowPath = Path()
    private val arrowEndX
        get () = rect.width() * .95f
    private val completePath = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val namePadding = dpToPx(4f)

    private val garbageEmits = ArrayList<Int>()
    private var isComplete = false

    private val timeObjects = java.util.ArrayList<TimeObject>()
    private val garbageTimes = ArrayList<Int>()

    init {
        paint.color = Color.DKGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpToPx(3f)
        paint.strokeCap = Paint.Cap.ROUND

        updatePath()
    }

    private fun updatePath() {
        arrowPath.reset()
        arrowPath.moveTo(0f, rect.centerY())
        arrowPath.lineTo(arrowEndX, rect.centerY())
        arrowPath.moveTo(arrowEndX - dpToPx(20f), rect.centerY() - dpToPx(20f))
        arrowPath.lineTo(arrowEndX, rect.centerY())
        arrowPath.lineTo(arrowEndX - dpToPx(20f), rect.centerY() + dpToPx(20f))

        completePath.reset()
        completePath.moveTo(arrowEndX - dpToPx(40f), rect.centerY() - dpToPx(7f))
        completePath.lineTo(arrowEndX - dpToPx(40f), rect.centerY() + dpToPx(7f))
    }

    override fun onSizeChanged(width: Int, height: Int) {
        super.onSizeChanged(width, height)
        updatePath()
    }

    override fun onAddEmit(emitObject: EmitObject) {
        if (isComplete)
            throw IllegalStateException("you can't add emits after onComplete!")
    }

    /**
     * add [TimeObject] at [getInsertPoint] depending on [lock].
     *
     * **must be called on render thread.**
     */
    fun startTime(lock: TimeObject.Lock): TimeObject {
        val timeObject = TimeObject(getInsertPoint(), lock)
        timeObjects.add(timeObject)
        return timeObject
    }

    /**
     * remove time object, _drop it_.
     *
     * **must be called on render thread.**
     */
    fun removeTime(timeObject: TimeObject) {
        timeObjects.remove(timeObject)
    }

    override fun draw(delta: Long, canvas: Canvas) {

        canvas.drawPath(arrowPath, paint)

        canvas.drawText(name, namePadding, rect.centerY() - namePadding, textPaint)

        emitObjects.forEachIndexed { index, emitObject ->
            if (!isComplete)
                emitObject.rect.offset(- delta * Utils.emitSpeed, 0f)
            emitObject.draw(canvas)
            if (emitObject.rect.right < 0f)
                garbageEmits.add(0, index)
        }
        timeObjects.forEachIndexed { index, timeObject ->
            if (!isComplete) {
                timeObject.rect.left -= delta * Utils.emitSpeed
                timeObject.rect.right -= if (timeObject.locked) delta * Utils.emitSpeed else 0f
            }
            timeObject.draw(canvas)
            if (timeObject.rect.right < 0f)
                garbageTimes.add(0, index)
        }
        if (isComplete)
            canvas.drawPath(completePath, paint)

        garbageEmits.forEach { removeEmitAt(it) }
        garbageEmits.clear()
        garbageTimes.forEach { timeObjects.removeAt(it) }
        garbageTimes.clear()
    }

    override fun getInsertPoint(): Point
            = Point(arrowEndX - dpToPx(80f) - Utils.emitSize *.5f, rect.centerY() - Utils.emitSize *.5f)

    /**
     * must be called on render thread.
     */
    fun complete() {
        isComplete = true
    }
}