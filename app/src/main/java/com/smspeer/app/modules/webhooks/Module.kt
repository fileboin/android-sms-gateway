package com.smspeer.app.modules.webhooks

import com.smspeer.app.modules.webhooks.db.WebhookQueueRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val webhooksModule = module {
    singleOf(::WebHooksService)
    singleOf(::WebhookQueueRepository)
    singleOf(::WebhookPayloadStorage)
}

val NAME = "webhooks"
