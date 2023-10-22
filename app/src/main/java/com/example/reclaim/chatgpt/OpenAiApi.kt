package com.example.reclaim.chatgpt

import com.example.reclaim.BuildConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiApi {
    @Headers("Authorization: Bearer ${BuildConfig.OPEN_AI_KEY}")
    @POST("v1/completions")
    suspend fun getCompletion(@Body completionResponse: CompletionRequest): Response<CompletionResponse>
}