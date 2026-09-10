package com.shindofarm.print;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
    private static final String BT_PRINT_PACKAGE = "com.iyaltamizh.bluetoothprint";

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // Jembatan JavaScript <-> Android untuk fitur print
        webView.addJavascriptInterface(new AndroidPrintBridge(this), "AndroidPrint");

        String url = getString(R.string.web_app_url);
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Class jembatan yang dipanggil dari JavaScript di halaman web
     * lewat: AndroidPrint.cetakNota(teksAtauHtml)
     */
    public static class AndroidPrintBridge {

        private final Context context;

        AndroidPrintBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void cetakNota(String content) {
            Log.d(TAG, "cetakNota dipanggil, panjang konten: " + content.length());

            if (!isBluetoothPrintInstalled()) {
                showToast("Aplikasi Bluetooth Print belum terinstall, mengarahkan ke Play Store...");
                openPlayStore();
                return;
            }

            try {
                sendToBluetoothPrint(content);
            } catch (Exception e) {
                Log.e(TAG, "Gagal mengirim ke Bluetooth Print", e);
                showToast("Gagal mencetak: " + e.getMessage());
            }
        }

        private boolean isBluetoothPrintInstalled() {
            PackageManager pm = context.getPackageManager();
            try {
                pm.getPackageInfo(BT_PRINT_PACKAGE, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        private void sendToBluetoothPrint(String content) {
            // Kirim teks nota ke aplikasi Bluetooth Print via Intent SEND
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage(BT_PRINT_PACKAGE);
            intent.putExtra(Intent.EXTRA_TEXT, content);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Bluetooth Print tidak dapat dibuka", e);
                showToast("Gagal membuka aplikasi Bluetooth Print");
                openPlayStore();
            }
        }

        private void openPlayStore() {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + BT_PRINT_PACKAGE));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + BT_PRINT_PACKAGE));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

        private void showToast(final String message) {
            if (context instanceof AppCompatActivity) {
                ((AppCompatActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show());
            }
        }
    }
}
