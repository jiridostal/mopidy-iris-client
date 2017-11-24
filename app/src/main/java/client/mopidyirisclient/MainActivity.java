package client.mopidyirisclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private static final String JAMESBARNSLEY_HOST = "jamesbarnsley.co.nz";
    private static final String SPOTIFY_ACCOUNTS_HOST = "accounts.spotify.com";
    private String irisUrl;
    private String irisHost;
    private WebView web;

    public String getIrisUrl() {
        return irisUrl;
    }

    public void setIrisUrl(String irisUrl) {
        this.irisUrl = irisUrl;
    }

    public String getIrisHost() {
        return irisHost;
    }

    public void setIrisHost(String irisHost) {
        this.irisHost = irisHost;
    }

    public WebView getWeb() {
        return web;
    }

    public void setWeb(WebView web) {
        this.web = web;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public WebView getPopupWebView() {
        return popupWebView;
    }

    public void setPopupWebView(WebView popupWebView) {
        this.popupWebView = popupWebView;
    }

    public FrameLayout getFrameLayout() {
        return frameLayout;
    }

    public void setFrameLayout(FrameLayout frameLayout) {
        this.frameLayout = frameLayout;
    }

    public String getRecentUrlHost() {
        return recentUrlHost;
    }

    public void setRecentUrlHost(String recentUrlHost) {
        this.recentUrlHost = recentUrlHost;
    }

    private Context context;
    private WebView popupWebView;
    private FrameLayout frameLayout;
    private String recentUrlHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContext(this.getApplicationContext());
        setWeb((WebView) findViewById(R.id.webview));
        setFrameLayout((FrameLayout) findViewById(R.id.webview_frame));
        IrisWebviewHelper.setActivity(this);
        // Check and setup provided URL
        IrisWebviewHelper.setupUrl();
        // Set websettings according to our needs
        IrisWebviewHelper.setWebSettings(getWeb());
        // attach View Clients to Web View
        getWeb().setWebViewClient(new UriWebViewClient());
        getWeb().setWebChromeClient(new UriChromeClient());
        // Finally, load up url
        IrisWebviewHelper.loadIrisUrl(getIrisUrl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                getWeb().getContext().startActivity(settingsIntent);
                break;
            case R.id.menu_refresh:
                IrisWebviewHelper.refresh();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * Handle back button properly
     */
    @Override
    public void onBackPressed() {
        if (getWeb().canGoBack()) {
            getWeb().goBack();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * These overrides handle redirects on login pages, including Spotify access confirmation
     */
    class UriWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            String currentUrl = Uri.parse(url).getHost();
            super.onPageStarted(view, url, favicon);
            if (getRecentUrlHost() != null && currentUrl != null) {
                if (getRecentUrlHost().equals(SPOTIFY_ACCOUNTS_HOST) && currentUrl.equals(JAMESBARNSLEY_HOST)) {
                    getPopupWebView().setVisibility(View.GONE);
                }
            }
            setRecentUrlHost(currentUrl);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String host = Uri.parse(url).getHost();
            // If we are at our Iris, dismiss login popup
            if (host.equals(getIrisHost())) {
                if (getPopupWebView() != null) {
                    getPopupWebView().setVisibility(View.GONE);
                    getFrameLayout().removeView(getPopupWebView());
                    setPopupWebView(null);
                }
                return false;
            }
            // Login page requested
            if (host.equals(JAMESBARNSLEY_HOST)) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(intent);
            return true;
        }
    }

    @Override
    protected void onResume() {
        // If URL changed, update it immediately
        super.onResume();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String settingsUrl = pref.getString("settings_iris_url", "").toLowerCase();
        if (!settingsUrl.equals(getIrisUrl())) {
            setIrisUrl(settingsUrl);
            IrisWebviewHelper.refresh();
        }
    }


    /**
     * Handler for opening login windows
     */
    class UriChromeClient extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            setPopupWebView(new WebView(getContext()));
            getPopupWebView().setVerticalScrollBarEnabled(false);
            getPopupWebView().setHorizontalScrollBarEnabled(false);
            getPopupWebView().setWebViewClient(new UriWebViewClient());
            IrisWebviewHelper.setWebSettings(getPopupWebView());
            getPopupWebView().setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            getFrameLayout().addView(getPopupWebView());
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(getPopupWebView());
            resultMsg.sendToTarget();
            return true;
        }

    }
}
