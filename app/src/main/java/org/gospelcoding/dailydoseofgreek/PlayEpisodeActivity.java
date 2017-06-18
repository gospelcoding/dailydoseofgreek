package org.gospelcoding.dailydoseofgreek;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class PlayEpisodeActivity extends AppCompatActivity {

    WebView webView;
    String vimeoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        vimeoUrl = getIntent().getStringExtra(VideoListActivity.VIMEO_URL_EXTRA);

        loadVideo();
    }

    private void loadVideo(){
        String html = "<iframe src=\"" +
                vimeoUrl +
                "\" width=\"1080\" height=\"608\" " +
                "frameborder=\"0\" title=\"Video Title\" " +
                "webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>";
        //webView.setJavascriptenabled();
        webView.loadData(html, "text/html", null);

    }
}
