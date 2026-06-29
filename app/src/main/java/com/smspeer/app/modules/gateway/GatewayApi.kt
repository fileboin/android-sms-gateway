package com.smspeer.app.modules.gateway

import com.google.gson.annotations.SerializedName
import com.smspeer.app.domain.ProcessingState
import com.smspeer.app.modules.webhooks.domain.WebHookEvent
import java.util.Date

class GatewayApi {
    data class DeviceGetResponse(
        val externalIp: String,
    )

    data class DeviceRegisterRequest(
        val name: String,
        val pushToken: String?,
    )

    data class DeviceRegisterResponse(
        val id: String,
        val token: String,
        val login: String,
        val password: String?,
    )

    data class DevicePatchRequest(
        val id: String,
        val pushToken: String?,
    )

    data class MessagePatchRequest(
        val id: String,
        val state: ProcessingState,
        val recipients: List<RecipientState>,
        val states: Map<ProcessingState, Date>
    )

    data class PasswordChangeRequest(
        val currentPassword: String,
        val newPassword: String
    )

    data class GetUserCodeResponse(
        val code: String,
        val validUntil: Date
    )

    sealed class MessageContent {
        class Text(
            val text: String,
        ) : MessageContent()

        class Data(
            val data: String,
            val port: UShort,
        ) : MessageContent()
    }

    data class Message(
        val id: String,
        @SerializedName("textMessage")
        val _textMessage: MessageContent.Text?,
        @SerializedName("dataMessage")
        val _dataMessage: MessageContent.Data?,
        val phoneNumbers: List<String>,
        val simNumber: Int?,
        val withDeliveryReport: Boolean?,
        val isEncrypted: Boolean?,
        val validUntil: Date?,
        val scheduleAt: Date?,
        val priority: Byte?,
        val createdAt: Date?,

        @SerializedName("message")
        val _message: String?,
    ) {
        val content: MessageContent
            get() = this._dataMessage
                ?: this._textMessage
                ?: _message?.let { MessageContent.Text(it) }
                ?: throw RuntimeException("Invalid message content")
    }

    data class RecipientState(
        val phoneNumber: String,
        val state: ProcessingState,
        val error: String?,
    )

    data class WebHook(
        val id: String,
        val url: String,
        val event: WebHookEvent,
    )

    enum class ProcessingOrder {
        @SerializedName("lifo")
        LIFO,

        @SerializedName("fifo")
        FIFO;

        override fun toString(): String {
            return this.name.lowercase()
        }
    }
}
