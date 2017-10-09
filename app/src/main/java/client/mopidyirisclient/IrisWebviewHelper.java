package client.mopidyirisclient;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by jiri on 10.10.17.
 */

class IrisWebviewHelper {
    private static MainActivity activity;

    public static MainActivity getActivity() {
        return activity;
    }

    public static void setActivity(MainActivity activity) {
        IrisWebviewHelper.activity = activity;
    }

    public static void setWebSettings(WebView web) {
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setSupportMultipleWindows(true);
        ws.setAppCacheEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
    }

    public static void setupUrl() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        activity.setIrisUrl(pref.getString("settings_iris_url", ""));
        activity.setIrisHost(Uri.parse(activity.getIrisUrl()).getHost());
    }
    public static void loadIrisUrl(String url) {
        IrisUrlChecker checker = new IrisUrlChecker(activity);
        checker.execute(url);
    }
    public static void refresh() {
        IrisWebviewHelper.setupUrl();
        loadIrisUrl(activity.getIrisUrl());
    }
}
