package com.smspeer.app.modules.localserver.routes

import android.content.Context
import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import com.smspeer.app.data.entities.MessageWithRecipients
import com.smspeer.app.domain.EntitySource
import com.smspeer.app.domain.MessageContent
import com.smspeer.app.domain.ProcessingState
import com.smspeer.app.helpers.DateTimeParser
import com.smspeer.app.modules.localserver.LocalServerSettings
import com.smspeer.app.modules.localserver.auth.AuthScopes
import com.smspeer.app.modules.localserver.auth.requireScope
import com.smspeer.app.modules.localserver.domain.PostMessagesInboxExportRequest
import com.smspeer.app.modules.localserver.domain.messages.DataMessage
import com.smspeer.app.modules.localserver.domain.messages.PostMessageRequest
import com.smspeer.app.modules.localserver.domain.messages.TextMessage
import com.smspeer.app.modules.messages.MessagesService
import com.smspeer.app.modules.messages.data.Message
import com.smspeer.app.modules.messages.data.SendParams
import com.smspeer.app.modules.messages.data.SendRequest
import com.smspeer.app.modules.messages.exceptions.ConflictException
import com.smspeer.app.modules.receiver.ReceiverService
import java.util.Date

class MessagesRoutes(
    private val context: Context,
    private val messagesService: MessagesService,
    private val receiverService: ReceiverService,
    private val settings: LocalServerSettings,
) {
    fun register(routing: Route) {
        routing.apply {
            messagesRoutes()
            route("/inbox") {
                inboxRoutes(context)
            }
        }
    }

    private fun Route.messagesRoutes() {
        get {
            if (!requireScope(AuthScopes.MessagesRead)) return@get
            // Parse and validate parameters
            val state = call.request.queryParameters["state"]?.takeIf { it.isNotEmpty() }
                ?.let { ProcessingState.valueOf(it) }
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val includeContent = call.request.queryParameters["includeContent"]?.let {
                it.toBooleanStrictOrNull() ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("message" to "includeContent must be true or false")
                    )
                    return@get
                }
            } ?: false

            // Parse date range parameters
            val from = call.request.queryParameters["from"]?.let {
                DateTimeParser.parseIsoDateTime(it)?.time
            } ?: 0
            val to = call.request.queryParameters["to"]?.let {
                DateTimeParser.parseIsoDateTime(it)?.time
            } ?: Date().time

            val deviceId = call.request.queryParameters["deviceId"]
            if (deviceId != null && deviceId != settings.deviceId) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Invalid device ID")
                )
                return@get
            }

            // Ensure start date is before end date
            if (from > to) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Start date cannot be after end date")
                )
                return@get
            }

            // Get total count for pagination
            val total = try {
                messagesService.countMessages(EntitySource.Local, state, from, to)
            } catch (e: Throwable) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Failed to count messages: ${e.message}")
                )
                return@get
            }

            // Get messages with pagination
            val messages = try {
                messagesService.selectMessages(EntitySource.Local, state, from, to, limit, offset)
            } catch (e: Throwable) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Failed to retrieve messages: ${e.message}")
                )
                return@get
            }

            call.response.headers.append("X-Total-Count", total.toString())

            call.respond(
                messages.map {
                    it.toDomain(requireNotNull(settings.deviceId), includeContent)
                }
            )
        }

        post {
            if (!requireScope(AuthScopes.MessagesSend)) return@post
            val request = call.receive<PostMessageRequest>().validate()

            if (request.deviceId?.let { it == settings.deviceId } == false) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Invalid device ID")
                )
                return@post
            }

            val skipPhoneValidation =
                call.request.queryParameters["skipPhoneValidation"]
                    ?.toBooleanStrict() ?: false

            // Create message content based on type
            val messageContent = when {
                request.message != null -> {
                    MessageContent.Text(request.message)
                }

                request.textMessage != null -> {
                    MessageContent.Text(request.textMessage.text)
                }

                request.dataMessage != null -> {
                    MessageContent.Data(
                        request.dataMessage.data,
                        request.dataMessage.port.toUShort()
                    )
                }

                else -> {
                    // This case should be caught by validation, but just in case
                    throw IllegalStateException("Unknown message type")
                }
            }

            val sendRequest = SendRequest(
                EntitySource.Local,
                Message(
                    request.id ?: NanoIdUtils.randomNanoId(),
                    content = messageContent,
                    phoneNumbers = request.phoneNumbers,
                    isEncrypted = request.isEncrypted ?: false,
                    createdAt = Date(),
                ),
                SendParams(
                    request.withDeliveryReport ?: true,
                    skipPhoneValidation = skipPhoneValidation,
                    simNumber = request.simNumber,
                    validUntil = request.validUntil,
                    scheduleAt = request.scheduleAt,
                    priority = request.priority,
                )
            )

            val message = try {
                messagesService.enqueueMessage(sendRequest)
            } catch (e: ConflictException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("message" to e.message)
                )
                return@post
            }


            call.respond(
                HttpStatusCode.Accepted,
                message.toDomain(requireNotNull(settings.deviceId), true)
            )
        }
        get("{id}") {
            if (!requireScope(AuthScopes.MessagesRead)) return@get
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val message = try {
                messagesService.getMessage(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound)
            } catch (e: Throwable) {
                return@get call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to e.message)
                )
            }

            call.respond(
                message.toDomain(
                    requireNotNull(settings.deviceId),
                    includeContent = true
                )
            )
        }
    }

    private fun Route.inboxRoutes(context: Context) {
        post("export") {
            if (!requireScope(AuthScopes.MessagesExport)) return@post
            val request = call.receive<PostMessagesInboxExportRequest>().validate()
            try {
                receiverService.export(context, request.period, true)
                call.respond(HttpStatusCode.Accepted)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("message" to "Failed to export inbox: ${e.message}")
                )
            }
        }
    }

    private fun MessageWithRecipients.toDomain(
        deviceId: String,
        includeContent: Boolean = false
    ): com.smspeer.app.modules.localserver.domain.messages.Message {
        return com.smspeer.app.modules.localserver.domain.messages.Message(
            id = message.id,
            deviceId = deviceId,
            state = message.state,
            isHashed = false,
            isEncrypted = message.isEncrypted,
            textMessage = when (includeContent) {
                true -> message.textContent?.let {
                    TextMessage(it.text)
                }

                else -> null
            },
            dataMessage = when (includeContent) {
                true -> message.dataContent?.let {
                    DataMessage(
                        data = it.data,
                        port = it.port.toInt()
                    )
                }

                else -> null
            },
            hashedMessage = null,
            recipients = recipients.map {
                com.smspeer.app.modules.localserver.domain.messages.Message.Recipient(
                    it.phoneNumber,
                    it.state,
                    it.error
                )
            },
            states = states.associate {
                it.state to Date(it.updatedAt)
            },
            scheduleAt = message.scheduleAt,
        )
    }
}
