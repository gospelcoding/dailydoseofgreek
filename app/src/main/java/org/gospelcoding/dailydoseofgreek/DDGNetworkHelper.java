package org.gospelcoding.dailydoseofgreek;

import android.content.Context;
import android.os.AsyncTask;
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


public class DDGNetworkHelper {

    private static final int INITIAL_FETCH = 0;
    private static final int FETCH_NEW = 1;
    private static final int FETCH_NEW_AND_NOTIFY = 2;
    private static final int FETCH_ALL = 3;

    Parser parser;

    public DDGNetworkHelper(){
        parser = new Parser();
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
        pickBackUpHere();
    }

    private boolean wantMoreEpisodes(int fetchType, int rssListSize, int savedEpisodeCount){
        switch(fetchType){
            case INITIAL_FETCH:
                return false;
            case FETCH_NEW:
            case FETCH_NEW_AND_NOTIFY:
                return rssListSize == savedEpisodeCount;
            case FETCH_ALL:
                return true;
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
