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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
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
    private var spotifyProfile by mutableStateOf<String?>(null)
    private var isSpotifyLoading by mutableStateOf(false)
    private var currentTab by mutableStateOf("home")

    private val spotifyLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val token = spotifyManager.handleLoginResponse(result.resultCode, result.data)
        if (token != null) {
            secureStorage.saveToken(token)
            fetchSpotifyDataWithToken(token)
        } else {
            Toast.makeText(this, "Spotify girişi başarısız veya iptal edildi.", Toast.LENGTH_SHORT).show()
            isSpotifyLoading = false
        }
    }

    private fun fetchSpotifyDataWithToken(token: String) {
        isSpotifyLoading = true
        spotifyManager.fetchUserProfile(token) { profileData ->
            runOnUiThread {
                spotifyProfile = profileData
                spotifyManager.fetchUserTopArtists(token) { artistsData ->
                    runOnUiThread {
                        if (artistsData != null) {
                            spotifyData = artistsData
                        } else {
                            Toast.makeText(this@MainActivity, "Spotify verileri alınamadı.", Toast.LENGTH_SHORT).show()
                        }
                        isSpotifyLoading = false
                    }
                }
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
            fetchSpotifyDataWithToken(savedToken)
        }
        
        setContent {
            VisualVibeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (spotifyData != null) {
                            NavigationBar(
                                containerColor = androidx.compose.ui.graphics.Color(0xFF0A1A12),
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == "home",
                                    onClick = { currentTab = "home" },
                                    icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = "Home") },
                                    label = { Text("Analiz") }
                                )
                                NavigationBarItem(
                                    selected = currentTab == "profile",
                                    onClick = { currentTab = "profile" },
                                    icon = { Icon(Icons.Rounded.AccountCircle, contentDescription = "Profile") },
                                    label = { Text("Profil") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    if (spotifyData != null) {
                        if (currentTab == "home") {
                            MainScreen(
                                modifier = Modifier.padding(innerPadding),
                                spotifyData = spotifyData,
                                isSpotifyLoading = isSpotifyLoading,
                                onLoginClick = {}
                            )
                        } else {
                            com.furkansoyleyici.visualvibe.ui.ProfileScreen(
                                modifier = Modifier.padding(innerPadding),
                                userProfileJson = spotifyProfile,
                                topArtistsJson = spotifyData,
                                onLogoutClick = {
                                    secureStorage.clearToken()
                                    spotifyData = null
                                    spotifyProfile = null
                                    currentTab = "home"
                                }
                            )
                        }
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