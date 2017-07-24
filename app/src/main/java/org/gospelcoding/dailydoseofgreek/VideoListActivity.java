package org.gospelcoding.dailydoseofgreek;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoListActivity extends AppCompatActivity {

    public static final String VIMEO_URL_EXTRA = "org.gospelcoding.dailydoseofgreek.vimeo_url";
    ArrayAdapter<Episode> episodesAdapter;
    DDGNetworkHelper networkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        networkHelper = new DDGNetworkHelper(this);

        new LoadEpisodesFromDB().execute();
        setAlarmIfNecessary();
    }

    public void fetchAllEpisodes(View v){
        networkHelper.fetchAllEpisodes(episodesAdapter);
    }

    private boolean episodesNotFound(ArrayList<Article> list){
        if(list.size() == 0)
            return true;
        Pattern failPattern = Pattern.compile("404 Not Found");
        Matcher m = failPattern.matcher(list.get(0).getTitle());
        if(m.find())
            return true;
        return false;
    }

    private AdapterView.OnItemClickListener episodeClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Episode clickedEpisode = episodesAdapter.getItem(position);
            launchPlayEpisodeActivity(clickedEpisode);
        }
    };


    private void launchPlayEpisodeActivity(Episode episode){
        Intent intent = new Intent(this, PlayEpisodeActivity.class);
        intent.putExtra(PlayEpisodeActivity.EPISODE_ID_EXTRA, episode.getId());
        startActivity(intent);
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

    private void setAlarmIfNecessary(){
        Intent intent = new Intent(this, FeedChecker.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                                         nextCalendarAtTime(10, 15).getTimeInMillis(),
                                         AlarmManager.INTERVAL_DAY,
                                         alarmIntent);
    }

    private Calendar nextCalendarAtTime(int hour, int minute){
        Calendar now = Calendar.getInstance();
        Calendar rVal = Calendar.getInstance();
        rVal.set(Calendar.HOUR_OF_DAY, hour);
        rVal.set(Calendar.MINUTE, minute);
        if(now.getTimeInMillis() > rVal.getTimeInMillis())
            rVal.add(Calendar.DAY_OF_MONTH, 1);
        return rVal;
    }

    private void setupEpisodesAdapter(List<Episode> episodes){
        episodesAdapter = new ArrayAdapter<Episode>(this,
                android.R.layout.simple_list_item_1, episodes);
        ListView episodesView = (ListView) findViewById(R.id.episodes_listview);
        episodesView.setAdapter(episodesAdapter);
        episodesView.setOnItemClickListener(episodeClickListener);
    }

    private void fetchNewEpisodes(int existingCount){
        if(existingCount == 0)
            networkHelper.initialFetchNewEpisodes(episodesAdapter);
        else
            networkHelper.fetchNewEpisodes(episodesAdapter);
    }

    private class LoadEpisodesFromDB extends AsyncTask<Void, Void, List<Episode>> {

        protected List<Episode> doInBackground(Void... params){
            return Episode.listAllInOrder();
        }

        protected void onPostExecute(List<Episode> episodes){
            setupEpisodesAdapter(episodes);
            fetchNewEpisodes(episodes.size());
        }
    }
}
