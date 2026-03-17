package com.furkansoyleyici.visualvibe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furkansoyleyici.visualvibe.R


@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    isSpotifyLoading: Boolean,
    onLoginClick: () -> Unit
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A1A12), Color(0xFF050505))
    )
    val accentGreen = Color(0xFF1DB954)

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
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "VisualVibe",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Müzik zevkinize uygun görsel analiz ve şarkı keşfine başlamak için giriş yapın.",
                fontSize = 16.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = onLoginClick,
                enabled = !isSpotifyLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                shape = RoundedCornerShape(32.dp)
            ) {
                if (isSpotifyLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("BAĞLANIYOR...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_spotify),
                        contentDescription = "Spotify Logo",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("SPOTIFY ILE GİRİŞ YAP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
