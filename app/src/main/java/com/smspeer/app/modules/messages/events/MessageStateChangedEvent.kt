package com.smspeer.app.modules.messages.events

import com.smspeer.app.domain.EntitySource
import com.smspeer.app.domain.ProcessingState
import com.smspeer.app.modules.events.AppEvent

class MessageStateChangedEvent(
    val id: String,
    val source: EntitySource,
    val phoneNumbers: Set<String>,
    val state: ProcessingState,
    val simNumber: Int?,
    val partsCount: Int?,
    val error: String?
): AppEvent(NAME) {

    companion object {
        const val NAME = "MessageStateChangedEvent"
    }
}