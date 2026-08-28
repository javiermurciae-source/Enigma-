package com.enigma.mobile.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrowserScreen(vm: BrowserViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Main Screen ─────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.urlBarText,
                        onValueChange = { vm.setUrl(it) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar o escribir URL") },
                    )
                },
                actions = {
                    IconButton(onClick = { vm.go(state.urlBarText) }) {
                        Icon(Icons.Default.Search, "Ir")
                    }
                    IconButton(onClick = { vm.openSheet(Sheet.Menu) }) {
                        Icon(Icons.Default.Menu, "Menú")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString = state.profiles.find {
                        it.id == state.activeProfileId
                    }?.userAgent
                    loadUrl(state.currentUrl)
                }
            },
            update = { webView ->
                if (webView.url != state.currentUrl) {
                    webView.loadUrl(state.currentUrl)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }

    // ── Menu Sheet ──────────────────────────────────────────────
    if (state.sheet is Sheet.Menu) {
        ModalBottomSheet(
            onDismissRequest = { vm.closeSheet() },
            sheetState = bottomSheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Menú", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Perfiles
                ListItem(
                    headlineContent = { Text("Perfiles") },
                    leadingContent = { Icon(Icons.Default.Person, null) },
                    supportingContent = { Text("${state.profiles.size} perfiles activos") },
                    modifier = Modifier.clickable { vm.openSheet(Sheet.Profiles) }
                )

                // Proxies
                ListItem(
                    headlineContent = { Text("Proxies") },
                    leadingContent = { Icon(Icons.Default.Language, null) },
                    supportingContent = { Text("${state.proxies.size} configurados") },
                    modifier = Modifier.clickable { vm.openSheet(Sheet.Proxies) }
                )

                // Favoritos
                ListItem(
                    headlineContent = { Text("Favoritos") },
                    leadingContent = { Icon(Icons.Default.Star, null) },
                    supportingContent = { Text("${state.bookmarks.size} guardados") },
                    modifier = Modifier.clickable { vm.openSheet(Sheet.Bookmarks) }
                )

                // Renovar fingerprint
                ListItem(
                    headlineContent = { Text("Renovar Fingerprint") },
                    leadingContent = { Icon(Icons.Default.Refresh, null) },
                    supportingContent = { Text("Generar nueva identidad") },
                    modifier = Modifier.clickable {
                        vm.renewFingerprint()
                        vm.closeSheet()
                    }
                )

                // IP Info
                state.publicIp?.let { ip ->
                    ListItem(
                        headlineContent = { Text("IP pública") },
                        leadingContent = { Icon(Icons.Default.Info, null) },
                        supportingContent = { Text(ip) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Profiles Sheet ──────────────────────────────────────────
    if (state.sheet is Sheet.Profiles) {
        ModalBottomSheet(
            onDismissRequest = { vm.closeSheet() },
            sheetState = bottomSheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Perfiles", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        vm.createProfile("Perfil ${state.profiles.size + 1}")
                        vm.closeSheet()
                    }) {
                        Icon(Icons.Default.Add, "Crear perfil")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(state.profiles) { profile ->
                        val isActive = profile.id == state.activeProfileId
                        ListItem(
                            headlineContent = { Text(profile.name) },
                            supportingContent = {
                                Text("${profile.deviceModel} · ${profile.region.uppercase()} · ${profile.userAgent.take(40)}...")
                            },
                            leadingContent = {
                                Icon(
                                    if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    null,
                                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingContent = {
                                if (isActive) {
                                    Icon(Icons.Default.Delete, "Eliminar",
                                        modifier = Modifier.clickable {
                                            vm.deleteProfile(profile.id)
                                            vm.closeSheet()
                                        })
                                }
                            },
                            modifier = Modifier.clickable {
                                vm.selectProfile(profile.id)
                                vm.closeSheet()
                            }
                        )
                        HorizontalDivider()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Proxies Sheet ───────────────────────────────────────────
    if (state.sheet is Sheet.Proxies) {
        var proxyInput by remember { mutableStateOf("") }
        var proxyType by remember { mutableStateOf("HTTP") }

        ModalBottomSheet(
            onDismissRequest = { vm.closeSheet() },
            sheetState = bottomSheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Proxies", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Input para agregar proxy
                OutlinedTextField(
                    value = proxyInput,
                    onValueChange = { proxyInput = it },
                    label = { Text("host:port:user:pass") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.padding(top = 8.dp)) {
                    FilterChip(
                        selected = proxyType == "HTTP",
                        onClick = { proxyType = "HTTP" },
                        label = { Text("HTTP") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = proxyType == "SOCKS5",
                        onClick = { proxyType = "SOCKS5" },
                        label = { Text("SOCKS5") }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        if (proxyInput.isNotBlank()) {
                            val proxy = com.enigma.mobile.data.ProxyConfig(
                                name = proxyInput.split(":").take(2).joinToString(":"),
                                host = proxyInput.split(":").first(),
                                port = proxyInput.split(":").getOrNull(1) ?: "",
                                type = proxyType,
                                username = proxyInput.split(":").getOrNull(2) ?: "",
                                password = proxyInput.split(":").drop(3).joinToString(":"),
                            )
                            vm.addProxy(proxy)
                            proxyInput = ""
                        }
                    }) {
                        Text("Agregar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn {
                    items(state.proxies) { proxy ->
                        ListItem(
                            headlineContent = { Text(proxy.name.ifBlank { proxy.host }) },
                            supportingContent = { Text("${proxy.type} · ${proxy.host}:${proxy.port}") },
                            trailingContent = {
                                IconButton(onClick = { vm.deleteProxy(proxy.id) }) {
                                    Icon(Icons.Default.Delete, "Eliminar")
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Bookmarks Sheet ─────────────────────────────────────────
    if (state.sheet is Sheet.Bookmarks) {
        var bmLabel by remember { mutableStateOf("") }
        var bmUrl by remember { mutableStateOf(state.currentUrl) }

        ModalBottomSheet(
            onDismissRequest = { vm.closeSheet() },
            sheetState = bottomSheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Favoritos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Input para agregar bookmark
                OutlinedTextField(
                    value = bmLabel,
                    onValueChange = { bmLabel = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bmUrl,
                    onValueChange = { bmUrl = it },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (bmLabel.isNotBlank() && bmUrl.isNotBlank()) {
                            vm.addBookmark(bmLabel, bmUrl)
                            bmLabel = ""
                            bmUrl = state.currentUrl
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Guardar favorito")
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn {
                    items(state.bookmarks) { bm ->
                        ListItem(
                            headlineContent = { Text(bm.label) },
                            supportingContent = { Text(bm.url) },
                            leadingContent = { Icon(Icons.Default.Star, null) },
                            trailingContent = {
                                IconButton(onClick = { vm.deleteBookmark(bm.id) }) {
                                    Icon(Icons.Default.Delete, "Eliminar")
                                }
                            },
                            modifier = Modifier.clickable {
                                vm.go(bm.url)
                                vm.closeSheet()
                            }
                        )
                        HorizontalDivider()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Settings Sheet ──────────────────────────────────────────
    if (state.sheet is Sheet.Settings) {
        ModalBottomSheet(
            onDismissRequest = { vm.closeSheet() },
            sheetState = bottomSheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ajustes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                ListItem(
                    headlineContent = { Text("Fingerprint actual") },
                    supportingContent = { Text(state.profiles.find { it.id == state.activeProfileId }?.fpId ?: "N/A") }
                )

                ListItem(
                    headlineContent = { Text("Modelo") },
                    supportingContent = { Text(state.profiles.find { it.id == state.activeProfileId }?.deviceModel ?: "N/A") }
                )

                ListItem(
                    headlineContent = { Text("Región") },
                    supportingContent = { Text(state.profiles.find { it.id == state.activeProfileId }?.region?.uppercase() ?: "N/A") }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ── Toast ───────────────────────────────────────────────────
    state.toast?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2000)
            vm.consumeToast()
        }
    }
}