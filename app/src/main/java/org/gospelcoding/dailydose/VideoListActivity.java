package org.gospelcoding.dailydose;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    public static final String VIMEO_URL_EXTRA = "org.gospelcoding.dailydose.vimeo_url";
    public static final String SHARED_PREFERENCES_TAG = "org.gospelcoding.dailydose.Shared_Prefs";
    public static final String DOWNLOADED_ALL = "downloadedAll";

    DDGArrayAdapter episodesAdapter;
    DDGNetworkHelper networkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_episodes);

        networkHelper = new DDGNetworkHelper(this);

        AlarmManager.setAlarmIfNecessary(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        new LoadEpisodesFromDB().execute();
    }

    public void setListView(ArrayList<Episode> episodes){
        setupEpisodesAdapter(episodes);
        networkHelper.fetchAllEpisodes(episodesAdapter);
    }

    public void alertNoInternet(){
        TextView noInternetTextView = (TextView) findViewById(R.id.no_internet_text);
        if(noInternetTextView != null)
            noInternetTextView.setVisibility(View.VISIBLE);
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

    private void setupEpisodesAdapter(List<Episode> episodes){
        if(episodes.size() > 0) {
            setContentView(R.layout.activity_video_list);
            episodesAdapter = new DDGArrayAdapter(this, episodes);
            ListView episodesView = (ListView) findViewById(R.id.episodes_listview);
            episodesView.setAdapter(episodesAdapter);
            episodesView.setOnItemClickListener(episodeClickListener);
        }
    }

    private void fetchNewEpisodes(int existingCount){
        SharedPreferences values = getSharedPreferences(SHARED_PREFERENCES_TAG, 0);
        boolean downloadedAll = values.getBoolean(DOWNLOADED_ALL, false);
        if(existingCount == 0)
            networkHelper.initialFetchNewEpisodes(this);
        else if(!downloadedAll)
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
