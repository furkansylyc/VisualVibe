package com.furkansoyleyici.visualvibe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.json.JSONObject

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userProfileJson: String?,
    topArtistsJson: String?
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A1A12), Color(0xFF050505))
    )
    val cardBackground = Color(0xFF1E2E26).copy(alpha = 0.7f)
    val accentGreen = Color(0xFF1DB954)

    // Parsing JSON safely
    val userName = remember(userProfileJson) {
        try {
            if (userProfileJson != null) {
                JSONObject(userProfileJson).getString("display_name")
            } else "Bilinmeyen Kullanıcı"
        } catch (e: Exception) {
            "Bilinmeyen Kullanıcı"
        }
    }

    val userImageUrl = remember(userProfileJson) {
        try {
            if (userProfileJson != null) {
                val images = JSONObject(userProfileJson).optJSONArray("images")
                if (images != null && images.length() > 0) {
                    images.getJSONObject(0).getString("url")
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    val topArtists = remember(topArtistsJson) {
        val list = mutableListOf<Pair<String, String?>>()
        try {
            if (topArtistsJson != null) {
                val items = JSONObject(topArtistsJson).getJSONArray("items")
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val name = item.getString("name")
                    val images = item.optJSONArray("images")
                    val imageUrl = if (images != null && images.length() > 0) {
                        images.getJSONObject(0).getString("url")
                    } else null
                    list.add(Pair(name, imageUrl))
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        list
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profilim",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // User Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cardBackground,
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userImageUrl != null) {
                        AsyncImage(
                            model = userImageUrl,
                            contentDescription = "Profil Resmi",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(userName.take(1).uppercase(), fontSize = 32.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = userName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Spotify Kullanıcısı",
                            fontSize = 14.sp,
                            color = accentGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Favori Şarkıcıların",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )

            // Artists List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(topArtists) { artist ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1E2E26).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (artist.second != null) {
                                AsyncImage(
                                    model = artist.second,
                                    contentDescription = artist.first,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color.DarkGray)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(artist.first.take(1).uppercase(), color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = artist.first,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
