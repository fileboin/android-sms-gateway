package com.smspeer.app.modules.settings

interface Exporter {
    fun export(): Map<String, *>
}