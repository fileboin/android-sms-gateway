package me.capcom.smsgateway.modules.webhooks.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import me.capcom.smsgateway.modules.logs.LogsService
import me.capcom.smsgateway.modules.logs.db.LogEntry
import me.capcom.smsgateway.modules.webhooks.NAME
import me.capcom.smsgateway.modules.webhooks.db.WebhookQueueRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Processes the webhook queue by marking all pending entries as completed locally.
 * Outbound HTTP delivery has been removed — webhooks are not sent over the network.
 */
class WebhookQueueProcessorWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val logsSvc: LogsService by inject()
    private val webhookRepository: WebhookQueueRepository by inject()

    companion object {
        private const val WORK_NAME = "webhook_queue_processor"

        @Suppress("UNUSED_PARAMETER")
        fun start(
            context: Context,
            internetRequired: Boolean = false
        ) {
            val workRequest = OneTimeWorkRequestBuilder<WebhookQueueProcessorWorker>().build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }

        fun stop(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        logsSvc.insert(
            priority = LogEntry.Priority.INFO,
            module = NAME,
            message = "Webhook queue processing: marking all pending as completed (outbound HTTP disabled)"
        )

        try {
            val pending = webhookRepository.getPendingWebhooks(limit = 200)
            for (webhook in pending) {
                webhookRepository.startProcessing(webhook.id)
                webhookRepository.completeWebhook(webhook.id)
            }
            webhookRepository.cleanupOldEntries()
        } catch (e: Exception) {
            logsSvc.insert(
                priority = LogEntry.Priority.ERROR,
                module = NAME,
                message = "Webhook queue cleanup failed: ${e.message}"
            )
        }

        return Result.success()
    }
}
