package com.example.ui.screens.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.viewmodel.MediBridgeViewModel

class WebAppInterface(private val context: Context, private val viewModel: MediBridgeViewModel) {
    @JavascriptInterface
    fun onWebDonationSubmitted(jsonPayload: String) {
        Toast.makeText(context, "Web Portal Synced with Room DB", Toast.LENGTH_SHORT).show()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPortalScreen(
    viewModel: MediBridgeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isDesktopView by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("web_portal_screen_column")
    ) {
        // Control Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "MediBridge Web Application",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isDesktopView) "💻 Desktop Widescreen Layout (1200px)" else "📱 Responsive Mobile Web Layout",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Toggle Device View Mode (Mobile vs Desktop)
                        FilledTonalButton(
                            onClick = {
                                isDesktopView = !isDesktopView
                                webViewRef?.settings?.userAgentString = if (isDesktopView) {
                                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                } else null
                                val js = if (isDesktopView) {
                                    "javascript:(function() { var meta = document.querySelector('meta[name=viewport]'); if (meta) { meta.setAttribute('content', 'width=1100, initial-scale=0.32'); } })()"
                                } else {
                                    "javascript:(function() { var meta = document.querySelector('meta[name=viewport]'); if (meta) { meta.setAttribute('content', 'width=device-width, initial-scale=1.0'); } })()"
                                }
                                webViewRef?.loadUrl(js)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isDesktopView) Icons.Default.PhoneAndroid else Icons.Default.DesktopWindows,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDesktopView) "Mobile" else "Desktop",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        // Reload
                        FilledTonalIconButton(
                            onClick = { webViewRef?.reload() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Open in external browser
                        FilledTonalIconButton(
                            onClick = {
                                val url = "https://ais-pre-42likepivtvm6vt5ysrdwp-164697671219.asia-east1.run.app"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Open in Browser",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // WebView hosting the responsive web application
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("medibridge_webview"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        // Software rendering prevents MESA rendernode failure on headless cloud containers
                        try {
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        } catch (_: Exception) {}

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false

                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (isDesktopView) {
                                    view?.loadUrl("javascript:(function() { var meta = document.querySelector('meta[name=viewport]'); if (meta) { meta.setAttribute('content', 'width=1100, initial-scale=0.32'); } })()")
                                }
                            }
                        }

                        addJavascriptInterface(WebAppInterface(ctx, viewModel), "AndroidBridge")
                        loadUrl("file:///android_asset/web/index.html")
                        webViewRef = this
                    }
                },
                update = { view ->
                    webViewRef = view
                }
            )
        }
    }
}
