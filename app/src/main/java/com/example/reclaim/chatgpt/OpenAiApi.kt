package com.example.reclaim.chatgpt

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAiApi {
    @Headers("Authorization: Bearer ${API_KEY.API_PROFILE}")
    @POST("v1/completions")
    suspend fun getCompletion(@Body completionResponse: CompletionRequest): Response<CompletionResponse>
}