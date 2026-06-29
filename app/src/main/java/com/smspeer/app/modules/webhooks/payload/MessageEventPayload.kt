package com.smspeer.app.modules.webhooks.payload

abstract class MessageEventPayload(
    val messageId: String,
    val sender: String?,
    val recipient: String?,
    val simNumber: Int?,

    @Deprecated("Use sender for received events, recipient for sent events")
    val phoneNumber: String,
)
