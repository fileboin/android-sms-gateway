package me.capcom.smsgateway.modules.webhooks.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.capcom.smsgateway.modules.logs.LogsService
import me.capcom.smsgateway.modules.logs.db.LogEntry
import me.capcom.smsgateway.modules.webhooks.NAME
import me.capcom.smsgateway.modules.webhooks.db.WebhookQueueRepository
import me.capcom.smsgateway.modules.webhooks.domain.WebHookEventDTO
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

/**
 * Webhook delivery is disabled — outbound HTTP calls have been removed.
 * This worker records webhook events locally and marks them as completed.
 *
 * @deprecated Remove after 2026-11-30
 */
class SendWebhookWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {

    private val logsSvc: LogsService by inject()

    override suspend fun doWork(): Result {
        logsSvc.insert(
            priority = LogEntry.Priority.INFO,
            module = NAME,
            message = "Webhook delivery skipped (outbound HTTP disabled)"
        )
        return Result.success()
    }

    companion object : KoinComponent {
        @Suppress("UNUSED_PARAMETER")
        fun start(
            context: Context,
            url: String,
            data: WebHookEventDTO,
            internetRequired: Boolean,
        ) {
            val logsService = get<LogsService>()

            logsService.insert(
                priority = LogEntry.Priority.DEBUG,
                module = NAME,
                message = "Webhook enqueued (delivery disabled — outbound HTTP removed)",
                context = mapOf("url" to url)
            )

            WebhookQueueProcessorWorker.start(context = context)
        }
    }
}
