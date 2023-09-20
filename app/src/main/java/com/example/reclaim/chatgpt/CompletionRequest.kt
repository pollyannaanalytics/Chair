package com.example.reclaim.chatgpt

import com.squareup.moshi.Json

data class CompletionRequest(
    val model: String,
    val prompt: String,
    @Json(name = "max_tokens")
    val maxTokens: Int,
    val temperature: Float = 0f,

    )