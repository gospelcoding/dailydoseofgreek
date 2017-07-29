package org.gospelcoding.dailydoseofgreek;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class PlayEpisodeActivity extends AppCompatActivity {

    public static final String EPISODE_ID_EXTRA = "org.gospelcoding.dailydoseofgreek.episode_extra";

    HTML5WebView webView;
    Episode episode;

    String html1 = "<!DOCTYPE html><html><style>html, body{height: 100%;} iframe{height: 100%; width: 100%}</style><body><iframe src='";
    String html2 = "?autoplay=1' frameborder='0' webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe></body></html>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long episodeId = getIntent().getLongExtra(EPISODE_ID_EXTRA, -1);
        episode = Episode.findById(Episode.class, episodeId);
        if(episode.vimeoUrl == null)
            new FetchVimeoUrlTask().execute();
        else {
            loadVideo();
            updateVideo();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    private void loadVideo(){
        webView = new HTML5WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.OFF);
        webView.getSettings().setAllowFileAccess(true);

        webView.loadData(html1 + episode.vimeoUrl + html2, "text/html", null);

        setContentView(webView.getLayout());
    }

    private void updateVideo(){
        episode.lastWatched = System.currentTimeMillis();
        episode.save();
    }

    private void failureToast(){
        Toast.makeText(this, "Unable to retrieve video", Toast.LENGTH_SHORT).show();
    }

    private class FetchVimeoUrlTask extends AsyncTask<Void, Void, String> {
//
        protected String doInBackground(Void... params){
            try {
                Document doc = Jsoup.connect(episode.ddgUrl).get();
                Element iframe = doc.select("iframe").first();
                String vimeoUrl = iframe.attr("src");
                return vimeoUrl;
            } catch (IOException e) {
                Log.e("DDG IO Error", e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(String vimeoUrl){
            if(vimeoUrl == null) {
                failureToast();
                return;
            }
            episode.vimeoUrl = vimeoUrl;
            loadVideo();
            updateVideo();
        }
    }
}
