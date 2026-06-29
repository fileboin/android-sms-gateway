package com.smspeer.app.modules.ping.events

import com.smspeer.app.domain.HealthResponse
import com.smspeer.app.modules.events.AppEvent

class PingEvent(
    val health: HealthResponse,
) : AppEvent(TYPE) {
    companion object {
        const val TYPE = "PingEvent"
    }
}