package com.vtopu.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.vtopu.app.data.VtopRepository

class PortalActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private val repository by lazy { VtopRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Full VTOP"

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            visibility = View.VISIBLE
        }
        webView = WebView(this).apply {
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    progressBar.progress = newProgress
                    progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                    view.activateShellIfNeeded()
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError
                ) {
                    super.onReceivedError(view, request, error)
                    if (request.isForMainFrame) {
                        Toast.makeText(
                            this@PortalActivity,
                            error.description?.toString() ?: "VTOP could not be loaded.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(false)
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            settings.userAgentString = VTOP_DESKTOP_USER_AGENT
        }

        setContentView(buildContentView())
        webView.syncVtopCookiesAndLoad(repository.portalStartUrl(), repository.cookiesForWebView())
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
        }
        super.onDestroy()
    }

    private fun buildContentView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(5, 5, 5))
        }
        root.addView(buildTopBar())
        root.addView(progressBar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            4.dp()
        ))
        root.addView(webView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))
        return root
    }

    private fun buildTopBar(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(14.dp(), 8.dp(), 8.dp(), 8.dp())
            setBackgroundColor(Color.rgb(9, 9, 9))
        }
        val title = TextView(this).apply {
            text = "gamma VTOP"
            setTextColor(Color.WHITE)
            textSize = 19f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        bar.addView(title, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        bar.addView(topButton("Home") {
            webView.syncVtopCookiesAndLoad(repository.portalHomeUrl(), repository.cookiesForWebView())
        })
        bar.addView(topButton("Reload") {
            webView.syncVtopCookies(repository.cookiesForWebView())
            webView.reload()
        })
        bar.addView(topButton("Chrome") {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repository.portalHomeUrl())))
        })
        bar.addView(topButton("Close") { finish() })
        return bar
    }

    private fun topButton(label: String, onClick: () -> Unit): View =
        TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(10.dp(), 8.dp(), 10.dp(), 8.dp())
            setOnClickListener { onClick() }
        }

    private fun WebView.syncVtopCookiesAndLoad(url: String, cookies: List<String>) {
        syncVtopCookies(cookies)
        loadUrl(url)
    }

    private fun WebView.syncVtopCookies(cookies: List<String>) {
        val manager = CookieManager.getInstance()
        manager.setAcceptCookie(true)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            manager.setAcceptThirdPartyCookies(this, true)
        }
        cookies.forEach { cookie ->
            manager.setCookie(VTOP_BASE_URL, cookie)
            manager.setCookie("$VTOP_BASE_URL/vtop", cookie)
        }
        manager.flush()
    }

    private fun WebView.activateShellIfNeeded() {
        evaluateJavascript(
            """
                (function() {
                  var text = (document.body && document.body.innerText || '').trim();
                  if (text.length > 80) return;
                  var candidates = Array.from(document.querySelectorAll('a,button,[onclick]'));
                  var target = candidates.find(function(el) {
                    var value = ((el.innerText || '') + ' ' + (el.getAttribute('title') || '') + ' ' + (el.getAttribute('onclick') || '')).toLowerCase();
                    return value.includes('home') || value.includes('content') || value.includes('menu');
                  });
                  if (target) target.click();
                })();
            """.trimIndent(),
            null
        )
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, PortalActivity::class.java)
        private const val VTOP_BASE_URL = "https://vtop.vitbhopal.ac.in"
        private const val VTOP_DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0 Safari/537.36"
    }
}
