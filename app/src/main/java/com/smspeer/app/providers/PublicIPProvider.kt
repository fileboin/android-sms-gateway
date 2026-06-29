package com.smspeer.app.providers

class PublicIPProvider : IPProvider {
    override suspend fun getIP(): String? {
        // Cloud IP lookup removed — returns null (no outbound HTTP)
        return null
    }
}
