package org.gospelcoding.dailydose;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String VIMEO_URL_EXTRA = "org.gospelcoding.dailydose.vimeo_url";
    public static final String SHARED_PREFERENCES_TAG = "org.gospelcoding.dailydose.Shared_Prefs";
    public static final String DOWNLOADED_ALL = "downloadedAll";
    public static final String VERSION = "version";

    private boolean chapterSpinnerFrozen = false;

    DDGArrayAdapter episodesAdapter;
    ArrayAdapter bookNamesAdapter;
    ArrayAdapter chaptersAdapter;
    DDGNetworkHelper networkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_episodes);

        processUpdate();

        networkHelper = new DDGNetworkHelper(this);

        AlarmManager.setAlarmIfNecessary(this);

        new LoadEpisodesFromDB().execute();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(episodesAdapter != null && episodesAdapter.getCount() > 1) {
            Episode.updateEpisodeList(episodesAdapter);
            fetchNewEpisodes(episodesAdapter.getCount());
        }
    }

    private void processUpdate(){
        SharedPreferences values = getSharedPreferences(SHARED_PREFERENCES_TAG, 0);
        int lastVersion = values.getInt(VERSION, 0);

        // We want to remove this once everybody is >= 9 (0.8)
        if(lastVersion < 9){
            Episode.updateBibleBookNames(this);
        }

        int currentVersion = BuildConfig.VERSION_CODE;
        SharedPreferences.Editor valuesEditor = values.edit();
        valuesEditor.putInt(VERSION, currentVersion);
        valuesEditor.commit();
    }

    public void addNewEpisodes(ArrayList<Episode> episodes){
        if(episodes.size() == 0)
            processUpdate();
        if(episodesAdapter == null) {
            setupEpisodesAdapter(episodes);
            networkHelper.fetchAllEpisodes();
        }
        else{
            for(Episode episode : episodes){
                episodesAdapter.insert(episode);
            }
        }

    }

    public void alertNoInternet(){
        TextView noInternetTextView = (TextView) findViewById(R.id.no_internet_text);
        if(noInternetTextView != null)
            noInternetTextView.setVisibility(View.VISIBLE);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new ConnectivityReceiver(), filter);
    }

    private AdapterView.OnItemClickListener episodeClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            CheckBox watched = (CheckBox) view.findViewById(R.id.watched_checkbox);
//            watched.setChecked(true);
            Episode clickedEpisode = episodesAdapter.getItem(position);
            episodesAdapter.markWatched(clickedEpisode);
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

            setupSpinners();
        }
    }

    public void updateSpinners(String bookName, String chapter){
        Spinner bookSpinner = (Spinner) findViewById(R.id.book_spinner);
        Spinner chapterSpinner = (Spinner) findViewById(R.id.chapter_spinner);

        if(bookNamesAdapter.getPosition(bookName) < 0) {
            String selectedBookName = (String) bookSpinner.getSelectedItem();
            episodesAdapter.insertBookName(bookNamesAdapter, bookName);
            if(!((String) bookSpinner.getSelectedItem()).equals(selectedBookName)) {
                chapterSpinnerFrozen = true;
                bookSpinner.setSelection(bookNamesAdapter.getPosition(selectedBookName));
            }
        }

        else if(bookSpinner.getSelectedItem().equals(bookName) && chaptersAdapter.getPosition(chapter) < 0) {
            String selectedChapter = (String) chapterSpinner.getSelectedItem();
            episodesAdapter.insertChapter(chaptersAdapter, chapter);
            chapterSpinner.setSelection(chaptersAdapter.getPosition(selectedChapter));
        }
    }

    private void setupSpinners(){
        List<String> bookNames = episodesAdapter.bookNames();
        bookNamesAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, bookNames);
        Spinner bookNameSpinner = (Spinner) findViewById(R.id.book_spinner);
        bookNameSpinner.setAdapter(bookNamesAdapter);
        bookNameSpinner.setOnItemSelectedListener(this);

        updateChapterSpinner();
    }

    public void updateChapterSpinner(){
        Spinner bookNamesSpinner = (Spinner) findViewById(R.id.book_spinner);
        String bookName = (String) bookNamesSpinner.getSelectedItem();
        List<String> chapters = episodesAdapter.chapterNumbers(bookName);
        chaptersAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, chapters);
        Spinner chaptersSpinner = (Spinner) findViewById(R.id.chapter_spinner);
        chaptersSpinner.setAdapter(chaptersAdapter);
        chaptersSpinner.setOnItemSelectedListener(this);
    }

    private void onBookSelected(String bookName){
        Spinner chapterSpinner = (Spinner) findViewById(R.id.chapter_spinner);
        if(bookName.equals(DDGArrayAdapter.ALL) || bookName.equals(DDGArrayAdapter.SPECIALS)){
            chapterSpinner.setVisibility(View.INVISIBLE);
            episodesAdapter.getFilter().filter(bookName);
        }
        else{
            if(!chapterSpinnerFrozen)
                updateChapterSpinner();
            chapterSpinner.setVisibility(View.VISIBLE);
        }
        chapterSpinnerFrozen = false;
    }

    private void onChapterSelected(String chapter){
        String bookName = (String) ((Spinner) findViewById(R.id.book_spinner)).getSelectedItem();
        if(chapter == DDGArrayAdapter.ALL)
            episodesAdapter.getFilter().filter(bookName);
        else
            episodesAdapter.getFilter().filter(bookName + ":" + chapter);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
        // This method gets called for both spinners
        Spinner bookNamesSpinner = (Spinner) findViewById(R.id.book_spinner);
        Spinner chaptersSpinner = (Spinner) findViewById(R.id.chapter_spinner);
        if(parent == bookNamesSpinner)
            onBookSelected((String) parent.getSelectedItem());
        else if(parent == chaptersSpinner)
            onChapterSelected((String) parent.getSelectedItem());
        else
            Log.e("Spinners", "VideoListActivity.onItemSelected() called for neither bookNamesSpinnner nor chaptersSpinner");
    }

    public void onNothingSelected(AdapterView<?> parent){
        // Don't think my code lets me get here
        Log.e("Spinners", "Unexpected call of VideoListActivity.onNothingSelected()");
    }

    private void fetchNewEpisodes(int existingCount){
        SharedPreferences values = getSharedPreferences(SHARED_PREFERENCES_TAG, 0);
        boolean downloadedAll = values.getBoolean(DOWNLOADED_ALL, false);
        if(existingCount == 0)
            networkHelper.initialFetchNewEpisodes(this);
        else if(!downloadedAll)
            networkHelper.fetchAllEpisodes();
        else
            networkHelper.fetchNewEpisodes();
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

    private class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            if (networkHelper.internetAvailable()){
                TextView noInternetTextView = (TextView) findViewById(R.id.no_internet_text);
                if(noInternetTextView != null)
                    noInternetTextView.setVisibility(View.VISIBLE);

                if (episodesAdapter == null)
                    fetchNewEpisodes(0);
                else
                    fetchNewEpisodes(episodesAdapter.getCount());
                context.unregisterReceiver(this);
            }
        }
    }
}
