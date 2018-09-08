package com.anwesh.uiprojects.triopenshiftview

/**
 * Created by anweshmishra on 09/09/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

val nodes : Int = 5

fun Canvas.drawTOSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    paint.color = Color.parseColor("#3949AB")
    paint.strokeWidth = Math.min(w, h) / 50
    paint.strokeCap = Paint.Cap.ROUND
    val size : Float = gap/3
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(scale - 0.5f, 0f)) * 2
    if (sc2 == 0f) {
        paint.style = Paint.Style.FILL_AND_STROKE
    } else {
        paint.style = Paint.Style.FILL
    }
    save()
    translate(w/2, gap + gap * i)
    for(j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        save()
        translate((w/2 - size) * sc2 + size/2, 0f * sc2)
        rotate(180f * sc2)
        val path : Path = Path()
        path.moveTo(-size/2, -size/2)
        path.lineTo(-size/2 + (size) * sc1, 0f)
        path.lineTo(-size/2, size/2)
        drawPath(path, paint)
        restore()
        restore()
    }
    restore()
}

class TriOpenShiftView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {
        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {
        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class TOSNode(var i : Int) {
        private val state : State = State()
        private var next : TOSNode? = null
        private var prev : TOSNode? = null

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = TOSNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawTOSNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun getNext(dir : Int, cb : () -> Unit) : TOSNode {
            var curr : TOSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedTriOpenShift(var i : Int) {
        private var root : TOSNode = TOSNode(0)
        private var curr : TOSNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : TriOpenShiftView) {

        private val animator : Animator = Animator(view)
        private val ltos : LinkedTriOpenShift = LinkedTriOpenShift(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            ltos.draw(canvas, paint)
            animator.animate {
                ltos.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ltos.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : TriOpenShiftView {
            val view : TriOpenShiftView = TriOpenShiftView(activity)
            activity.setContentView(view)
            return view
        }
    }
}