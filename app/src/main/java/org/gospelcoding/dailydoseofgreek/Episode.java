package org.gospelcoding.dailydoseofgreek;

import android.util.Log;

import com.orm.SugarRecord;
import com.prof.rssparser.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rick on 6/11/17.
 */

public class Episode extends SugarRecord<Episode> {
    String title;
    String vimeoUrl;
    String ddgUrl;
    long pubDate;
    long lastWatched;
    String bibleBook;
    int bibleChapter;

    public Episode(){
    }

    public Episode(String newTitle, String newDdgUrl, Date newPubDate){
        title = newTitle;
        ddgUrl = newDdgUrl;
        pubDate = newPubDate.getTime();
        setBibleData();
    }

    private void setBibleData(){
        //Title for episodes about verses come in the format "{Book} {Chap}-{Verse}"
        Pattern chapterVerse = Pattern.compile("(.+) (\\d{1,3})-(\\d{1,3})");
        Matcher m = chapterVerse.matcher(title);
        if(!m.find())
            return;
        bibleBook = m.group(1);
        bibleChapter = Integer.parseInt(m.group(2));
    }

    public void addCategory(Category c){
        CategoryEpisode catEp = new CategoryEpisode(c, this);
        catEp.save();
    }

    public boolean saveIfNew(){
        if(ddgUrl == null)
            return false;
        List<Episode> existing = Episode.find(Episode.class, "ddg_url=?", ddgUrl);
        if (existing.size() > 0)
            return false;
        save();
        return true;
    }

    public static ArrayList<Episode> saveEpisodesFromRSS(ArrayList<Article> articles){
        ArrayList<Episode> newEpisodes = new ArrayList<Episode>();
        for (Article article: articles) {
            Episode episode = new Episode(article.getTitle(),
                                            article.getLink(),
                                            article.getPubDate());
            if(episode.saveIfNew())
                newEpisodes.add(episode);
        }
        return newEpisodes;
    }

    public String toString(){
        return title;
    }
    //Accessors
    public String getTitle(){
        return title;
    }

}
