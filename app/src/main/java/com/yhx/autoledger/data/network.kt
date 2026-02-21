package com.yhx.autoledger.data.network

import com.yhx.autoledger.models.ChatRequest
import com.yhx.autoledger.models.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LlmApiService {
    @POST("v1/chat/completions")
    suspend fun getAiCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: ChatRequest
    ): ChatResponse
}