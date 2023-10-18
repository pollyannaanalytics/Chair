package com.example.reclaim.chatgpt

import com.squareup.moshi.Json

data class CompletionRequest(
    val model: String,
    val prompt: String,

    val max_tokens: Int,
    val temperature: Float = 0f,

    )