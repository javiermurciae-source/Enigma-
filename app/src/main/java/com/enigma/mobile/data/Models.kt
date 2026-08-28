package com.enigma.mobile.data

import java.util.UUID

data class BrowserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Perfil",
    val os: String = "Android 14",
    val userAgent: String = IdentityFactory.randomUserAgent(),
    val languages: String = IdentityFactory.randomLanguages(),
    val timezone: String = IdentityFactory.randomTimezone(),
    val cores: Int = IdentityFactory.randomCores(),
    val ram: Int = IdentityFactory.randomRam(),
    val screen: String = IdentityFactory.randomScreen(),
    val fpId: String = IdentityFactory.randomFpId(),
    val deviceModel: String = IdentityFactory.randomModel(),
    val webglVendor: String = IdentityFactory.randomGpuVendor(),
    val webglRenderer: String = IdentityFactory.randomGpuRenderer(),
    val chromeMajor: Int = IdentityFactory.randomChromeMajor(),
    val chromeFull: String = "",
    val canvasSeed: Double = IdentityFactory.randomCanvasSeed(),
    val audioSeed: Double = IdentityFactory.randomAudioSeed(),
    val region: String = "mx",
    val proxyId: String? = null,
    val homeUrl: String = UrlRouter.SEARCH_HOME,
)

data class ProxyConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val host: String = "",
    val port: String = "",
    val type: String = "HTTP",
    val username: String = "",
    val password: String = "",
) {
    fun isConfigured() = host.isNotBlank() && port.isNotBlank()
    fun hasAuth() = username.isNotBlank()

    fun label(): String {
        if (!isConfigured()) return "Directo"
        val base = if (name.isBlank()) "$host:$port" else name
        val auth = if (hasAuth()) " 🔐" else ""
        return "$type · $base$auth"
    }

    companion object {
        fun parseLine(raw: String, defaultType: String = "HTTP"): ProxyConfig? {
            val cred = raw.trim()
            if (cred.isBlank()) return null
            val parts = cred.split(":")
            if (parts.size < 2) return null
            val host = parts[0].trim()
            val port = parts[1].trim()
            if (host.isBlank() || port.toIntOrNull() == null) return null
            val user = parts.getOrNull(2) ?: ""
            val pass = parts.drop(3).joinToString(":")
            return ProxyConfig(
                name = "$host:$port",
                host = host,
                port = port,
                type = defaultType,
                username = user,
                password = pass,
            )
        }
    }
}

data class QuickSite(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val url: String,
)

data class HistoryEntry(
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
)