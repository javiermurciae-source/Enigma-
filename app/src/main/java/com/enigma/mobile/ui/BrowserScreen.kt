package com.enigma.mobile.ui

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(vm: BrowserViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                        Icon(Icons.Default.Menu, "Menu")
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

    // Menu Sheet
    if (state.sheet is Sheet.Menu) {
        ModalBottomSheet(
            onDismissRequest = { vm.closeSheet() },
            sheetState = bottomSheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Menu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                ListItem(
                    headlineContent = { Text("Perfiles") },
                    leadingContent = { Icon(Icons.Default.Person, null) },
                    supportingContent = { Text("${state.profiles.size} perfiles activos") },
                    modifier = Modifier.clickable { vm.openSheet(Sheet.Profiles) }
                )

                ListItem(
                    headlineContent = { Text("Proxies") },
                    leadingContent = { Icon(Icons.Default.Settings, null) },
                    supportingContent = { Text("${state.proxies.size} configurados") },
                    modifier = Modifier.clickable { vm.openSheet(Sheet.Proxies) }
                )

                ListItem(
                    headlineContent = { Text("Favoritos") },
                    leadingContent = { Icon(Icons.Default.Star, null) },
                    supportingContent = { Text("${state.bookmarks.size} guardados") },
                    modifier = Modifier.clickable { vm.openSheet(Sheet.Bookmarks) }
                )

                ListItem(
                    headlineContent = { Text("Renovar Fingerprint") },
                    leadingContent = { Icon(Icons.Default.Refresh, null) },
                    supportingContent = { Text("Generar nueva identidad") },
                    modifier = Modifier.clickable {
                        vm.renewFingerprint()
                        vm.closeSheet()
                    }
                )

                state.publicIp?.let { ip ->
                    ListItem(
                        headlineContent = { Text("IP publica") },
                        leadingContent = { Icon(Icons.Default.Info, null) },
                        supportingContent = { Text(ip) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Profiles Sheet
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
                                Text("${profile.deviceModel} · ${profile.region.uppercase()}")
                            },
                            leadingContent = {
                                Icon(
                                    if (isActive) Icons.Default.CheckCircle else Icons.Default.Star,
                                    null,
                                    tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingContent = {
                                if (isActive) {
                                    IconButton(onClick = {
                                        vm.deleteProfile(profile.id)
                                        vm.closeSheet()
                                    }) {
                                        Icon(Icons.Default.Delete, "Eliminar")
                                    }
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

    // Proxies Sheet
    if (state.sheet is Sheet.Proxies) {
        var proxyInput by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { vm.closeSheet() },
            sheetState = bottomSheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Proxies", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = proxyInput,
                    onValueChange = { proxyInput = it },
                    label = { Text("host:port:user:pass") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (proxyInput.isNotBlank()) {
                            val parts = proxyInput.split(":")
                            val proxy = com.enigma.mobile.data.ProxyConfig(
                                name = parts.take(2).joinToString(":"),
                                host = parts.first(),
                                port = parts.getOrNull(1) ?: "",
                                type = "HTTP",
                                username = parts.getOrNull(2) ?: "",
                                password = parts.drop(3).joinToString(":"),
                            )
                            vm.addProxy(proxy)
                            proxyInput = ""
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Agregar")
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

    // Bookmarks Sheet
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

    // Settings Sheet
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    state.toast?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2000)
            vm.consumeToast()
        }
    }
}
