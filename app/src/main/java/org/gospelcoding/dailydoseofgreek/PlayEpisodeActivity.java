package org.gospelcoding.dailydoseofgreek;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class PlayEpisodeActivity extends AppCompatActivity {

    HTML5WebView webView;
    String vimeoUrl;

    String html1 = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "</head>" +
                    "<body>" +
		            "<div class=\"videoOut\">" +
			        "<iframe id=\"player_1\" src=\"";
    String html2 = "?title=0&amp;byline=0&amp;portrait=0&amp;color=c9ff23&amp;player_id=player_1\" width=\"100%\" height=\"50%\" frameborder=\"0\" webkitAllowFullScreen mozallowfullscreen allowFullScreen>" +
	                "</iframe>" +
		            "</div>" +
	                "</body>" +
                    "</html>";

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
