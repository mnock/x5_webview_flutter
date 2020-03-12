package com.cjx.x5_webview
import android.app.Activity
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.gyf.immersionbar.ImmersionBar
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient


class X5WebViewActivity : Activity() {
    var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        ImmersionBar.with(this)
                .transparentStatusBar()  //透明状态栏
                .fitsSystemWindows(true)
                .init()

        initView()
    }

    private fun initView() {
        webView = WebView(this)
        val rl_web_container: RelativeLayout = findViewById(R.id.rl_web_container)
        rl_web_container.addView(webView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        actionBar?.show()
        actionBar?.setDisplayHomeAsUpEnabled(true)
        title = intent.getStringExtra("title") ?: ""
        var tvGunTitle=findViewById<TextView>(R.id.tvGunTitle)
        tvGunTitle.postDelayed(Runnable { tvGunTitle.setSelected(true) }, 1900)
        tvGunTitle.setText(title)
        webView?.apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            loadUrl(intent.getStringExtra("url"))
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                    view.loadUrl(url)
                    return super.shouldOverrideUrlLoading(view, url)
                }

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
                    view.loadUrl(request?.url.toString())
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

}