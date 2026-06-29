package com.smspeer.app.modules.gateway.events

import com.smspeer.app.modules.events.AppEvent

class WebhooksUpdatedEvent : AppEvent(NAME) {
    companion object {
        const val NAME = "WebhooksUpdatedEvent"
    }
}