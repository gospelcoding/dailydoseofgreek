package org.gospelcoding.dailydose;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.Calendar;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    public static final String VIMEO_URL_EXTRA = "org.gospelcoding.dailydose.vimeo_url";
    DDGArrayAdapter episodesAdapter;
    DDGNetworkHelper networkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        networkHelper = new DDGNetworkHelper(this);

        setAlarmIfNecessary();
    }

    @Override
    protected void onResume(){
        super.onResume();
        new LoadEpisodesFromDB().execute();
    }

    private AdapterView.OnItemClickListener episodeClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CheckBox watched = (CheckBox) view.findViewById(R.id.watched_checkbox);
            watched.setChecked(true);
            Episode clickedEpisode = episodesAdapter.getItem(position);
            launchPlayEpisodeActivity(clickedEpisode);
        }
    };


    private void launchPlayEpisodeActivity(Episode episode){
        Intent intent = new Intent(this, PlayEpisodeActivity.class);
        intent.putExtra(PlayEpisodeActivity.EPISODE_ID_EXTRA, episode.getId());
        startActivity(intent);
    }

    private void setAlarmIfNecessary(){
        Intent intent = new Intent(this, FeedChecker.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        Calendar debugCal = nextCalendarAtTime(21, 37);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                                         nextCalendarAtTime(10, 15).getTimeInMillis(),
                                         AlarmManager.INTERVAL_DAY,
                                         alarmIntent);
//        Log.e("DDG Alarm", "Set Alarm for " + debugCal.getTime().toString());
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
        episodesAdapter = new DDGArrayAdapter(this, episodes);
        ListView episodesView = (ListView) findViewById(R.id.episodes_listview);
        episodesView.setAdapter(episodesAdapter);
        episodesView.setOnItemClickListener(episodeClickListener);
    }

    private void fetchNewEpisodes(int existingCount){
        if(existingCount == 0)
            networkHelper.fetchAllEpisodes(episodesAdapter);
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