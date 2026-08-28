package com.enigma.mobile.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(vm: BrowserViewModel = viewModel()) {
    val state by vm.state.collectAsState()

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

    // Toast
    state.toast?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2000)
            vm.consumeToast()
        }
    }
}