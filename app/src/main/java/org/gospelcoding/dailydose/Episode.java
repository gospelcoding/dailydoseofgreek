package org.gospelcoding.dailydose;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.prof.rssparser.Article;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rick on 6/11/17.
 */

public class Episode extends SugarRecord<Episode> implements Serializable {
    String title;
    String vimeoUrl;
    String ddgUrl;
    long pubDate;
    long lastWatched;
    String bibleBook;
    int bibleChapter;

    @Ignore
    boolean featured = false;

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

    public boolean olderThan(Episode e){
        return pubDate < e.pubDate;
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

    public static List<Episode> listAllInOrder(){
        List<Episode> episodes = find(Episode.class, null, null, null, "pub_date DESC", null);
        if(episodes.size() == 0)
            return episodes;
        Episode featured = findFeaturedEpisode();
        if(featured != null && featured.id != episodes.get(0).id)
            episodes.add(0, featured);
        return episodes;
    }

    public String toString(){
        return title;
    }
    //Accessors
    public String getTitle(){
        return title;
    }

    private static Episode findFeaturedEpisode(){
        List<Episode> watchedEpisodes = find(Episode.class, "last_watched NOT NULL", null, null, "last_watched DESC", null);
        int i = 0;
        while(watchedEpisodes.get(i).bibleBook == null) {
            ++i;
            if (i == watchedEpisodes.size())
                return null;
        }
        Episode lastEpisode = watchedEpisodes.get(i);
        String[] whereArgs = {lastEpisode.bibleBook, String.valueOf(lastEpisode.pubDate)};
        List<Episode> nextEpisode = find(Episode.class, "bible_book = ? and pub_date > ?", whereArgs, null, "pub_date ASC", "1");
        if(nextEpisode.size() > 0) {
            Episode featured = nextEpisode.get(0);
            featured.featured = true;
            return featured;
        }
        return null;
    }
//    public static void debugDeleteMostReccentEpisode(){
//       List<Episode> episodes = find(Episode.class, null, null, null, "pub_date DESC", null);
//        Episode e = episodes.get(0);
//        Log.e("DDG Alarm", "Deleting episode: " + e.title);
//        e.delete();
//    }
}
