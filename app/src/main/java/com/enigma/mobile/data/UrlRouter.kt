package com.enigma.mobile.data

object UrlRouter {
    const val SEARCH_HOME = "https://www.google.com"

    fun resolve(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return SEARCH_HOME
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        return "https://$trimmed"
    }
}