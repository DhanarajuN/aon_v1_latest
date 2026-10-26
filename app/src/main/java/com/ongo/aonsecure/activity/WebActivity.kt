package com.ongo.aonsecure.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ongo.aonsecure.R

class WebActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        // Secure: Prevent screenshots (Optional but recommended for policy pages)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.toolbar1)
        window.statusBarColor = resources.getColor(R.color.black)

        val toolbar = findViewById<Toolbar>(R.id.toolbar1)
        val backBtn = toolbar.findViewById<ImageView>(R.id.back_press_iv)
        val title = toolbar.findViewById<TextView>(R.id.title_tv)
        title.setText(R.string.terms)

        backBtn.setOnClickListener {
            finish()
        }

        val url = intent.getStringExtra("URL") ?: ""

        webView = findViewById(R.id.webView)
        secureWebViewSettings(webView)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                val secureUrl = request?.url.toString()
                return if (secureUrl.startsWith("https://")) {
                    false  // allow loading
                } else {
                    true   // block HTTP or unknown schemes
                }
            }
        }

        webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun secureWebViewSettings(webView: WebView) {
        val settings = webView.settings

        // Disable JavaScript unless absolutely required
        settings.javaScriptEnabled = false

        // Disable file access (VERY important)
        settings.allowFileAccess = false
        settings.allowContentAccess = false

        // Disable risky features
        settings.domStorageEnabled = false
        settings.databaseEnabled = false
        settings.javaScriptCanOpenWindowsAutomatically = false

        // Block mixed content (HTTP inside HTTPS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        // Safe Browsing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WebView.enableSlowWholeDocumentDraw()
            WebView.setWebContentsDebuggingEnabled(false)
            settings.safeBrowsingEnabled = true
        }

        // Prevent caching sensitive content
        settings.cacheMode = WebSettings.LOAD_NO_CACHE

        // Disable zoom controls
        settings.displayZoomControls = false
        settings.builtInZoomControls = false

        // Disable saving form data
        settings.saveFormData = false
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
