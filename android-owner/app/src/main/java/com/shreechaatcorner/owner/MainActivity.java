package com.shreechaatcorner.owner;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.webkit.*;
import android.content.Intent;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_PICKER = 1001;
    private static final int NOTIFICATION_PERMISSION = 1002;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermissionIfNeeded();
        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        FirebaseMessaging.getInstance().subscribeToTopic("shree_chaat_owner_orders")
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) android.util.Log.w("SCCOwner", "Could not subscribe to order alerts", task.getException());
            });
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setLoadsImagesAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = params.createIntent();
                try { startActivityForResult(intent, FILE_PICKER); }
                catch (Exception e) { fileCallback = null; return false; }
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://shreechaatcorner.github.io/shree-chaat-corner/owner.html");
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER && fileCallback != null) {
            Uri[] results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fileCallback.onReceiveValue(results);
            fileCallback = null;
        }
    }
    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
