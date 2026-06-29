package com.smspeer.app.modules.messages.data

import com.smspeer.app.domain.EntitySource

open class SendRequest(
    val source: EntitySource,
    val message: Message,
    val params: SendParams,
)