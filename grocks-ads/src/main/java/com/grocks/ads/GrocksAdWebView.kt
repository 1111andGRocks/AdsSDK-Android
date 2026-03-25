package com.grocks.ads

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun GrocksAdWebView(
    startUri: Uri,
    onLoadingStateChange: (Boolean) -> Unit,
    onMainFrameNavigation: (Uri?) -> Unit,
    onMailOrTel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.setSupportMultipleWindows(true)
                settings.javaScriptCanOpenWindowsAutomatically = true
                isNestedScrollingEnabled = true

                webViewClient =
                    object : WebViewClient() {
                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            if (view == null || url == null) return false
                            return handleUrl(view, Uri.parse(url), isForMainFrame = true)
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest,
                        ): Boolean {
                            val isMainFrame =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    request.isForMainFrame
                                } else {
                                    true
                                }
                            return handleUrl(view, request.url, isMainFrame)
                        }

                        private fun handleUrl(
                            view: WebView,
                            url: Uri,
                            isForMainFrame: Boolean,
                        ): Boolean {
                            val scheme = url.scheme?.lowercase()
                            val isHttpFamily = scheme == "http" || scheme == "https"

                            if (scheme == "mailto" || scheme == "tel" || scheme == "telprompt") {
                                onMailOrTel()
                                return false
                            }

                            if (isForMainFrame) {
                                onLoadingStateChange(true)
                                onMainFrameNavigation(url)
                                return false
                            }

                            if (isHttpFamily) {
                                onLoadingStateChange(true)
                                onMainFrameNavigation(url)
                                view.loadUrl(url.toString())
                                return true
                            }

                            return false
                        }

                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: Bitmap?,
                        ) {
                            super.onPageStarted(view, url, favicon)
                            onLoadingStateChange(true)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            onLoadingStateChange(false)
                        }

                        override fun onReceivedError(
                            view: WebView,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?,
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            onLoadingStateChange(false)
                        }
                    }

                webChromeClient =
                    object : WebChromeClient() {
                        override fun onCloseWindow(window: WebView?) {
                            super.onCloseWindow(window)
                        }
                    }

                loadUrl(startUri.toString())
            }
        },
        modifier = modifier,
    )
}
