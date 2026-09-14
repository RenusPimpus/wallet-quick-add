package com.polimorph.walletquickadd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

class WalletApi {
    suspend fun getAccounts(token: String): List<WalletAccount> = withContext(Dispatchers.IO) {
        val payload = request("/v1/api/accounts", token)
        extractArray(payload, listOf("accounts", "data", "items")).mapNotNull { value ->
            val item = value as? JSONObject ?: return@mapNotNull null
            val id = item.optString("id").ifBlank { item.optString("_id") }
            val name = item.optString("name").ifBlank { item.optString("title") }
            if (id.isBlank() || name.isBlank()) null else WalletAccount(id, name, readCurrency(item))
        }
    }

    suspend fun getCategories(token: String): List<WalletCategory> = withContext(Dispatchers.IO) {
        val payload = request("/v1/api/categories", token)
        buildList { flattenCategories(extractArray(payload, listOf("categories", "data", "items")), this) }
            .distinctBy { it.id }
    }

    suspend fun createExpense(token: String, accountId: String, categoryId: String, amount: BigDecimal, note: String) =
        withContext(Dispatchers.IO) {
            val record = JSONObject()
                .put("accountId", accountId)
                .put("categoryId", categoryId)
                .put("amount", JSONObject().put("value", amount.abs().negate()))
                .put("recordDate", Instant.now().toString())
            if (note.isNotBlank()) record.put("note", note.trim())
            request("/v1/api/records", token, "POST", JSONArray().put(record).toString())
        }

    private fun request(path: String, token: String, method: String = "GET", body: String? = null): String {
        val connection = URL(BASE_URL + path).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Authorization", "Bearer ${token.trim()}")
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                throw IOException("Wallet API zwróciło HTTP $status${response.take(300).let { if (it.isBlank()) "" else ": $it" }}")
            }
            response
        } finally {
            connection.disconnect()
        }
    }

    private fun extractArray(payload: String, candidateKeys: List<String>): JSONArray {
        if (payload.isBlank()) return JSONArray()
        return when (val root = JSONTokener(payload).nextValue()) {
            is JSONArray -> root
            is JSONObject -> candidateKeys.firstNotNullOfOrNull { root.optJSONArray(it) } ?: JSONArray()
            else -> JSONArray()
        }
    }

    private fun flattenCategories(array: JSONArray, output: MutableList<WalletCategory>) {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id").ifBlank { item.optString("_id") }
            val name = item.optString("name").ifBlank { item.optString("title") }
            if (id.isNotBlank() && name.isNotBlank()) output += WalletCategory(id, name)
            listOf("children", "subcategories", "categories").forEach { key ->
                item.optJSONArray(key)?.let { flattenCategories(it, output) }
            }
        }
    }

    private fun readCurrency(item: JSONObject): String {
        val direct = item.optString("currency")
        if (direct.isNotBlank()) return direct
        return item.optJSONObject("currency")?.optString("code").orEmpty()
    }

    private companion object {
        const val BASE_URL = "https://rest.budgetbakers.com/wallet"
    }
}
