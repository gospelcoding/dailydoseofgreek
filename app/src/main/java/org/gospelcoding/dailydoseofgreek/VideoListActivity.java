package org.gospelcoding.dailydoseofgreek;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        List<Episode> episodes = Episode.listAll(Episode.class);
        ArrayAdapter<Episode> episodesAdapter = new ArrayAdapter<Episode>(this,
                android.R.layout.simple_list_item_1, episodes);
        ListView episodesView = (ListView) findViewById(R.id.episodes_listview);
        episodesView.setAdapter(episodesAdapter);
    }

    private void loadSomeRSS(){
        String urlString = "http://dailydoseofgreek.com/feed";
        Parser parser = new Parser();
        parser.execute(urlString);
        parser.onFinish(new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                Episode.saveEpisodesFromRSS(list);
                printAllTitles();
            }

            @Override
            public void onError() {
                //log.e('rss error', 'Some error')
            }
        });
    }

    private void printAllTitles(){
        List<Episode> episodes = Episode.listAll(Episode.class);
        String display = "";
        for(Episode episode : episodes){
            display += String.valueOf(episode.getId()) + " " + episode.getTitle() + ". Cat: ";
            if(episode.bibleBook == null)
                display += "Other\n";
            else
                display += episode.bibleBook + "\n";
        }
        //TextView tv = (TextView) findViewById(R.id.rssInfo);
        //tv.setText(display);
    }
}
