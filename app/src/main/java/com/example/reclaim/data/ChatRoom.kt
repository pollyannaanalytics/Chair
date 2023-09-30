package com.example.reclaim.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoom (
    val key: String,
    val selfId: String,
    val otherId: String,
    val selfName: String,
    val otherName: String,
    val lastSentence: String,
    val sendById: String,
    val selfImage: String,
    val otherImage: String
    ): Parcelable