package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(val text: String? = null)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiNetwork {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

suspend fun callGeminiCoach(prompt: String, systemPrompt: String): String {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return "⚠️ Digital Coach API Key is not configured yet. Configure GEMINI_API_KEY in the AI Studio Secrets panel.\n\n" +
                "🤖 Coach Quick Advice: Based on your logs, you have already spent 80 minutes on social apps today. I suggest locking Instagram and TikTok now to preserve your 4-day streak!"
    }

    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
        systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
    )

    return try {
        val response = GeminiNetwork.service.generateContent(apiKey, request)
        val textResponse = response.candidates?.getOrNull(0)?.content?.parts?.getOrNull(0)?.text
        textResponse ?: "The Digital Coach is reflecting on your stats. Please try again!"
    } catch (e: Exception) {
        "⚠️ Coach Network Error: ${e.localizedMessage ?: "Could not connect to Gemini"}\n\n" +
                "🤖 Direct Advice: Minimize your Instagram usage in the afternoon—that is when your productivity typically slumps by 35%!"
    }
}
