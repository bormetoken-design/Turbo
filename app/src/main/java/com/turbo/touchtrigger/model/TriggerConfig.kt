package com.turbo.touchtrigger.model

enum class ClickMode {
    SINGLE_TAP,
    CRAZY_TAP
}

object TriggerConfig {
    var mode: ClickMode = ClickMode.SINGLE_TAP
    var intervalMs: Long = 80L
    var touchDurationMs: Long = 40L

    var targetX: Float = 500f
    var targetY: Float = 500f

    var triggerX: Float = 200f
    var triggerY: Float = 800f
}
