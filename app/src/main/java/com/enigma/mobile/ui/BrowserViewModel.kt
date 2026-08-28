package com.enigma.mobile.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.enigma.mobile.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UiState(
    val profiles: List<BrowserProfile> = listOf(BrowserProfile()),
    val proxies: List<ProxyConfig> = emptyList(),
    val bookmarks: List<QuickSite> = emptyList(),
    val activeProfileId: String? = null,
    val history: List<HistoryEntry> = emptyList(),
    val urlBarText: String = UrlRouter.SEARCH_HOME,
    val currentUrl: String = UrlRouter.SEARCH_HOME,
    val isLoading: Boolean = false,
    val publicIp: String? = null,
    val toast: String? = null,
    val sheet: Sheet = Sheet.None,
)

sealed class Sheet {
    data object None : Sheet()
    data object Menu : Sheet()
    data object Profiles : Sheet()
    data object Proxies : Sheet()
    data object Bookmarks : Sheet()
    data object Settings : Sheet()
}

class BrowserViewModel(app: Application) : AndroidViewModel(app) {
    private val store = AppStore(app)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        boot()
    }

    private fun boot() {
        val bookmarks = store.wipeDisposableKeepBookmarks()
        val profiles = store.loadProfiles().ifEmpty { listOf(BrowserProfile(name = "Perfil 1")) }
        val proxies = store.loadProxies()
        val history = store.loadHistory()
        val activeId = store.getActiveProfileId() ?: profiles.firstOrNull()?.id
        val active = profiles.find { it.id == activeId } ?: profiles.first()

        _state.value = UiState(
            profiles = profiles,
            proxies = proxies,
            bookmarks = bookmarks,
            activeProfileId = activeId,
            history = history,
            urlBarText = active.homeUrl,
            currentUrl = active.homeUrl,
        )
        store.saveProfiles(profiles)
    }

    fun setUrl(text: String) { _state.value = _state.value.copy(urlBarText = text) }
    fun go(url: String) { _state.value = _state.value.copy(currentUrl = url, urlBarText = url) }
    fun openSheet(sheet: Sheet) { _state.value = _state.value.copy(sheet = sheet) }
    fun closeSheet() { _state.value = _state.value.copy(sheet = Sheet.None) }

    fun selectProfile(id: String) {
        _state.value = _state.value.copy(activeProfileId = id)
        store.setActiveProfileId(id)
    }

    fun createProfile(name: String) {
        val profile = BrowserProfile(name = name)
        val updated = _state.value.profiles + profile
        _state.value = _state.value.copy(profiles = updated, activeProfileId = profile.id)
        store.saveProfiles(updated)
        store.setActiveProfileId(profile.id)
    }

    fun deleteProfile(id: String) {
        val updated = _state.value.profiles.filter { it.id != id }
        _state.value = _state.value.copy(profiles = updated)
        store.saveProfiles(updated)
    }

    fun addProxy(proxy: ProxyConfig) {
        val updated = _state.value.proxies + proxy
        _state.value = _state.value.copy(proxies = updated)
        store.saveProxies(updated)
    }

    fun deleteProxy(id: String) {
        val updated = _state.value.proxies.filter { it.id != id }
        _state.value = _state.value.copy(proxies = updated)
        store.saveProxies(updated)
    }

    fun addBookmark(label: String, url: String) {
        val bm = QuickSite(label = label, url = url)
        val updated = _state.value.bookmarks + bm
        _state.value = _state.value.copy(bookmarks = updated)
        store.saveBookmarks(updated)
    }

    fun deleteBookmark(id: String) {
        val updated = _state.value.bookmarks.filter { it.id != id }
        _state.value = _state.value.copy(bookmarks = updated)
        store.saveBookmarks(updated)
    }

    fun showToast(msg: String) { _state.value = _state.value.copy(toast = msg) }
    fun consumeToast() { _state.value = _state.value.copy(toast = null) }

    fun renewFingerprint() {
        val id = _state.value.activeProfileId ?: return
        val profiles = _state.value.profiles.map {
            if (it.id == id) it.copy(
                fpId = IdentityFactory.randomFpId(),
                canvasSeed = IdentityFactory.randomCanvasSeed(),
                audioSeed = IdentityFactory.randomAudioSeed(),
            ) else it
        }
        _state.value = _state.value.copy(profiles = profiles)
        store.saveProfiles(profiles)
        showToast("Fingerprint renovado 🔒")
    }
}