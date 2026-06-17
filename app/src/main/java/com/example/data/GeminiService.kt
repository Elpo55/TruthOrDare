package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateChallenge(
        type: String, // "ACTION" or "VERITE"
        level: String, // "SOFT", "NORMAL", "CHAOS"
        activePlayer: String,
        otherPlayer: String?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is missing or default. Falling back to offline list.")
            return@withContext ""
        }

        val prompt = """
            Tu es le maître du jeu déjanté de la soirée dans une partie d'Action ou Vérité.
            Génère un défi amusant et original en FRANÇAIS.
            - Type de défi : ${if (type == "ACTION") "ACTION" else "VÉRITÉ"}
            - Niveau : $level (SOFT: bienveillant et drôle, NORMAL: un peu embarrassant et cocasse, CHAOS: absurde, fou d'improvisation et hilarant!)
            - Joueur principal : $activePlayer
            ${otherPlayer?.let { "- Autre joueur à faire participer si nécessaire : $it" } ?: ""}
            
            Règles strictes :
            1. Reste sécurisé, sain et poli. ÉVITE TOUT contenu sexuel explicite, humiliant, dangereux ou violent.
            2. Intègre directement le nom de $activePlayer dans le texte du défi d'une façon naturelle et drôle.
            3. Si tu fais intervenir un autre joueur, utilise le nom de $otherPlayer.
            4. Retourne UNIQUEMENT la phrase de défi rédigée en français directement, sans préambule ni guillemets d'introduction.
        """.trimIndent()

        try {
            // Construct raw JSON body for simplicity and foolproof operation
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed with code: ${response.code}")
                    return@withContext ""
                }

                val bodyStr = response.body?.string() ?: return@withContext ""
                val responseJson = JSONObject(bodyStr)
                val text = responseJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                return@withContext text.trim().removeSurrounding("\"")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            return@withContext ""
        }
    }
}
