package com.smspeer.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Crash screen — built 100% programmatically with zero XML/style dependencies.
 * Safe to show even when themes, Koin DI, or resources are completely broken.
 */
class CrashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Don't call super.onCreate() with a theme that might be broken.
        // We call it first so the Activity lifecycle works, then override everything.
        super.onCreate(savedInstanceState)

        val report = intent.getStringExtra(EXTRA_REPORT)
            ?: "No crash report available.\nCheck smspeer_crash.txt with a file manager."
        val extPath = intent.getStringExtra(EXTRA_EXT_PATH) ?: ""
        val intPath = intent.getStringExtra(EXTRA_INT_PATH) ?: ""

        // Build the full UI in code — zero XML inflation
        setContentView(buildUi(report, extPath, intPath))
    }

    private fun dp(v: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
    ).toInt()

    private fun buildUi(report: String, extPath: String, intPath: String): LinearLayout {
        val bg       = Color.parseColor("#0F1419")
        val surface  = Color.parseColor("#1E2530")
        val accent   = Color.parseColor("#FF6B35")
        val red      = Color.parseColor("#EF4444")
        val white    = Color.parseColor("#F0F4F8")
        val grey     = Color.parseColor("#7A8899")
        val redBg    = Color.parseColor("#2A0D0D")

        // Root
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(16), dp(40), dp(16), dp(16))

            // ── Title ─────────────────────────────────────────────────────────
            addView(TextView(context).apply {
                text = "SMSpeer — Crash Report"
                setTextColor(red)
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    .apply { bottomMargin = dp(8) }
            })

            // ── Red banner ────────────────────────────────────────────────────
            addView(TextView(context).apply {
                text = "Unhandled exception caught. Select the text below, " +
                        "copy it and share it for analysis. " +
                        "The report was also saved as smspeer_crash.txt."
                setTextColor(white)
                textSize = 12f
                setBackgroundColor(redBg)
                setPadding(dp(10), dp(10), dp(10), dp(10))
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    .apply { bottomMargin = dp(8) }
            })

            // ── Stack trace (scrollable, selectable) ──────────────────────────
            addView(ScrollView(context).apply {
                setBackgroundColor(surface)
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
                    .apply { bottomMargin = dp(8) }

                addView(TextView(context).apply {
                    text = report
                    setTextColor(white)
                    textSize = 11f
                    typeface = Typeface.MONOSPACE
                    setTextIsSelectable(true)
                    setPadding(dp(8), dp(8), dp(8), dp(8))
                    setLineSpacing(0f, 1.3f)
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                })
            })

            // ── File path ─────────────────────────────────────────────────────
            if (extPath.isNotEmpty() || intPath.isNotEmpty()) {
                addView(TextView(context).apply {
                    text = buildString {
                        append("Saved to:\n")
                        if (extPath.isNotEmpty()) appendLine(extPath)
                        if (intPath.isNotEmpty()) append(intPath)
                    }.trim()
                    setTextColor(grey)
                    textSize = 10f
                    typeface = Typeface.MONOSPACE
                    setTextIsSelectable(true)
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        .apply { topMargin = dp(4); bottomMargin = dp(8) }
                })
            }

            // ── Buttons ───────────────────────────────────────────────────────
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

                addView(makeButton(context, "Restart", surface, white, dp(8)).apply {
                    setOnClickListener {
                        packageManager.getLaunchIntentForPackage(packageName)
                            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            ?.let { startActivity(it) }
                        finish()
                    }
                })

                addView(makeButton(context, "Share", surface, white, dp(8)).apply {
                    setOnClickListener {
                        startActivity(Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, report)
                                putExtra(Intent.EXTRA_SUBJECT, "SMSpeer Crash Report")
                            },
                            "Share crash report"
                        ))
                    }
                })

                addView(makeButton(context, "Copy", accent, Color.WHITE, 0).apply {
                    setOnClickListener {
                        val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cb.setPrimaryClip(ClipData.newPlainText("crash", report))
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    }
                })
            })
        }
    }

    private fun makeButton(
        ctx: Context, label: String, bgColor: Int, textColor: Int, endMargin: Int
    ) = Button(ctx).apply {
        text = label
        setTextColor(textColor)
        setBackgroundColor(bgColor)
        typeface = Typeface.DEFAULT_BOLD
        textSize = 13f
        setPadding(dp(16), dp(8), dp(16), dp(8))
        layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            .apply { rightMargin = endMargin }
    }

    companion object {
        const val EXTRA_REPORT   = "smspeer.extra.CRASH_REPORT"
        const val EXTRA_EXT_PATH = "smspeer.extra.EXT_PATH"
        const val EXTRA_INT_PATH = "smspeer.extra.INT_PATH"
    }
}
