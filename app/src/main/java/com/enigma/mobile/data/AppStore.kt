package com.enigma.mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("enigma_store", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveProfiles(list: List<BrowserProfile>) = prefs.edit().putString("profiles", gson.toJson(list)).apply()
    fun loadProfiles(): List<BrowserProfile> = gson.fromJson(prefs.getString("profiles", null), object : TypeToken<List<BrowserProfile>>() {}.type) ?: emptyList()

    fun saveProxies(list: List<ProxyConfig>) = prefs.edit().putString("proxies", gson.toJson(list)).apply()
    fun loadProxies(): List<ProxyConfig> = gson.fromJson(prefs.getString("proxies", null), object : TypeToken<List<ProxyConfig>>() {}.type) ?: emptyList()

    fun saveBookmarks(list: List<QuickSite>) = prefs.edit().putString("bookmarks", gson.toJson(list)).apply()
    fun loadBookmarks(): List<QuickSite> = gson.fromJson(prefs.getString("bookmarks", null), object : TypeToken<List<QuickSite>>() {}.type) ?: emptyList()

    fun saveHistory(list: List<HistoryEntry>) = prefs.edit().putString("history", gson.toJson(list)).apply()
    fun loadHistory(): List<HistoryEntry> = gson.fromJson(prefs.getString("history", null), object : TypeToken<List<HistoryEntry>>() {}.type) ?: emptyList()

    fun getActiveProfileId(): String? = prefs.getString("active_profile", null)
    fun setActiveProfileId(id: String) = prefs.edit().putString("active_profile", id).apply()

    fun wipeDisposableKeepBookmarks(): List<QuickSite> {
        val bookmarks = loadBookmarks()
        prefs.edit().remove("profiles").remove("proxies").remove("history").apply()
        return bookmarks
    }
}