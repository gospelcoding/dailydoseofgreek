package org.gospelcoding.dailydose;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;
import java.util.Date;


public class DDGNetworkHelper {

    private static final int INITIAL_FETCH = 0;
    private static final int FETCH_NEW = 1;
    private static final int FETCH_NEW_AND_NOTIFY = 2;
    private static final int FETCH_ALL = 3;

    //Parser parser;
    Context context;
    VideoListActivity videoListActivity;
    DDGArrayAdapter episodesAdapter;

    public DDGNetworkHelper(Context context){
        //parser = new Parser();
        this.context = context;
    }

    public void fetchNewEpisodesAndNotify(){
        fetchEpisodes(1, FETCH_NEW_AND_NOTIFY);
    }

    public void fetchNewEpisodes(DDGArrayAdapter episodesAdapter){
        this.episodesAdapter = episodesAdapter;
        fetchEpisodes(1, FETCH_NEW);
    }

    public void initialFetchNewEpisodes(VideoListActivity listActivity){
        this.videoListActivity = listActivity;
        fetchEpisodes(1, INITIAL_FETCH);
    }

    public void fetchAllEpisodes(DDGArrayAdapter episodesAdapter){
        this.episodesAdapter = episodesAdapter;
        fetchEpisodes(1, FETCH_ALL);
    }

    private void fetchEpisodes(final int page, final int fetchType){
        if(!internetAvailable()){
            handleNoInternet(fetchType);
            return;
        }
        Parser parser = new Parser();
        parser.execute(urlForPage(page));
        parser.onFinish(new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Article> articleList) {
                ArrayList<Episode> newEpisodes = Episode.saveEpisodesFromRSS(articleList);
                if(fetchType == INITIAL_FETCH)
                    newEpisodesList(newEpisodes);
                if(episodesAdapter != null)
                    addEpisodesToAdapter(newEpisodes);
                if(wantMoreEpisodes(fetchType, articleList.size(), newEpisodes.size()))
                    fetchEpisodes(page + 1, fetchType);
                if(fetchType == FETCH_NEW_AND_NOTIFY)
                    notifyNewEpisodes(newEpisodes);
                if(fetchType == FETCH_ALL && articleList.size() == 0)
                    setDownloadedAll();
            }

            @Override
            public void onError() {
                Toast failToast = Toast.makeText(context, context.getString(R.string.download_error), Toast.LENGTH_LONG);
                failToast.show();
                Log.e("DDG RSS Error", "Some error - Do we get here when we run out of episodes?");
            }
        });
    }

    private void notifyNewEpisodes(ArrayList<Episode> newEpisodes){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo_notification)
                .setAutoCancel(true);
        if(newEpisodes.size() == 0)
            return;
        else if(newEpisodes.size() == 1)
            buildNotificationForSingleEpisode(mBuilder, newEpisodes.get(0));
        else
            buildNotificationForMultipleEpisodes(mBuilder, newEpisodes);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(1, mBuilder.build());
    }

    private void buildNotificationForSingleEpisode(NotificationCompat.Builder mBuilder, Episode episode){
        String title = "New Episode: " + episode.title;
        String text = "Published " + new Date(episode.pubDate).toString();
        Intent playEpisodeIntent = new Intent(context, PlayEpisodeActivity.class);
        playEpisodeIntent.putExtra(PlayEpisodeActivity.EPISODE_ID_EXTRA, episode.getId());
        PendingIntent playEpisodePendingIntent = PendingIntent.getActivity(context, 0, playEpisodeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentTitle(title).setContentText(text).setContentIntent(playEpisodePendingIntent);
    }

    private void buildNotificationForMultipleEpisodes(NotificationCompat.Builder mBuilder, ArrayList<Episode> newEpisodes){
        String title = String.valueOf(newEpisodes.size()) + " new episodes";
        String text = "";
        for(Episode episode : newEpisodes){
            text += episode.title + "\n";
        }
        Intent episodeListIntent = new Intent(context, VideoListActivity.class);
        PendingIntent episodeListPendingIntent = PendingIntent.getActivity(context, 0, episodeListIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentTitle(title).setContentText(text).setContentIntent(episodeListPendingIntent);
    }

    private boolean wantMoreEpisodes(int fetchType, int rssListSize, int savedEpisodeCount){
        Log.d("D/RSS", "rssListSize: " + String.valueOf(rssListSize));
        switch(fetchType){
            case INITIAL_FETCH:
                return false;
            case FETCH_NEW:
            case FETCH_NEW_AND_NOTIFY:
                return rssListSize == savedEpisodeCount;
            case FETCH_ALL:
                return rssListSize > 0;
        }
        Log.e("DDG Episde Fetch", "DDGNetworkHelper.wantMoreEpisodes called with invalid fetchType");
        return false;
    }

    private void setDownloadedAll(){
        SharedPreferences.Editor valuesEditor = context.getSharedPreferences(VideoListActivity.SHARED_PREFERENCES_TAG, 0).edit();
        valuesEditor.putBoolean(VideoListActivity.DOWNLOADED_ALL, true);
        valuesEditor.commit();
    }

    private String urlForPage(int page){
        String packageName = context.getPackageName();
        String language = packageName.substring(packageName.lastIndexOf('.') + 1);
        String urlString = "http://dailydoseof" + language + ".com/feed";
        if(page > 1)
            urlString += "/?paged=" + String.valueOf(page);
        return urlString;
    }

    private void newEpisodesList(ArrayList<Episode> newEpisodes){
        videoListActivity.setListView(newEpisodes);

    }

    private void addEpisodesToAdapter(ArrayList<Episode> newEpisodes){
        for (Episode episode : newEpisodes){
            int i = 0;
            while(i < episodesAdapter.getCount() && (episodesAdapter.getItem(i).featured || episode.olderThan(episodesAdapter.getItem(i))))
                ++i;
            episodesAdapter.insert(episode, i);
        }
    }

    private boolean internetAvailable(){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void handleNoInternet(int fetchType){
        if(fetchType == INITIAL_FETCH && videoListActivity != null)
            videoListActivity.alertNoInternet();
    }
}
