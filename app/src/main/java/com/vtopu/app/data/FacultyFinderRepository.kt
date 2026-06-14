package com.vtopu.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class FacultyFinderRepository {
    private val client = OkHttpClient()

    suspend fun searchFaculty(query: String): Result<List<FacultyProfile>> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isBlank()) {
                return@runCatching emptyList()
            }

            val urlBuilder = FACULTY_SUPABASE_URL.toHttpUrl()
                .newBuilder()
                .addPathSegments("rest/v1/teacher")
                .addQueryParameter("select", "*")
                .addQueryParameter("order", "name.asc")
                .addQueryParameter("name", "ilike.*$trimmedQuery*")

            val request = Request.Builder()
                .url(urlBuilder.build())
                .addHeader("apikey", FACULTY_SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer $FACULTY_SUPABASE_ANON_KEY")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Faculty lookup failed with HTTP ${response.code}." }
                val body = response.body?.string().orEmpty()
                val array = JSONArray(body)
                List(array.length()) { index ->
                    val item = array.getJSONObject(index)
                    FacultyProfile(
                        id = item.optString("id", item.optString("name", index.toString())),
                        name = item.optString("name", "Unknown faculty"),
                        cabinNumber = item.optNullableString("cabin_no"),
                        mobileNumber = item.optNullableString("mobile_no")
                    )
                }
            }
        }
    }

    private fun org.json.JSONObject.optNullableString(name: String): String? =
        optString(name).trim().takeIf { it.isNotBlank() && it != "null" }

    private companion object {
        const val FACULTY_SUPABASE_URL = "https://ncbdsoiifaeoaqkjvolx.supabase.co"
        const val FACULTY_SUPABASE_ANON_KEY =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5jYmRzb2lpZmFlb2Fxa2p2b2x4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTIwNzQ5NzcsImV4cCI6MjA2NzY1MDk3N30.urn5uTezwlR0fWZWYvLmWamOCrhaIBomrNcnxFAT78k"
    }
}
