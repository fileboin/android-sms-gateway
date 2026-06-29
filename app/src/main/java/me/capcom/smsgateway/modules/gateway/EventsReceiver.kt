package me.capcom.smsgateway.modules.gateway

import me.capcom.smsgateway.modules.events.EventBus
import me.capcom.smsgateway.modules.events.EventsReceiver

class EventsReceiver : EventsReceiver() {
    override suspend fun collect(eventBus: EventBus) {
        // Cloud event handling removed: no remote workers to dispatch
    }
}
