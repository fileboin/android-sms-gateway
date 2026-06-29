package com.smspeer.app.modules.ping

import com.smspeer.app.modules.settings.Exporter
import com.smspeer.app.modules.settings.Importer
import com.smspeer.app.modules.settings.KeyValueStorage
import com.smspeer.app.modules.settings.get

class PingSettings(
    private val storage: KeyValueStorage,
) : Exporter, Importer {
    val enabled: Boolean
        get() = intervalSeconds != null

    var intervalSeconds: Int?
        get() = storage.get<Int>(INTERVAL_SECONDS)?.takeIf { it > 0 }
        set(value) = storage.set(INTERVAL_SECONDS, value)

    companion object {
        private const val INTERVAL_SECONDS = "interval_seconds"
    }

    override fun export(): Map<String, *> {
        return mapOf(
            INTERVAL_SECONDS to intervalSeconds,
        )
    }

    override fun import(data: Map<String, *>): Boolean {
        return data.map { (key, value) ->
            when (key) {
                INTERVAL_SECONDS -> {
                    val newValue = value?.toString()?.toFloat()?.toInt()?.takeIf { it > 0 }
                    val changed = this.intervalSeconds != newValue

                    storage.set(
                        key,
                        newValue?.toString()
                    )

                    changed
                }

                else -> false
            }
        }.any { it }
    }
}