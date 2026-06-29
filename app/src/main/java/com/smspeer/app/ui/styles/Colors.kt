package com.smspeer.app.ui.styles

import android.graphics.Color

val com.smspeer.app.domain.ProcessingState.color: Int
    get() = when (this) {
        com.smspeer.app.domain.ProcessingState.Pending -> Color.parseColor("#FFBB86FC")
        com.smspeer.app.domain.ProcessingState.Processed -> Color.parseColor("#FF6200EE")
        com.smspeer.app.domain.ProcessingState.Sent -> Color.parseColor("#FF3700B3")
        com.smspeer.app.domain.ProcessingState.Delivered -> Color.parseColor("#FF03DAC5")
        com.smspeer.app.domain.ProcessingState.Failed -> Color.parseColor("#FF018786")
    }