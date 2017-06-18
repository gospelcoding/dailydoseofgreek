package org.gospelcoding.dailydoseofgreek;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    ArrayAdapter<Episode> episodesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        loadEpisodes();
        loadSomeRSS();
    }

    private void loadEpisodes(){
        List<Episode> episodes = Episode.listAll(Episode.class);
        episodesAdapter = new ArrayAdapter<Episode>(this,
                android.R.layout.simple_list_item_1, episodes);
        ListView episodesView = (ListView) findViewById(R.id.episodes_listview);
        episodesView.setAdapter(episodesAdapter);
        episodesView.setOnItemClickListener(episodeClickListener);
    }

    private void loadSomeRSS(){
        String urlString = "http://dailydoseofgreek.com/feed";
        Parser parser = new Parser();
        parser.execute(urlString);
        parser.onFinish(new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Article> list) {
                ArrayList<Episode> newEpisodes = Episode.saveEpisodesFromRSS(list);
                episodesAdapter.addAll(newEpisodes);
            }

            @Override
            public void onError() {
                Log.e("DDG RSS Error", "Some error");
            }
        });
    }

    private AdapterView.OnItemClickListener episodeClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.e("DDG Debug", "Clicked on episode at position: " + String.valueOf(position));
            Episode clickedEpisode = episodesAdapter.getItem(position);
            if(clickedEpisode.vimeoUrl != null) {
                Toast toast = Toast.makeText(parent.getContext(), "VimeoUrl is " + clickedEpisode.vimeoUrl, Toast.LENGTH_SHORT);
                toast.show();
            }
            else{
                new FetchVimeoUrlTask(parent.getContext()).execute(clickedEpisode);
            }
        }
    };

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

    private class FetchVimeoUrlTask extends AsyncTask<Episode, Void, String[]> {
        private Context context;

        public FetchVimeoUrlTask(Context cxt){
            super();
            context = cxt;
        }

        protected String[] doInBackground(Episode... episodes){
            String[] vimeoUrls = new String[episodes.length];
            for (int i=0; i<episodes.length; i++) {
                vimeoUrls[i] = fetchVimeoUrl(episodes[i]);
            }
            return vimeoUrls;
        }

        private String fetchVimeoUrl(Episode episode){
            try {
                Document doc = Jsoup.connect(episode.ddgUrl).get();
                Element iframe = doc.select("iframe").first();
                String vimeoUrl = iframe.attr("src");
                episode.vimeoUrl = vimeoUrl;
                episode.save();
                return vimeoUrl;
            } catch (IOException e) {
                Log.e("DDG IO Error", e.getMessage());
                return null;
            }
        }

        protected void onProgressUpdate(){
            //Do nothing
        }

        protected void onPostExecute(String[] vimeoUrls){
            //Just going to show the first
            String msg;
            if(vimeoUrls[0] != null)
                msg = "Vimeo Url: " + vimeoUrls[0];
            else
                msg = "Unable to fetch vimeoUrl";
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();
        }


    }
}
