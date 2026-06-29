package com.smspeer.app.modules.health

import com.smspeer.app.modules.connection.ConnectionService
import com.smspeer.app.modules.health.domain.HealthResult
import com.smspeer.app.modules.health.domain.Status
import com.smspeer.app.modules.health.monitors.BatteryMonitor
import com.smspeer.app.modules.messages.MessagesService

class HealthService(
    private val messagesSvc: MessagesService,
    private val connectionSvc: ConnectionService,
    private val batteryMon: BatteryMonitor,
) {

    fun healthCheck(): HealthResult {
        val messagesChecks = messagesSvc.healthCheck()
        val connectionChecks = connectionSvc.healthCheck()
        val batteryChecks = batteryMon.healthCheck()

        val allChecks = messagesChecks.mapKeys { "messages:${it.key}" } +
                connectionChecks.mapKeys { "connection:${it.key}" } +
                batteryChecks.mapKeys { "battery:${it.key}" }

        return HealthResult(
            when {
                allChecks.values.any { it.status == Status.FAIL } -> Status.FAIL
                allChecks.values.any { it.status == Status.WARN } -> Status.WARN
                else -> Status.PASS
            },
            allChecks
        )
    }
}