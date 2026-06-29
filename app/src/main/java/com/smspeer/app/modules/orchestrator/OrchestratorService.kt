package com.smspeer.app.modules.orchestrator

import android.content.Context
import com.smspeer.app.helpers.SettingsHelper
import com.smspeer.app.modules.gateway.GatewayService
import com.smspeer.app.modules.localserver.LocalServerService
import com.smspeer.app.modules.logs.LogsService
import com.smspeer.app.modules.logs.db.LogEntry
import com.smspeer.app.modules.messages.MessagesService
import com.smspeer.app.modules.ping.PingService
import com.smspeer.app.modules.receiver.ReceiverService
import com.smspeer.app.modules.webhooks.WebHooksService

class OrchestratorService(
    private val messagesSvc: MessagesService,
    private val gatewaySvc: GatewayService,
    private val localServerSvc: LocalServerService,
    private val webHooksSvc: WebHooksService,
    private val receiverService: ReceiverService,
    private val pingSvc: PingService,
    private val logsSvc: LogsService,
    private val settings: SettingsHelper,
) {
    fun start(context: Context, autostart: Boolean) {
        if (autostart && !settings.autostart) {
            return
        }

        logsSvc.start(context)
        messagesSvc.start(context)
        webHooksSvc.start(context)
        gatewaySvc.start(context)

        try {
            localServerSvc.start(context)
            pingSvc.start(context)
            receiverService.start(context)
        } catch (e: Throwable) {
            logsSvc.insert(
                LogEntry.Priority.WARN,
                MODULE_NAME,
                "Can't start foreground services while the app is running in the background"
            )
        }

        try {
            receiverService.start(context)
        } catch (e: Throwable) {
            logsSvc.insert(
                LogEntry.Priority.WARN,
                MODULE_NAME,
                "Can't register receiver"
            )
        }
    }

    fun stop(context: Context) {
        receiverService.stop(context)
        pingSvc.stop(context)
        localServerSvc.stop(context)

        gatewaySvc.stop(context)
        webHooksSvc.stop(context)
        messagesSvc.stop(context)
        logsSvc.stop(context)
    }
}