package com.smspeer.app.modules.messages.data

import com.smspeer.app.data.entities.MessageRecipient
import com.smspeer.app.domain.EntitySource
import com.smspeer.app.domain.ProcessingState

class StoredSendRequest(
    val id: Long,
    val state: ProcessingState,
    val recipients: List<MessageRecipient>,
    source: EntitySource,
    message: Message,
    params: SendParams
) :
    SendRequest(source, message, params)