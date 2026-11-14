package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val notes: String,
    val downloadUrl: String,
)

object UpdateChecker {
    private const val VERSION_JSON_URL =
        "https://raw.githubusercontent.com/Naruto2020-bl/NAVIDAD-/main/apk/version.json"

    private const val FALLBACK_DOWNLOAD_URL =
        "https://github.com/Naruto2020-bl/NAVIDAD-/raw/main/apk/app-debug.apk"

    suspend fun fetchLatestUpdate(currentVersionCode: Int): AppUpdateInfo? =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$VERSION_JSON_URL?t=${System.currentTimeMillis()}")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 6_000
                    readTimeout = 6_000
                    useCaches = false
                }
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext null
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parseUpdateIfNewer(body, currentVersionCode)
            } catch (_: Exception) {
                null
            } finally {
                connection?.disconnect()
            }
        }

    fun parseUpdateIfNewer(jsonBody: String, currentVersionCode: Int): AppUpdateInfo? {
        return try {
            val obj = JSONObject(jsonBody)
            val remoteCode = obj.optInt("versionCode", 0)
            if (remoteCode <= currentVersionCode) return null

            val remoteName = obj.optString("versionName", "Nueva versión")
            val notes = obj.optString(
                "notes",
                "Hay una nueva versión lista para descargar e instalar.",
            )
            val downloadUrl = obj.optString("downloadUrl", FALLBACK_DOWNLOAD_URL)
                .ifBlank { FALLBACK_DOWNLOAD_URL }

            AppUpdateInfo(
                versionCode = remoteCode,
                versionName = remoteName,
                notes = notes,
                downloadUrl = downloadUrl,
            )
        } catch (_: Exception) {
            null
        }
    }
}
