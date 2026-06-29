package com.smspeer.app.modules.events

data class ExternalEvent(
    val type: ExternalEventType,
    val data: String?,
)
