package com.smspeer.app.modules.events

import org.koin.dsl.module

val eventBusModule = module {
    single { EventBus() }
}