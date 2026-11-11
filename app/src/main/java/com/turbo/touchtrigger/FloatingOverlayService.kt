package com.turbo.touchtrigger

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import com.turbo.touchtrigger.model.TriggerConfig
import com.turbo.touchtrigger.view.ConnectingLineView

class FloatingOverlayService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "turbo_overlay_channel"
        private const val NOTIFICATION_ID = 1001

        var isRunning = false
            private set
    }

    private lateinit var windowManager: WindowManager
    private var triggerView: View? = null
    private var targetView: View? = null
    private var lineView: ConnectingLineView? = null

    private lateinit var triggerParams: WindowManager.LayoutParams
    private lateinit var targetParams: WindowManager.LayoutParams
    private lateinit var lineParams: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf()
            }
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
                setupFloatingViews()
                isRunning = true
            }
        }
        return START_NOT_STICKY
    }

    private fun setupFloatingViews() {
        if (triggerView != null) return

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val inflater = LayoutInflater.from(this)

        // 1. Line Overlay View (Covers full screen, non-touchable)
        lineView = ConnectingLineView(this)
        lineParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(lineView, lineParams)

        // 2. Target Point View (Point 2 - Draggable Target)
        targetView = inflater.inflate(R.layout.layout_target_point, null)
        targetParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = TriggerConfig.targetX.toInt()
            y = TriggerConfig.targetY.toInt()
        }
        setupDraggable(targetView!!, targetParams, isTarget = true)
        windowManager.addView(targetView, targetParams)

        // 3. Trigger Point View (Point 1 - Draggable & Clickable)
        triggerView = inflater.inflate(R.layout.layout_trigger_point, null)
        triggerParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = TriggerConfig.triggerX.toInt()
            y = TriggerConfig.triggerY.toInt()
        }
        setupTriggerInteraction(triggerView!!, triggerParams)
        windowManager.addView(triggerView, triggerParams)

        updateConnectingLine()
    }

    private fun setupDraggable(view: View, params: WindowManager.LayoutParams, isTarget: Boolean) {
        view.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(view, params)

                        if (isTarget) {
                            TriggerConfig.targetX = params.x + (view.width / 2f)
                            TriggerConfig.targetY = params.y + (view.height / 2f)
                        } else {
                            TriggerConfig.triggerX = params.x + (view.width / 2f)
                            TriggerConfig.triggerY = params.y + (view.height / 2f)
                        }
                        updateConnectingLine()
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun setupTriggerInteraction(view: View, params: WindowManager.LayoutParams) {
        var isMoving = false
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isMoving = false
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    // Trigger the touch on the target point
                    TouchTriggerService.instance?.onTriggerDown()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = Math.abs(event.rawX - initialTouchX)
                    val deltaY = Math.abs(event.rawY - initialTouchY)
                    if (deltaX > 15 || deltaY > 15 || isMoving) {
                        isMoving = true
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(view, params)

                        TriggerConfig.triggerX = params.x + (view.width / 2f)
                        TriggerConfig.triggerY = params.y + (view.height / 2f)
                        updateConnectingLine()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    TouchTriggerService.instance?.onTriggerUp()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateConnectingLine() {
        val tX = triggerParams.x + 30f
        val tY = triggerParams.y + 30f
        val targetX = targetParams.x + 25f
        val targetY = targetParams.y + 25f
        lineView?.updatePoints(tX, tY, targetX, targetY)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Turbo Touch Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Turbo Trigger Floating Controller"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Turbo Trigger is Active")
            .setContentText("ปุ่มทริกเกอร์ 2 จุดกำลังทำงาน (ออฟไลน์ 100%)")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        triggerView?.let { windowManager.removeView(it) }
        targetView?.let { windowManager.removeView(it) }
        lineView?.let { windowManager.removeView(it) }
        isRunning = false
    }
}
