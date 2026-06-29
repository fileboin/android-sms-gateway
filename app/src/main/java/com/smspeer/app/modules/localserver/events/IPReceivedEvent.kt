package com.smspeer.app.modules.localserver.events

import com.smspeer.app.modules.events.AppEvent

class IPReceivedEvent(
    val localIP: String?,
    val publicIP: String?,
): AppEvent(NAME) {
    companion object {
        const val NAME = "IPReceivedEvent"
    }
}