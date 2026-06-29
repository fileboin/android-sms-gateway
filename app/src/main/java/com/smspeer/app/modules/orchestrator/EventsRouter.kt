package com.smspeer.app.modules.orchestrator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.smspeer.app.modules.events.EventBus
import com.smspeer.app.modules.events.ExternalEvent
import com.smspeer.app.modules.events.ExternalEventType
import com.smspeer.app.modules.gateway.events.MessageEnqueuedEvent
import com.smspeer.app.modules.gateway.events.SettingsUpdatedEvent
import com.smspeer.app.modules.gateway.events.WebhooksUpdatedEvent
import com.smspeer.app.modules.receiver.events.MessagesExportRequestedEvent

class EventsRouter(
    private val eventBus: EventBus
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun route(event: ExternalEvent) {
        scope.launch {
            when (event.type) {
                ExternalEventType.MessageEnqueued ->
                    eventBus.emit(
                        MessageEnqueuedEvent()
                    )

                ExternalEventType.WebhooksUpdated ->
                    eventBus.emit(
                        WebhooksUpdatedEvent()
                    )

                ExternalEventType.MessagesExportRequested ->
                    eventBus.emit(
                        MessagesExportRequestedEvent.withPayload(
                            requireNotNull(event.data)
                        )
                    )

                ExternalEventType.SettingsUpdated ->
                    eventBus.emit(
                        SettingsUpdatedEvent()
                    )
            }
        }
    }
}