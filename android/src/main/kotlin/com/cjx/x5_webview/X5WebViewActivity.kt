package com.cjx.x5_webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import com.cjx.x5_webview.config.FullscreenHolder
import com.cjx.x5_webview.config.MyJavascriptInterface
import com.cjx.x5_webview.config.WebTools
import com.cjx.x5_webview.tencentx5.IX5WebPageView
import com.cjx.x5_webview.tencentx5.MyX5WebChromeClient
import com.cjx.x5_webview.tencentx5.MyX5WebViewClient
import com.cjx.x5_webview.tencentx5.WebProgress
import com.gyf.immersionbar.ImmersionBar
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView


class X5WebViewActivity : Activity(), IX5WebPageView {
    var webView: WebView? = null
    // 进度条
    private var mProgressBar: WebProgress? = null
    // 全屏时视频加载view
    private var videoFullView: FrameLayout? = null
    // 加载视频相关
    private var mWebChromeClient: MyX5WebChromeClient? = null
    // 网页链接
    private val mUrl: String? = null
    // 可滚动的title 使用简单 没有渐变效果，文字两旁有阴影
    private var mTitleToolBar: View? = null

    override fun setRequestedOrientation(requestedOrientation: Int) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && Utils.isTranslucentOrFloating(this)) {
            return
        }
        super.setRequestedOrientation(requestedOrientation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && Utils.isTranslucentOrFloating(this)) {
            val result: Boolean = Utils.fixOrientation(this)
        }
        setContentView(R.layout.activity_web_view)
        window.setFormat(PixelFormat.TRANSLUCENT)
        ImmersionBar.with(this)
                .transparentStatusBar()  //透明状态栏
                .statusBarDarkFont(true)
                .titleBarMarginTop(R.id.title_tool_bar)
                .init()

        initView()
    }

    private fun initView() {
        webView = WebView(this)
        mTitleToolBar = findViewById<View>(R.id.title_tool_bar)
        findViewById<View>(R.id.btnClose).setOnClickListener {
            finish()
        }
        val rl_web_container: FrameLayout = findViewById(R.id.rl_web_container)
        rl_web_container.addView(webView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        //actionBar?.show()
        //actionBar?.setDisplayHomeAsUpEnabled(true)
        title = intent.getStringExtra("title") ?: ""
        var tvGunTitle = findViewById<TextView>(R.id.tvGunTitle)
        tvGunTitle.postDelayed(Runnable { tvGunTitle.setSelected(true) }, 1900)
        tvGunTitle.setText(title)
        mProgressBar = WebProgress(this)
        mProgressBar?.setVisibility(View.GONE)
        rl_web_container.addView(mProgressBar, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        mProgressBar?.setColor(Color.parseColor("#2AD6E2"))
        mProgressBar?.show()
        webView?.apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

            val ws = webView!!.settings
            // 网页内容的宽度自适应屏幕
            ws.loadWithOverviewMode = true
            ws.useWideViewPort = true
            // 保存表单数据
            ws.saveFormData = true
            // 是否应该支持使用其屏幕缩放控件和手势缩放
            ws.setSupportZoom(true)
            ws.builtInZoomControls = true
            ws.displayZoomControls = false
            // 启动应用缓存
            ws.setAppCacheEnabled(true)
            // 设置缓存模式
            ws.cacheMode = WebSettings.LOAD_DEFAULT
            // setDefaultZoom  api19被弃用
// 设置此属性，可任意比例缩放。
            ws.useWideViewPort = true
            // 告诉WebView启用JavaScript执行。默认的是false。
            ws.javaScriptEnabled = true
            //  页面加载好以后，再放开图片
            ws.blockNetworkImage = false
            // 使用localStorage则必须打开
            ws.domStorageEnabled = true
            // 排版适应屏幕
            ws.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
            // WebView是否新窗口打开(加了后可能打不开网页)
            ws.setSupportMultipleWindows(true)
            // webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。MIXED_CONTENT_ALWAYS_ALLOW
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ws.setMixedContentMode(WebSettings.LOAD_NORMAL)
            }
            /** 设置字体默认缩放大小(改变网页字体大小,setTextSize  api14被弃用)*/
            /** 设置字体默认缩放大小(改变网页字体大小,setTextSize  api14被弃用) */
            ws.setTextZoom(100)

            mWebChromeClient = MyX5WebChromeClient(this@X5WebViewActivity)
            webChromeClient = mWebChromeClient
            // 与js交互
            addJavascriptInterface(MyJavascriptInterface(this@X5WebViewActivity), "injectedObject")
            webViewClient =  MyX5WebViewClient(this@X5WebViewActivity)
            settings.javaScriptEnabled = true
            loadUrl(intent.getStringExtra("url"))
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //全屏播放退出全屏
            when {
                mWebChromeClient?.inCustomView() ?: false -> {
                    hideCustomView()
                    return true
                    //返回网页上一页
                }
                webView?.canGoBack() ?: false -> {
                    webView?.goBack()
                    return true
                    //退出网页
                }
                else -> {
                    handleFinish()
                }
            }
        }
        return false
    }

    fun handleFinish() {
        finish()
    }

    /**
     * 全屏时按返加键执行退出全屏方法
     */
    @SuppressLint("SourceLockedOrientationActivity")
    fun hideCustomView() {
        mWebChromeClient?.onHideCustomView()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        webView?.onResume()
        // 支付宝网页版在打开文章详情之后,无法点击按钮下一步
        webView?.resumeTimers()
        // 设置为横屏
        if (requestedOrientation !== ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onDestroy() {
        if (videoFullView != null) {
            videoFullView?.removeAllViews()
            videoFullView = null
        }
        if (webView != null) {
            val parent = webView?.parent as ViewGroup
            parent?.removeView(webView)
            webView?.removeAllViews()
            webView?.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            webView?.stopLoading()
            webView?.webChromeClient = null
            webView?.webViewClient = null
            webView?.destroy()
            webView = null
        }
        super.onDestroy()
    }

    override fun showWebView() {
        webView?.visibility = View.VISIBLE
    }

    override fun hindWebView() {
        webView?.visibility = View.INVISIBLE
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {

    }

    override fun onPageFinished(view: WebView?, url: String?) {
        mProgressBar?.hide()
    }

    override fun fullViewAddView(view: View?) {
        val decor = window.decorView as FrameLayout
        videoFullView = FullscreenHolder(this)
        videoFullView?.addView(view)
        decor.addView(videoFullView)
    }

    override fun showVideoFullView() {
        videoFullView?.visibility = View.VISIBLE
    }

    override fun hindVideoFullView() {
        videoFullView?.visibility = View.GONE
    }

    override fun startProgress(newProgress: Int) {
        mProgressBar?.setWebProgress(newProgress)
    }


    override fun isOpenThirdApp(url: String?): Boolean {
        return WebTools.handleThirdApp(this, url)
    }


    override fun startFileChooserForResult(intent: Intent?, requestCode: Int) {
        startActivityForResult(intent, requestCode)
    }

    override fun getVideoFullView(): FrameLayout? {
        return videoFullView
    }

    override fun getVideoLoadingProgressView(): View? {
        return LayoutInflater.from(this).inflate(R.layout.video_loading_progress, null)
    }

}