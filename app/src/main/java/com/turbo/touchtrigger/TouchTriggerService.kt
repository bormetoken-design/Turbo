package com.turbo.touchtrigger

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.turbo.touchtrigger.model.ClickMode
import com.turbo.touchtrigger.model.TriggerConfig

class TouchTriggerService : AccessibilityService() {

    companion object {
        var instance: TouchTriggerService? = null
            private set

        val isRunning: Boolean
            get() = instance != null
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isRapidFiring = false

    private val rapidFireRunnable = object : Runnable {
        override fun run() {
            if (isRapidFiring) {
                performTap(TriggerConfig.targetX, TriggerConfig.targetY, TriggerConfig.touchDurationMs)
                handler.postDelayed(this, TriggerConfig.intervalMs)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used; strictly used for touch dispatch gestures
    }

    override fun onInterrupt() {
        stopRapidFire()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRapidFire()
        instance = null
    }

    /**
     * Executes a single tap at coordinates (x, y) without Root
     */
    fun performTap(x: Float, y: Float, durationMs: Long = 40L) {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    /**
     * Handles user interaction with Trigger Button (Point 1)
     */
    fun onTriggerDown() {
        when (TriggerConfig.mode) {
            ClickMode.SINGLE_TAP -> {
                performTap(TriggerConfig.targetX, TriggerConfig.targetY, TriggerConfig.touchDurationMs)
            }
            ClickMode.CRAZY_TAP -> {
                startRapidFire()
            }
        }
    }

    fun onTriggerUp() {
        if (TriggerConfig.mode == ClickMode.CRAZY_TAP) {
            stopRapidFire()
        }
    }

    private fun startRapidFire() {
        if (!isRapidFiring) {
            isRapidFiring = true
            handler.post(rapidFireRunnable)
        }
    }

    private fun stopRapidFire() {
        isRapidFiring = false
        handler.removeCallbacks(rapidFireRunnable)
    }
}
