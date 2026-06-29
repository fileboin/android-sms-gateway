package com.smspeer.app.modules.settings

interface Importer {
    fun import(data: Map<String, *>): Boolean
}