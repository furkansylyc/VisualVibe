package com.furkansoyleyici.visualvibe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.furkansoyleyici.visualvibe.ui.MainScreen
import com.furkansoyleyici.visualvibe.ui.theme.VisualVibeTheme
import com.furkansoyleyici.visualvibe.utils.NetworkUtils
import com.furkansoyleyici.visualvibe.utils.SecureStorage
import com.furkansoyleyici.visualvibe.SpotifyManager

class MainActivity : ComponentActivity() {

    private lateinit var spotifyManager: SpotifyManager
    private lateinit var secureStorage: SecureStorage
    private var spotifyData by mutableStateOf<String?>(null)
    private var isSpotifyLoading by mutableStateOf(false)

    private val spotifyLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val token = spotifyManager.handleLoginResponse(result.resultCode, result.data)
        if (token != null) {
            secureStorage.saveToken(token)
            fetchArtistsWithToken(token)
        } else {
            Toast.makeText(this, "Spotify girişi başarısız veya iptal edildi.", Toast.LENGTH_SHORT).show()
            isSpotifyLoading = false
        }
    }

    private fun fetchArtistsWithToken(token: String) {
        isSpotifyLoading = true
        spotifyManager.fetchUserTopArtists(token) { data ->
            runOnUiThread {
                if (data != null) {
                    spotifyData = data
                } else {
                    Toast.makeText(this@MainActivity, "Spotify verileri alınamadı.", Toast.LENGTH_SHORT).show()
                }
                isSpotifyLoading = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spotifyManager = SpotifyManager(this)
        secureStorage = SecureStorage(this)
        enableEdgeToEdge()
        
        val savedToken = secureStorage.getToken()
        if (savedToken != null) {
            fetchArtistsWithToken(savedToken)
        }
        
        setContent {
            VisualVibeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (spotifyData != null) {
                        MainScreen(
                            modifier = Modifier.padding(innerPadding),
                            spotifyData = spotifyData,
                            isSpotifyLoading = isSpotifyLoading,
                            onLoginClick = {}
                        )
                    } else {
                        com.furkansoyleyici.visualvibe.ui.LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            isSpotifyLoading = isSpotifyLoading,
                            onLoginClick = {
                                if (NetworkUtils.isNetworkAvailable(this@MainActivity)) {
                                    val intent = spotifyManager.getLoginIntent()
                                    spotifyLoginLauncher.launch(intent)
                                } else {
                                    Toast.makeText(this@MainActivity, "Lütfen internet bağlantınızı kontrol edin.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}