package org.gospelcoding.dailydoseofgreek;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;

public class VideoListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        loadSomeRSS();
    }

    private void loadSomeRSS(){
        String urlString = "http://dailydoseofgreek.com/feed";
        Parser parser = new Parser();
        parser.execute(urlString);
        parser.onFinish(new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                String title = list.get(0).getTitle();
                TextView rssInfo = (TextView) findViewById(R.id.rssInfo);
                rssInfo.setText(title);
            }

            @Override
            public void onError() {
                //log.e('rss error', 'Some error')
            }
        });
    }
}
