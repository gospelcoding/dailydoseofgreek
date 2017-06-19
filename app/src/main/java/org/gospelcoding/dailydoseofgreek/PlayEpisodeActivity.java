package org.gospelcoding.dailydoseofgreek;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
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
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback){
                //what?
            }

            public void onHideCustomView(View view, WebChromeClient.CustomViewCallback callback){
                //and.......what?
            }
        });
        String html = "<iframe src=\"" +
                vimeoUrl +
                "\"  width=\"100%\" " +
                "frameborder=\"0\" title=\"Video Title\" " +
                "webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>";
        //webView.setJavascriptenabled();
        webView.loadData(html, "text/html", null);

    }
}
