package org.gospelcoding.dailydoseofgreek;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;


public class DDGNetworkHelper {

    private static final int INITIAL_FETCH = 0;
    private static final int FETCH_NEW = 1;
    private static final int FETCH_NEW_AND_NOTIFY = 2;
    private static final int FETCH_ALL = 3;

    //Parser parser;
    Context context;

    public DDGNetworkHelper(Context context){
        //parser = new Parser();
        this.context = context;
    }

    public void fetchNewEpisodesAndNotify(){
        fetchEpisodes(null, 1, FETCH_NEW);
    }

    public void fetchNewEpisodes(ArrayAdapter<Episode> episodesAdapter){
        fetchEpisodes(episodesAdapter, 1, FETCH_NEW);
    }

    public void initialFetchNewEpisodes(ArrayAdapter<Episode> episodesAdapter){
        fetchEpisodes(episodesAdapter, 1, INITIAL_FETCH);
    }

    public void fetchAllEpisodes(final ArrayAdapter<Episode> episodesAdapter){
        fetchEpisodes(episodesAdapter, 1, FETCH_ALL);
    }

    private void fetchEpisodes(final ArrayAdapter<Episode> episodesAdapter, final int page, final int fetchType){
        Parser parser = new Parser();
        parser.execute(urlForPage(page));
        parser.onFinish(new Parser.OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Article> articleList) {
                ArrayList<Episode> newEpisodes = Episode.saveEpisodesFromRSS(articleList);
                if(episodesAdapter != null)
                    addEpisodesToAdapter(episodesAdapter, newEpisodes);
                if(wantMoreEpisodes(fetchType, articleList.size(), newEpisodes.size()))
                    fetchEpisodes(episodesAdapter, page + 1, fetchType);
                if(fetchType == FETCH_NEW_AND_NOTIFY)
                    notifyNewEpisodes(newEpisodes);
            }

            @Override
            public void onError() {
                Log.e("DDG RSS Error", "Some error - Do we get here when we run out of episodes?");
            }
        });
    }

    private void notifyNewEpisodes(ArrayList<Episode> newEpisodes){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo);
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

    private String urlForPage(int page){
        String urlString = "http://dailydoseofgreek.com/feed";
        if(page > 1)
            urlString += "/?paged=" + String.valueOf(page);
        return urlString;
    }

    private void addEpisodesToAdapter(ArrayAdapter<Episode> episodesAdapter, ArrayList<Episode> newEpisodes){
        for (Episode episode : newEpisodes){
            int i = 0;
            while(i < episodesAdapter.getCount() && episode.olderThan(episodesAdapter.getItem(i)))
                ++i;
            episodesAdapter.insert(episode, i);
        }
    }
}
