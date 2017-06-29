package org.gospelcoding.dailydoseofgreek;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class PlayEpisodeActivity extends AppCompatActivity {

    HTML5WebView webView;
    String vimeoUrl;

    String html1 = "<!DOCTYPE html><html><style>html, body{height: 100%;} iframe{height: 100%; width: 100%}</style><body><iframe src='";
    String html2 = "?autoplay=1' frameborder='0' webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe></body></html>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vimeoUrl = getIntent().getStringExtra(VideoListActivity.VIMEO_URL_EXTRA);

        webView = new HTML5WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.OFF);
        webView.getSettings().setAllowFileAccess(true);

        webView.loadData(html1 + vimeoUrl + html2, "text/html", null);

        setContentView(webView.getLayout());

        //loadVideo();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        //Toast.makeText(this, "Config change", Toast.LENGTH_SHORT).show();
    }
}
