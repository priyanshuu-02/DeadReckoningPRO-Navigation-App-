package nisargpatel.deadreckoning.util

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

data class PlaceSearchResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)

/** Small, dependency-free OpenStreetMap geocoding client for destination search. */
object NominatimPlaceSearch {
    private const val TAG = "PlaceSearch"
    private const val ENDPOINT = "https://nominatim.openstreetmap.org/search"

    fun search(query: String): List<PlaceSearchResult> {
        if (query.trim().length < 3) return emptyList()
        val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
        return runCatching { searchNominatim(encodedQuery) }
            .onFailure { Log.w(TAG, "Nominatim search failed", it) }
            .getOrDefault(emptyList())
            .ifEmpty {
                runCatching { searchPhoton(encodedQuery) }
                    .onFailure { Log.e(TAG, "Photon fallback search failed", it) }
                    .getOrDefault(emptyList())
            }
    }

    private fun searchNominatim(encodedQuery: String): List<PlaceSearchResult> {
        val connection = (URL("$ENDPOINT?format=jsonv2&limit=6&q=$encodedQuery").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("User-Agent", "MARK-V-Dead-Reckoning/1.0 (Android destination search)")
            setRequestProperty("Accept-Language", "en")
        }
        return try {
            if (connection.responseCode !in 200..299) return emptyList()
            val results = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            buildList {
                for (index in 0 until results.length()) {
                    val item = results.getJSONObject(index)
                    val latitude = item.optString("lat").toDoubleOrNull() ?: continue
                    val longitude = item.optString("lon").toDoubleOrNull() ?: continue
                    add(PlaceSearchResult(item.optString("display_name"), latitude, longitude))
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun searchPhoton(encodedQuery: String): List<PlaceSearchResult> {
        val connection = (URL("https://photon.komoot.io/api/?limit=6&q=$encodedQuery").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("User-Agent", "MARK-V-Dead-Reckoning/1.0 (Android destination search)")
        }
        return try {
            if (connection.responseCode !in 200..299) return emptyList()
            val features = JSONObject(connection.inputStream.bufferedReader().use { it.readText() }).getJSONArray("features")
            buildList {
                for (index in 0 until features.length()) {
                    val feature = features.getJSONObject(index)
                    val coordinates = feature.getJSONObject("geometry").getJSONArray("coordinates")
                    val longitude = coordinates.optDouble(0, Double.NaN)
                    val latitude = coordinates.optDouble(1, Double.NaN)
                    if (!latitude.isFinite() || !longitude.isFinite()) continue
                    val properties = feature.getJSONObject("properties")
                    val name = listOf("name", "street", "city", "state", "country")
                        .mapNotNull { key -> properties.optString(key).takeIf(String::isNotBlank) }
                        .joinToString(", ")
                        .ifBlank { "Selected destination" }
                    add(PlaceSearchResult(name, latitude, longitude))
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
