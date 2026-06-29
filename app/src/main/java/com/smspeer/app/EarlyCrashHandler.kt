package com.smspeer.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Installed as the VERY FIRST UncaughtExceptionHandler in App.onCreate(),
 * before startKoin and any other initialization code.
 *
 * On any unhandled exception it:
 *   1. Builds a human-readable crash report with full stack trace
 *   2. Writes the report to <external-files>/smspeer_crash.txt
 *   3. Launches CrashActivity so the user can read/copy/share the trace
 *   4. Kills the process (suppresses the OS "app stopped" dialog)
 */
class EarlyCrashHandler(
    private val context: Context,
    private val previousHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val report = buildReport(thread, throwable)
            writeToFile(report)
            startCrashActivity(report)
            // Give the Activity a moment to start before we kill the process
            Thread.sleep(800)
        } catch (secondary: Throwable) {
            Log.e(TAG, "EarlyCrashHandler itself failed", secondary)
            previousHandler?.uncaughtException(thread, throwable)
            return
        }
        Process.killProcess(Process.myPid())
    }

    private fun buildReport(thread: Thread, throwable: Throwable): String {
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val versionName = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (_: Throwable) { "?" }

        return buildString {
            appendLine("╔══════════════════════════════════════")
            appendLine("║  SMSpeer — Crash Report")
            appendLine("╠══════════════════════════════════════")
            appendLine("║  Time    : $ts")
            appendLine("║  Thread  : ${thread.name}")
            appendLine("║  Version : $versionName")
            appendLine("║  Device  : ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("║  Android : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("╚══════════════════════════════════════")
            appendLine()

            var ex: Throwable? = throwable
            var first = true
            while (ex != null) {
                if (!first) {
                    appendLine()
                    appendLine("Caused by:")
                }
                appendLine("${ex.javaClass.name}: ${ex.message}")
                ex.stackTrace.forEach { frame ->
                    appendLine("    at $frame")
                }
                ex = ex.cause
                first = false
            }
        }
    }

    private fun writeToFile(report: String) {
        try {
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            File(dir, CRASH_FILE_NAME).writeText(report, Charsets.UTF_8)
        } catch (_: Throwable) {
            // Writing to external storage might fail — silently ignore
        }
        // Always write to internal storage as a fallback
        try {
            File(context.filesDir, CRASH_FILE_NAME).writeText(report, Charsets.UTF_8)
        } catch (_: Throwable) {}
    }

    private fun startCrashActivity(report: String) {
        // Android Binder limit is ~1 MB per transaction. Truncate if needed.
        val safeReport = if (report.length > 60_000)
            report.take(60_000) + "\n\n[... truncated — see smspeer_crash.txt for full report ...]"
        else report

        val extPath = context.getExternalFilesDir(null)
            ?.let { File(it, CRASH_FILE_NAME).absolutePath } ?: ""
        val intPath = File(context.filesDir, CRASH_FILE_NAME).absolutePath

        context.startActivity(
            Intent(context, CrashActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
                putExtra(CrashActivity.EXTRA_REPORT, safeReport)
                putExtra(CrashActivity.EXTRA_EXT_PATH, extPath)
                putExtra(CrashActivity.EXTRA_INT_PATH, intPath)
            }
        )
    }

    companion object {
        const val TAG = "EarlyCrashHandler"
        const val CRASH_FILE_NAME = "smspeer_crash.txt"
    }
}
