package com.smspeer.app.modules.gateway

import com.smspeer.app.modules.events.EventBus
import com.smspeer.app.modules.events.EventsReceiver

class EventsReceiver : EventsReceiver() {
    override suspend fun collect(eventBus: EventBus) {
        // Cloud event handling removed: no remote workers to dispatch
    }
}
