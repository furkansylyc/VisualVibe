package com.furkansoyleyici.visualvibe

import com.furkansoyleyici.visualvibe.BuildConfig
import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.scale
import com.furkansoyleyici.visualvibe.ui.VibeResult

class GeminiVibeEngine {
    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    suspend fun analyzeVibe(bitmap: Bitmap, spotifyData: String): VibeResult? =
        withContext(Dispatchers.IO) {
            try {
                val maxDimension = 1024
                val scale = maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)
                val scaledBitmap = if (scale < 1.0f) {
                    bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt())
                } else {
                    bitmap
                }

                var parsedArtists = spotifyData
                try {
                    val jsonObject = org.json.JSONObject(spotifyData)
                    val items = jsonObject.optJSONArray("items")
                    if (items != null) {
                        val namesAndGenres = mutableListOf<String>()
                        for (i in 0 until items.length()) {
                            val item = items.optJSONObject(i)
                            val name = item?.optString("name")
                            val genresArray = item?.optJSONArray("genres")
                            val genres = mutableListOf<String>()
                            if (genresArray != null) {
                                for (j in 0 until Math.min(
                                    genresArray.length(),
                                    3
                                )) {
                                    genres.add(genresArray.getString(j))
                                }
                            }
                            if (name != null) {
                                namesAndGenres.add("$name (${genres.joinToString(", ")})")
                            }
                        }
                        if (namesAndGenres.isNotEmpty()) {
                            parsedArtists = namesAndGenres.joinToString(" | ")
                        }
                    }
                } catch (ignore: Exception) {
                }

                val prompt = """
                Kullanıcının Spotify müzik karakteri (En çok dinlediği sanatçılar ve türler): $parsedArtists. 

                Görevin:
                1. Analiz: Bu fotoğrafın renk paletini, ışığını ve mekanını bir küratör gibi detaylıca analiz et.
                2. Eşleştirme: Fotoğrafın yarattığı atmosfer (melankolik, enerjik, vintage vb.) ile kullanıcının müzik zevki arasında nasıl bir köprü kurduğunu açıkla. Neden bu sanatçıyı veya şarkıyı seçtiğini detaylandır.
                3. Genişletme: Sadece listedeki sanatçılardan değil, bu türleri temsil eden benzeri (hidden gem) sanatçılardan da ilham alabilirsin.

                Lütfen çıktını tam olarak şu formatta ver:

                [ŞARKI]
                **Şarkı Adı** - Sanatçı Adı

                [ANALİZ]
                Buraya seçtiğin şarkının arkasındaki detaylı düşünce yapısını, fotoğraf analizini ve müzik zevkiyle olan ilişkisini yaz.
            """.trimIndent()

                val response = model.generateContent(
                    content {
                        image(scaledBitmap)
                        text(prompt)
                    }
                )
                
                val responseText = response.text?.trim() ?: return@withContext null
                
                val songTag = "[ŞARKI]"
                val analysisTag = "[ANALİZ]"
                
                val songIndex = responseText.indexOf(songTag)
                val analysisIndex = responseText.indexOf(analysisTag)
                
                if (songIndex != -1 && analysisIndex != -1) {
                    val songPart = if (songIndex < analysisIndex) {
                        responseText.substring(songIndex + songTag.length, analysisIndex).trim()
                    } else {
                        responseText.substring(songIndex + songTag.length).trim()
                    }
                    
                    val analysisPart = if (analysisIndex < songIndex) {
                        responseText.substring(analysisIndex + analysisTag.length, songIndex).trim()
                    } else {
                        responseText.substring(analysisIndex + analysisTag.length).trim()
                    }
                    
                    VibeResult(
                        suggestedSong = songPart.replace("**", ""),
                        explanation = analysisPart,
                        artistsUsed = parsedArtists
                    )
                } else {
                    VibeResult(
                        suggestedSong = "Spotify Vibe'ı",
                        explanation = responseText,
                        artistsUsed = parsedArtists
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("GeminiVibeEngine", "Gemini API error", e)
                null
            }
        }
}