package com.admail1.temp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private static final String APP_URL = "https://admail1.base44.app";
    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> webView.reload());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Enable Service Workers and offline caching
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.webkit.ServiceWorkerController swController =
                android.webkit.ServiceWorkerController.getInstance();
            swController.setServiceWorkerClient(new android.webkit.ServiceWorkerClient() {
                @Override
                public android.webkit.WebResourceResponse shouldInterceptRequest(
                        android.webkit.WebResourceRequest request) {
                    return null; // allow default SW fetch handling
                }
            });
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return false;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefresh.setRefreshing(false);
            }
            @Override
            public void onReceivedError(WebView view, android.webkit.WebResourceRequest request,
                    android.webkit.WebResourceError error) {
                if (request.isForMainFrame()) {
                    view.loadUrl("file:///android_asset/offline.html");
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            String startUrl = resolveDeepLinkUrl(getIntent());
            webView.loadUrl(startUrl);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String url = resolveDeepLinkUrl(intent);
        webView.loadUrl(url);
    }

    /**
     * If the activity was launched via a deep link, load that URL directly.
     * For custom-scheme links (myapp://) we map them to the web URL.
     * Falls back to APP_URL for normal launches.
     */
    private String resolveDeepLinkUrl(Intent intent) {
        if (intent == null) return APP_URL;
        Uri data = intent.getData();
        if (data == null) return APP_URL;
        String scheme = data.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            // HTTP/HTTPS deep link — load the exact URL
            return data.toString();
        }
        return APP_URL;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
