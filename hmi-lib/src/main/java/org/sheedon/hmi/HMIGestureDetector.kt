package org.sheedon.hmi

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller

/**
 * HMI视图手势检测器，放大缩小，移动
 *
 * @Author: sheedon
 * @Email: sheedonsun@163.com
 * @Date: 2022/2/23 3:20 下午
 */
@SuppressLint("ClickableViewAccessibility")
class HMIGestureDetector(context: Context, val view: View) {

    // 比例手势检测器
    private val mScaleGestureDetector: ScaleGestureDetector =
        ScaleGestureDetector(context, ScaleGestureListener())
    // 手势检测器
    private var mGestureDetector: GestureDetector? =
        GestureDetector(context, GestureListener(context))
    private var preScale = 1f // 默认前一次缩放比例为1
    private var scale = 1f
    private var isMultiFingered = false

    init {
        view.setOnTouchListener { _, event ->
            val temp = event.action and MotionEvent.ACTION_MASK
            if (temp == MotionEvent.ACTION_POINTER_DOWN) {
                isMultiFingered = true
            } else if (temp == MotionEvent.ACTION_POINTER_UP) {
                isMultiFingered = false
            }

            if (isMultiFingered) {
                mScaleGestureDetector.onTouchEvent(event)
            } else {
                mGestureDetector!!.onTouchEvent(event)
            }
        }
    }


    private inner class ScaleGestureListener : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {

            val previousSpan = detector.previousSpan
            val currentSpan = detector.currentSpan
            scale = if (currentSpan < previousSpan) {
                // 缩小
                preScale - (previousSpan - currentSpan) / 50
            } else {
                // 放大
                preScale + (currentSpan - previousSpan) / 50
            }

            if (scale < 0.5) {
                scale = 0.5F
            }

            // 缩放view
            view.scaleX = scale
            view.scaleY = scale
            return false
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            // 一定要返回true才会进入onScale()这个函数
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            preScale = scale //记录本次缩放比例
        }
    }

    private inner class GestureListener(context: Context) :
        GestureDetector.SimpleOnGestureListener() {
        private val mScroller = OverScroller(context)

        override fun onDown(e: MotionEvent?): Boolean {
            //Cancel any current fling
            if (!mScroller.isFinished) {
                mScroller.abortAnimation();
            }
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            view.scrollBy((distanceX / scale).toInt(), (distanceY / scale).toInt())
            return true
        }

    }

}