package com.furkansoyleyici.visualvibe.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkansoyleyici.visualvibe.GeminiVibeEngine
import com.furkansoyleyici.visualvibe.utils.NetworkUtils
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    spotifyData: String?,
    isSpotifyLoading: Boolean,
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vibeEngine = remember { GeminiVibeEngine() }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    var uiState by remember { mutableStateOf<VibeUiState>(VibeUiState.Idle) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A1A12), Color(0xFF050505))
    )
    val cardBackground = Color(0xFF1E2E26).copy(alpha = 0.7f)
    val accentGreen = Color(0xFF1DB954)
    val errorRed = Color(0xFFE57373)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
        if (uri != null) {
            val bitmap = uriToBitmap(context, uri)
            selectedBitmap = bitmap
            if (bitmap != null) {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    uiState = VibeUiState.Error("İnternet bağlantınızı kontrol edin ve tekrar deneyin.")
                    return@rememberLauncherForActivityResult
                }
                
                uiState = VibeUiState.Loading

                scope.launch {
                    val fallbackData = "Kullanıcı verisi henüz yüklenmedi"
                    val dataToUse = spotifyData ?: fallbackData
                    val result = vibeEngine.analyzeVibe(bitmap, dataToUse)
                    if (result != null) {
                         uiState = VibeUiState.Success(result)
                    } else {
                         uiState = VibeUiState.Error("Analiz sırasında bir hata oluştu veya bağlantı kesildi.")
                    }
                }
            } else {
                uiState = VibeUiState.Error("Fotoğraf okunamadı. Lütfen başka bir resim seçin.")
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "VisualVibe",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )



            AnimatedContent(
                targetState = selectedBitmap != null,
                label = "ImageState"
            ) { hasImage ->
                if (hasImage) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Seçilen Fotoğraf",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .shadow(24.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            color = cardBackground,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when(val state = uiState) {
                                    is VibeUiState.Idle -> { }
                                    is VibeUiState.Loading -> {
                                        CircularProgressIndicator(color = accentGreen)
                                        Text("Vibe Analiz Ediliyor...", color = Color.White, modifier = Modifier.padding(top = 8.dp))
                                    }
                                    is VibeUiState.Success -> {
                                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = accentGreen)
                                        Text(
                                            text = state.result.suggestedSong,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 12.dp)
                                        )
                                        Text(
                                            text = state.result.explanation,
                                            fontSize = 14.sp,
                                            color = Color.LightGray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                        )
                                    }
                                    is VibeUiState.Error -> {
                                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = errorRed)
                                        Text(
                                            text = state.message,
                                            color = errorRed,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardBackground)
                            .clickable {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = accentGreen, modifier = Modifier.size(64.dp))
                            Text("Vibe'ını yakalamak için fotoğraf yükle", color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(if (selectedBitmap == null) "GALERİYİ AÇ" else "DEĞİŞTİR", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}