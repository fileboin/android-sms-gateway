package com.smspeer.app.domain

import com.smspeer.app.BuildConfig
import com.smspeer.app.modules.health.domain.CheckResult
import com.smspeer.app.modules.health.domain.HealthResult
import com.smspeer.app.modules.health.domain.Status

class HealthResponse(
    healthResult: HealthResult,

    val version: String = BuildConfig.VERSION_NAME,
    val releaseId: Int = BuildConfig.VERSION_CODE,
) {
    val status: Status = healthResult.status
    val checks: Map<String, CheckResult> = healthResult.checks
}