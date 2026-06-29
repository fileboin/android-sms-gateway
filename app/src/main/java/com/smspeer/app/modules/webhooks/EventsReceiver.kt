package com.smspeer.app.modules.webhooks

import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.smspeer.app.domain.ProcessingState
import com.smspeer.app.helpers.SubscriptionsHelper
import com.smspeer.app.modules.events.EventBus
import com.smspeer.app.modules.events.EventsReceiver
import com.smspeer.app.modules.messages.events.MessageStateChangedEvent
import com.smspeer.app.modules.ping.events.PingEvent
import com.smspeer.app.modules.webhooks.domain.WebHookEvent
import com.smspeer.app.modules.webhooks.payload.SmsEventPayload
import org.koin.core.component.get
import java.util.Date

class EventsReceiver : EventsReceiver() {
    override suspend fun collect(eventBus: EventBus) {
        coroutineScope {
            launch {
                eventBus.collect<PingEvent> {
                    Log.d("EventsReceiver", "Event: $it")

                    get<WebHooksService>().emit(
                        get(),
                        WebHookEvent.SystemPing,
                        mapOf("health" to it.health)
                    )
                }
            }

            launch {
                eventBus.collect<MessageStateChangedEvent> { event ->
                    Log.d("EventsReceiver", "Event: $event")

                    val webhookEventType = when (event.state) {
                        ProcessingState.Sent -> WebHookEvent.SmsSent
                        ProcessingState.Delivered -> WebHookEvent.SmsDelivered
                        ProcessingState.Failed -> WebHookEvent.SmsFailed
                        else -> return@collect
                    }

                    // Get sender's device number using SubscriptionsHelper
                    val context = get<android.content.Context>()
                    val sender = event.simNumber?.let { simIndex ->
                        SubscriptionsHelper.getPhoneNumber(context, simIndex - 1)
                    }

                    event.phoneNumbers.forEach { destination ->
                        val payload = when (webhookEventType) {
                            WebHookEvent.SmsSent -> SmsEventPayload.SmsSent(
                                messageId = event.id,
                                simNumber = event.simNumber,
                                partsCount = event.partsCount ?: -1,
                                sentAt = Date(),
                                sender = sender,
                                recipient = destination,
                            )

                            WebHookEvent.SmsDelivered -> SmsEventPayload.SmsDelivered(
                                messageId = event.id,
                                simNumber = event.simNumber,
                                deliveredAt = Date(),
                                sender = sender,
                                recipient = destination,
                            )

                            WebHookEvent.SmsFailed -> SmsEventPayload.SmsFailed(
                                messageId = event.id,
                                simNumber = event.simNumber,
                                failedAt = Date(),
                                reason = event.error ?: "Unknown",
                                sender = sender,
                                recipient = destination,
                            )

                            else -> return@forEach
                        }

                        get<WebHooksService>().emit(
                            get(), webhookEventType, payload
                        )
                    }
                }
            }
        }
    }
}