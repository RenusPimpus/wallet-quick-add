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
        requestAllPages("/v1/api/accounts", token, listOf("accounts", "data", "items"))
            .mapNotNull { item ->
                val id = item.optString("id").ifBlank { item.optString("_id") }
                val name = item.optString("name").ifBlank { item.optString("title") }
                if (id.isNotBlank() && name.isNotBlank()) {
                    WalletAccount(id, name, readCurrency(item))
                } else {
                    null
                }
            }
            .distinctBy { it.id }
    }

    suspend fun getCategories(token: String): List<WalletCategory> = withContext(Dispatchers.IO) {
        buildList {
            requestAllPages("/v1/api/categories", token, listOf("categories", "data", "items"))
                .forEach { flattenCategory(it, this) }
        }.distinctBy { it.id }
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

    private fun requestAllPages(
        path: String,
        token: String,
        candidateKeys: List<String>
    ): List<JSONObject> {
        val results = mutableListOf<JSONObject>()
        val visitedOffsets = mutableSetOf<Int>()
        var offset = 0

        repeat(MAX_PAGES) {
            if (!visitedOffsets.add(offset)) return results

            val payload = request("$path?limit=$PAGE_LIMIT&offset=$offset", token)
            val root = if (payload.isBlank()) JSONArray() else JSONTokener(payload).nextValue()
            val page = extractArray(root, candidateKeys)

            for (index in 0 until page.length()) {
                page.optJSONObject(index)?.let(results::add)
            }

            val reportedNextOffset = readNextOffset(root)
            val nextOffset = when {
                reportedNextOffset != null && reportedNextOffset > offset -> reportedNextOffset
                page.length() == PAGE_LIMIT -> offset + page.length()
                else -> null
            }
            offset = nextOffset ?: return results
        }

        throw IOException("Wallet API zwróciło zbyt wiele stron danych.")
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

    private fun extractArray(root: Any?, candidateKeys: List<String>): JSONArray =
        when (root) {
            is JSONArray -> root
            is JSONObject -> candidateKeys.firstNotNullOfOrNull { root.optJSONArray(it) } ?: JSONArray()
            else -> JSONArray()
        }

    private fun readNextOffset(root: Any?): Int? {
        if (root !is JSONObject) return null
        val candidates = listOfNotNull(
            root.opt("nextOffset").takeUnless { it == JSONObject.NULL },
            root.optJSONObject("pagination")?.opt("nextOffset")?.takeUnless { it == JSONObject.NULL },
            root.optJSONObject("meta")?.opt("nextOffset")?.takeUnless { it == JSONObject.NULL }
        )
        return candidates.firstNotNullOfOrNull { value ->
            when (value) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            }
        }
    }

    private fun flattenCategory(item: JSONObject, output: MutableList<WalletCategory>) {
        val id = item.optString("id").ifBlank { item.optString("_id") }
        val name = item.optString("name").ifBlank { item.optString("title") }
        if (id.isNotBlank() && name.isNotBlank()) output += WalletCategory(id, name)
        listOf("children", "subcategories", "categories").forEach { key ->
            item.optJSONArray(key)?.let { children ->
                for (index in 0 until children.length()) {
                    children.optJSONObject(index)?.let { flattenCategory(it, output) }
                }
            }
        }
    }

    private fun readCurrency(item: JSONObject): String {
        val direct = item.optString("currency")
        if (direct.isNotBlank()) return direct
        return item.optJSONObject("currency")?.optString("code")
            .orEmpty()
            .ifBlank { item.optJSONObject("balance")?.optString("currencyCode").orEmpty() }
    }

    private companion object {
        const val BASE_URL = "https://rest.budgetbakers.com/wallet"
        const val PAGE_LIMIT = 200
        const val MAX_PAGES = 100
    }
}
