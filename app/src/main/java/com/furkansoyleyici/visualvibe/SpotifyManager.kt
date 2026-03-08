package com.furkansoyleyici.visualvibe

import android.app.Activity
import android.content.Intent
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.*
import java.io.IOException

class SpotifyManager(private val activity: Activity) {

    private val clientId = BuildConfig.SPOTIFY_CLIENT_ID
    private val redirectUri = "visualvibe://callback"

    fun login() {
        val builder = AuthorizationRequest.Builder(
            clientId,
            AuthorizationResponse.Type.TOKEN,
            redirectUri
        )

        builder.setScopes(arrayOf("user-top-read"))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(activity, REQUEST_CODE, request)
    }

    fun handleLoginResponse(resultCode: Int, intent: Intent?): String? {
        val response = AuthorizationClient.getResponse(resultCode, intent)

        return when (response.type) {
            AuthorizationResponse.Type.TOKEN -> response.accessToken
            AuthorizationResponse.Type.ERROR -> null
            else -> null
        }
    }

    fun fetchUserTopArtists(token: String, callback: (String?) -> Unit) {
        val client = OkHttpClient()
        val url = "https://api.spotify.com/v1/me/top/artists?time_range=long_term&limit=50"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonBody = response.body?.string()
                    callback(jsonBody)
                } else {
                    callback(null)
                }
            }
        })
    }

    companion object {
        const val REQUEST_CODE = 1337
    }
}