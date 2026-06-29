package com.smspeer.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithRecipients(
    @Embedded val message: Message,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId",
    )
    val recipients: List<MessageRecipient>,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId",
    )
    val states: List<MessageState> = emptyList(),
    @ColumnInfo(name = "rowid")
    val rowId: Long = 0,
) {
    val state: com.smspeer.app.domain.ProcessingState
        get() = when {
            recipients.any { it.state == com.smspeer.app.domain.ProcessingState.Pending } -> com.smspeer.app.domain.ProcessingState.Pending
            recipients.any { it.state == com.smspeer.app.domain.ProcessingState.Processed } -> com.smspeer.app.domain.ProcessingState.Processed

            recipients.all { it.state == com.smspeer.app.domain.ProcessingState.Failed } -> com.smspeer.app.domain.ProcessingState.Failed
            recipients.all { it.state == com.smspeer.app.domain.ProcessingState.Delivered } -> com.smspeer.app.domain.ProcessingState.Delivered
            else -> com.smspeer.app.domain.ProcessingState.Sent
        }
}
