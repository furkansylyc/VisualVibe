package com.furkansoyleyici.visualvibe

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiVibeEngine {
    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val model = "gemini-1.5-flash"
    apiKey = apiKey
}