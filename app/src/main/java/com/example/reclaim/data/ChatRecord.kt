package com.example.reclaim.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "chat_record")
data class ChatRecord(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "record_id")
    val id: String ="",

    @ColumnInfo(name = "content")
    val content : String = "",

    @ColumnInfo(name = "send_time")
    val sendTime: String = "",

    @ColumnInfo(name = "sender_id")
    val sender : String = "",

    @ColumnInfo(name = "receiver_id")
    val receiver: String = ""
)