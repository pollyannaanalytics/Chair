package com.example.reclaim.chatgpt

import com.squareup.moshi.Json

data class CompletionResponse(
    val id: String,
    val obj: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val text: String,
    val index: Int,
    val logprobs: Any?,

    @Json(name = "first_reason")
    val first_reason: String
)

data class Usage(
    @Json(name = "prompt_tokens")
    val prompt_tokens: Int,

    @Json(name = "completion_tokens")
    val completion_tokens: Int,


    val total_tokens: Int
)