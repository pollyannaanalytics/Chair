package com.example.reclaim.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json


data class ChatRecord(



    val chatRoomKey: String ,

    val content : String ,


    val sendTime: String,

    val sender : String,

    val type: MessageType,

    val meetingId: String,

    val otherImage : String,

    val selfImage: String,

    val selfName : String,

    val otherName: String,

    val isSeen: Boolean,
    val meetingOver: Boolean

)