package com.turbo.touchtrigger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.turbo.touchtrigger.model.ClickMode
import com.turbo.touchtrigger.model.TriggerConfig

class MainActivity : Activity() {

    private lateinit var btnOverlayPermission: Button
    private lateinit var btnAccessibilityPermission: Button
    private lateinit var btnToggleService: Button
    private lateinit var tvStatus: TextView
    private lateinit var rgClickMode: RadioGroup
    private lateinit var sbInterval: SeekBar
    private lateinit var tvIntervalLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun initViews() {
        btnOverlayPermission = findViewById(R.id.btnOverlayPermission)
        btnAccessibilityPermission = findViewById(R.id.btnAccessibilityPermission)
        btnToggleService = findViewById(R.id.btnToggleService)
        tvStatus = findViewById(R.id.tvStatus)
        rgClickMode = findViewById(R.id.rgClickMode)
        sbInterval = findViewById(R.id.sbInterval)
        tvIntervalLabel = findViewById(R.id.tvIntervalLabel)
    }

    private fun setupListeners() {
        btnOverlayPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        btnAccessibilityPermission.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "โปรดค้นหา 'Turbo Trigger' และเปิดใช้งาน", Toast.LENGTH_LONG).show()
        }

        rgClickMode.setOnCheckedChangeListener { _, checkedId ->
            TriggerConfig.mode = if (checkedId == R.id.rbCrazyTap) {
                ClickMode.CRAZY_TAP
            } else {
                ClickMode.SINGLE_TAP
            }
        }

        sbInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val interval = Math.max(30, progress).toLong()
                TriggerConfig.intervalMs = interval
                tvIntervalLabel.text = "ความถี่การคลิกรัว: $interval ms"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnToggleService.setOnClickListener {
            if (!hasOverlayPermission() || !TouchTriggerService.isRunning) {
                Toast.makeText(this, "กรุณาเปิดสิทธิ์ทั้ง 2 ข้อก่อนเริ่มใช้งาน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (FloatingOverlayService.isRunning) {
                stopOverlayService()
            } else {
                startOverlayService()
            }
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, FloatingOverlayService::class.java).apply {
            action = FloatingOverlayService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        btnToggleService.text = getString(R.string.btn_stop_overlay)
    }

    private fun stopOverlayService() {
        val intent = Intent(this, FloatingOverlayService::class.java).apply {
            action = FloatingOverlayService.ACTION_STOP
        }
        startService(intent)
        btnToggleService.text = getString(R.string.btn_start_overlay)
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun updatePermissionStatus() {
        val hasOverlay = hasOverlayPermission()
        val hasAccessibility = TouchTriggerService.isRunning

        btnOverlayPermission.text = if (hasOverlay) "✓ สิทธิ์ Overlay: เปิดแล้ว" else "1. เปิดสิทธิ์แสดงทับหน้าจอ (Overlay)"
        btnAccessibilityPermission.text = if (hasAccessibility) "✓ สิทธิ์แตะหน้าจอ: เปิดแล้ว" else "2. เปิดสิทธิ์แตะหน้าจอ (Accessibility)"

        if (hasOverlay && hasAccessibility) {
            tvStatus.text = getString(R.string.status_ready)
            tvStatus.setTextColor(getColor(R.color.success))
        } else {
            tvStatus.text = getString(R.string.status_need_perm)
            tvStatus.setTextColor(getColor(R.color.text_gray))
        }

        btnToggleService.text = if (FloatingOverlayService.isRunning) {
            getString(R.string.btn_stop_overlay)
        } else {
            getString(R.string.btn_start_overlay)
        }
    }
}
