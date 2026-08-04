package com.atra.webapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainActivity : AppCompatActivity() {

    // Change this to your own server address if it ever changes.
    private val siteUrl = "http://154.211.2.75:5000"

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()

        // Exposes AndroidScanner.scanQr() to the web page's JavaScript.
        webView.addJavascriptInterface(ScannerBridge(), "AndroidScanner")

        webView.loadUrl(siteUrl)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    inner class ScannerBridge {
        @JavascriptInterface
        fun scanQr() {
            runOnUiThread {
                IntentIntegrator(this@MainActivity)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                    .setPrompt("QR کد رو بگیر جلوی دوربین")
                    .setBeepEnabled(true)
                    .initiateScan()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val scanned = result.contents.replace("'", "\\'")
            // Calls a JS function the web page defines to handle the scanned code.
            webView.evaluateJavascript("if (window.onNativeQrScanned) { window.onNativeQrScanned('$scanned'); }", null)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
