package com.smspeer.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Shown instead of the OS "app stopped" dialog when an unhandled exception
 * is caught by EarlyCrashHandler. Displays the full stack trace and offers
 * copy / share options.
 *
 * Intentionally has NO Koin dependency injection — it must work even when
 * the crash happened before or during DI initialization.
 */
class CrashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)

        val report = intent.getStringExtra(EXTRA_REPORT)
            ?: "No crash report available.\nTry opening smspeer_crash.txt with a file manager."

        val extPath = intent.getStringExtra(EXTRA_EXT_PATH) ?: ""
        val intPath = intent.getStringExtra(EXTRA_INT_PATH) ?: ""

        findViewById<TextView>(R.id.crashTraceText).apply {
            text = report
            // textIsSelectable is set in XML, but set here as well for safety
            setTextIsSelectable(true)
        }

        val pathInfo = buildString {
            if (extPath.isNotEmpty()) appendLine("External: $extPath")
            if (intPath.isNotEmpty()) appendLine("Internal: $intPath")
        }.trim()

        if (pathInfo.isNotEmpty()) {
            findViewById<TextView>(R.id.crashFilePathText).text =
                "Also saved to:\n$pathInfo"
        }

        findViewById<Button>(R.id.btnCopyCrash).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("SMSpeer crash", report))
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnShareCrash).setOnClickListener {
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, report)
                        putExtra(Intent.EXTRA_SUBJECT, "SMSpeer Crash Report")
                    },
                    "Share crash report"
                )
            )
        }

        findViewById<Button>(R.id.btnRestartApp).setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val EXTRA_REPORT   = "smspeer.extra.CRASH_REPORT"
        const val EXTRA_EXT_PATH = "smspeer.extra.EXT_PATH"
        const val EXTRA_INT_PATH = "smspeer.extra.INT_PATH"
    }
}
