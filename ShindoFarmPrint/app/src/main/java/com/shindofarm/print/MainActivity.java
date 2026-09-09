package com.shindofarm.print;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ShindoFarmPrint";
    private WebView webView;
    private String webAppUrl;
    private String btPrintPackage;
    private String btPrintClass;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load config dari strings.xml
        webAppUrl = getString(R.string.web_app_url);
        btPrintPackage = getString(R.string.bluetooth_print_package);
        btPrintClass = getString(R.string.bluetooth_print_class);

        webView = findViewById(R.id.webView);
        setupWebView();

        // Load URL
        webView.loadUrl(webAppUrl);
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Enable cookies untuk session Laravel
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        // Inject JavascriptInterface untuk print
        webView.addJavascriptInterface(new PrintBridge(), "AndroidPrint");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Keep navigation inside WebView
                if (url.startsWith("http")) {
                    view.loadUrl(url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Inject print function ke halaman web setelah load selesai
                injectPrintFunction();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "WebView error: " + description);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
    }

    private void injectPrintFunction() {
        // Inject JS yang akan dipanggil dari web: window.AndroidPrint.printNota(htmlContent)
        String js = "javascript:(function() {" +
            "if (window.AndroidPrint) { return; }" +
            "window.AndroidPrint = { " +
            "  printNota: function(content) { " +
            "    if (typeof AndroidPrint !== 'undefined' && AndroidPrint.cetakNota) { " +
            "      AndroidPrint.cetakNota(content); " +
            "    } " +
            "  }" +
            "};" +
            "})()";
        webView.evaluateJavascript(js, null);
    }

    // Bridge class yang dipanggil dari JavaScript
    public class PrintBridge {
        @JavascriptInterface
        public void cetakNota(String content) {
            Log.d(TAG, "cetakNota dipanggil dari web");
            runOnUiThread(() -> {
                if (isBluetoothPrintInstalled()) {
                    sendPrintIntent(content);
                } else {
                    showInstallDialog();
                }
            });
        }
    }

    private boolean isBluetoothPrintInstalled() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(btPrintPackage, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void sendPrintIntent(String content) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(btPrintPackage, btPrintClass));
            intent.putExtra("content", content);
            // Optional: MAC address printer untuk direct print (isi jika perlu)
            // intent.putExtra("device_address", "0F:02:18:B0:53:AA");
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error kirim intent print", e);
            runOnUiThread(() -> Toast.makeText(this, "Gagal cetak: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void showInstallDialog() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Aplikasi Print Tidak Ditemukan")
            .setMessage("Butuh aplikasi 'Bluetooth Print' oleh iyaltamizh untuk cetak ke printer thermal. Install dari Play Store?")
            .setPositiveButton("Install", (dialog, which) -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + btPrintPackage)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + btPrintPackage)));
                }
            })
            .setNegativeButton("Batal", null)
            .show();
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