package me.capcom.smsgateway.modules.gateway

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.capcom.smsgateway.modules.events.EventBus
import me.capcom.smsgateway.modules.logs.LogsService
import me.capcom.smsgateway.modules.messages.MessagesService

class GatewayService(
    private val messagesService: MessagesService,
    private val settings: GatewaySettings,
    private val events: EventBus,
    private val logsService: LogsService,
) {
    fun start(context: Context) {}

    fun stop(context: Context) {}

    fun isActiveLiveData(context: Context): LiveData<Boolean> = MutableLiveData(false)

    suspend fun getLoginCode(): GatewayApi.GetUserCodeResponse {
        throw UnsupportedOperationException("Cloud features are disabled in this build")
    }

    suspend fun getLoginCodeWithPassword(password: String): GatewayApi.GetUserCodeResponse {
        throw UnsupportedOperationException("Cloud features are disabled in this build")
    }

    suspend fun changePassword(current: String, new: String) {
        throw UnsupportedOperationException("Cloud features are disabled in this build")
    }

    internal suspend fun registerDevice(
        pushToken: String?,
        registerMode: RegistrationMode
    ) {
        throw UnsupportedOperationException("Cloud registration is disabled in this build")
    }

    internal suspend fun updateDevice(pushToken: String?) {}

    suspend fun getPublicIP(): String {
        throw UnsupportedOperationException("Cloud features are disabled in this build")
    }

    sealed class RegistrationMode {
        object Anonymous : RegistrationMode()
        class WithCredentials(val login: String, val password: String) : RegistrationMode()
        class WithCode(val code: String) : RegistrationMode()
    }
}
