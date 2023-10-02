package com.example.reclaim.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoom(
    val key: String,
    val selfId: String,
    val otherId: String,
    val selfName: String,
    val otherName: String,
    val lastSentence: String,
    val sendById: String,
    val selfImage: String,
    val otherImage: String,
    val sentTime: String
) : Parcelable

@Entity(tableName = "chat_room")
data class ChatRoomLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "self_id")
    val selfId: String,

    @ColumnInfo(name = "other_id")
    val otherId: String,

    @ColumnInfo(name = "self_name")
    val selfName: String,

    @ColumnInfo(name = "other_name")
    val otherName: String,

    @ColumnInfo(name = "last_sentence")
    val lastSentence: String,

    @ColumnInfo(name = "send_by_id")
    val sendById: String,

    @ColumnInfo(name = "self_image")
    val selfImage: String,

    @ColumnInfo(name = "other_image")
    val otherImage: String
    )