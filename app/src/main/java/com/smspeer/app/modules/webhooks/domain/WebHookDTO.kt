package com.smspeer.app.modules.webhooks.domain

import com.smspeer.app.domain.EntitySource

data class WebHookDTO(
    val id: String?,
    val deviceId: String?,
    val url: String,
    val event: WebHookEvent,
    val source: EntitySource,
)
