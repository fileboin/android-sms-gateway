package com.smspeer.app.domain

enum class EntitySource {
    Local,
    Cloud,

    @Deprecated("Not used anymore")
    Gateway,
}