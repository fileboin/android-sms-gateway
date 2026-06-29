package com.smspeer.app

import android.app.Application
import android.content.Context
import healthModule
import com.smspeer.app.data.dbModule
import com.smspeer.app.helpers.LocaleHelper
import com.smspeer.app.modules.connection.connectionModule
import com.smspeer.app.modules.encryption.encryptionModule
import com.smspeer.app.modules.events.eventBusModule
import com.smspeer.app.modules.incoming.incomingModule
import com.smspeer.app.modules.localserver.localserverModule
import com.smspeer.app.modules.logs.logsModule
import com.smspeer.app.modules.messages.messagesModule
import com.smspeer.app.modules.notifications.notificationsModule
import com.smspeer.app.modules.orchestrator.OrchestratorService
import com.smspeer.app.modules.orchestrator.orchestratorModule
import com.smspeer.app.modules.ping.pingModule
import com.smspeer.app.modules.receiver.receiverModule
import com.smspeer.app.modules.settings.settingsModule
import com.smspeer.app.modules.webhooks.webhooksModule
import com.smspeer.app.receivers.EventsReceiver
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App: Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.let { LocaleHelper.onAttach(it) })
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                eventBusModule,
                settingsModule,
                dbModule,
                logsModule,
                notificationsModule,
                messagesModule,
                incomingModule,
                receiverModule,
                encryptionModule,
                com.smspeer.app.modules.gateway.gatewayModule,
                healthModule,
                webhooksModule,
                localserverModule,
                pingModule,
                connectionModule,
                orchestratorModule,
            )
        }

        Thread.setDefaultUncaughtExceptionHandler(
            GlobalExceptionHandler(
                Thread.getDefaultUncaughtExceptionHandler()!!,
                get()
            )
        )

        instance = this

        EventsReceiver.register(this)

        get<OrchestratorService>().start(this, true)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
