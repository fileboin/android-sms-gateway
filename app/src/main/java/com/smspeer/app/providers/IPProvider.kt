package com.smspeer.app.providers

interface IPProvider {
    suspend fun getIP(): String?
}