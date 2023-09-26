package com.example.reclaim.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "chat_record")
data class ChatRecord(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "chat_room_key")
    val chatRoomKey: String ,

    @ColumnInfo(name = "content")
    val content : String ,

    @ColumnInfo(name = "sent_time")
    val sendTime: String,

    @ColumnInfo(name = "sender_id")
    val sender : String,

    val type: String,

    val meetingId: String
)