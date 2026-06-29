package com.smspeer.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.smspeer.app.modules.logs.LogsService
import com.smspeer.app.modules.logs.LogsUtils.toLogContext
import com.smspeer.app.modules.logs.db.LogEntry
import com.smspeer.app.modules.messages.MODULE_NAME
import com.smspeer.app.modules.messages.MessagesService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EventsReceiver : BroadcastReceiver(), KoinComponent {

    private val messagesService: MessagesService by inject()
    private val logsService: LogsService by inject()

    override fun onReceive(context: Context, intent: Intent) {
        scope.launch {
            try {
                messagesService
                    .processStateIntent(intent, resultCode)
            } catch (e: Throwable) {
                logsService.insert(
                    LogEntry.Priority.ERROR,
                    MODULE_NAME,
                    "Can't process message state intent",
                    intent.toLogContext() + e.toLogContext()
                )
            }

        }
    }

    companion object {
        private val job = SupervisorJob()
        private val scope = CoroutineScope(job)

        private var INSTANCE: EventsReceiver? = null

        const val ACTION_SENT = "com.smspeer.app.ACTION_SENT"
        const val ACTION_DELIVERED = "com.smspeer.app.ACTION_DELIVERED"

        private fun getInstance(): EventsReceiver {
            return INSTANCE ?: EventsReceiver().also { INSTANCE = it }
        }

        fun register(context: Context) {
            context.registerReceiver(
                getInstance(),
                IntentFilter(ACTION_SENT)
                    .apply { addAction(ACTION_DELIVERED) }
            )
        }
    }
}