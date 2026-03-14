package com.furkansoyleyici.visualvibe

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiVibeEngine {
    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash", 
        apiKey = apiKey
    )

    suspend fun analyzeVibe(bitmap: Bitmap, spotifyData: String): String? = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Kullanıcının Spotify müzik karakteri (En çok dinlediği 50 sanatçı ve tür): $spotifyData. 

                Görevin:
                1. Analiz: Bu fotoğrafın renk paletini, ışığını ve mekanını bir küratör gibi analiz et.
                2. Eşleştirme: Fotoğrafın yarattığı atmosfer (melankolik, enerjik, vintage vb.) ile kullanıcının müzik zevki arasında bir köprü kur.
                3. Genişletme: Sadece listedeki sanatçılardan değil, bu türleri temsil eden benzeri (hidden gem) sanatçılardan da ilham al.

                Sonuç Formatı:
                **Şarkı Adı - Sanatçı**
                (Sadece bu formatta cevap ver, başka hiçbir açıklama veya metin ekleme.)
            """.trimIndent()

            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            response.text?.trim()
        } catch (e: Exception) {
            null
        }
    }
}