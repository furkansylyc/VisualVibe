package com.furkansoyleyici.visualvibe

import android.graphics.Bitmap
import android.util.Log // 1. Yeni malzeme: Log yazdırmak için gerekli kütüphane
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class ImageAnalyzer {
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    fun AnalyzePhoto(bitmap: Bitmap, onResult: (List<String>) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        // Test için: Analiz başladığında haber ver
        Log.d("BUSE_TEST", "Analiz süreci başlatıldı...")

        labeler.process(image)
            .addOnSuccessListener { labels ->
                val result = labels.map { it.text }
                
                // 2. GEÇİCİ TEST SATIRI: Bulunan kelimeleri Logcat ekranına yazdırır
                Log.d("BUSE_TEST", "Bulunan Nesneler: $result")
                
                onResult(result)
            }
            .addOnFailureListener { e ->
                // Test için: Hata olursa sebebini yazdır
                Log.e("BUSE_TEST", "Hata oluştu: ${e.message}")
                onResult(emptyList())
            }
    }
}