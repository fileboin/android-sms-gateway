package com.smspeer.app.modules.webhooks.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smspeer.app.domain.EntitySource
import com.smspeer.app.modules.webhooks.domain.WebHookEvent

@Entity
data class WebHook(
    @PrimaryKey
    val id: String,
    val url: String,
    val event: WebHookEvent,
    val source: EntitySource,
)