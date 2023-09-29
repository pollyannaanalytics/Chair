package com.example.reclaim.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoom (
    val key: String,
    val userAId: String,
    val userBId: String,
    val userAName: String,
    val userBName: String,
    val lastSentence: String,
    val sendById: String,
    val userAImg: String,
    val userBImg: String
    ): Parcelable