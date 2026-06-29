package com.smspeer.app.modules.settings

import androidx.preference.PreferenceManager
import com.smspeer.app.helpers.SettingsHelper
import com.smspeer.app.modules.encryption.EncryptionSettings
import com.smspeer.app.modules.gateway.GatewaySettings
import com.smspeer.app.modules.localserver.LocalServerSettings
import com.smspeer.app.modules.logs.LogsSettings
import com.smspeer.app.modules.messages.MessagesSettings
import com.smspeer.app.modules.ping.PingSettings
import com.smspeer.app.modules.receiver.StateStorage
import com.smspeer.app.modules.webhooks.TemporaryStorage
import com.smspeer.app.modules.webhooks.WebhooksSettings
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val settingsModule = module {
    singleOf(::SettingsService)
    factory { PreferenceManager.getDefaultSharedPreferences(get()) }
    factory { SettingsHelper(get()) }

    factory {
        EncryptionSettings(
            PreferencesStorage(get(), "encryption")
        )
    }
    factory {
        GatewaySettings(
            PreferencesStorage(get(), "gateway")
        )
    }
    factory {
        MessagesSettings(
            PreferencesStorage(get(), "messages")
        )
    }
    factory {
        LocalServerSettings(
            PreferencesStorage(get(), "localserver")
        )
    }
    factory {
        PingSettings(
            PreferencesStorage(get(), "ping")
        )
    }
    factory {
        LogsSettings(
            PreferencesStorage(get(), "logs")
        )
    }
    factory {
        WebhooksSettings(
            PreferencesStorage(get(), "webhooks")
        )
    }
    single {
        TemporaryStorage(
            PreferencesStorage(get(), "webhooks")
        )
    }
    single {
        StateStorage(
            PreferencesStorage(get(), "receiver")
        )
    }
}