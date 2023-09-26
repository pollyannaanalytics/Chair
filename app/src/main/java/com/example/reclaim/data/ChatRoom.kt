package com.example.reclaim.data

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
    )