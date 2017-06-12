package org.gospelcoding.dailydoseofgreek;

import com.orm.SugarRecord;
import com.prof.rssparser.Article;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rick on 6/11/17.
 */

public class Episode extends SugarRecord<Episode> {
    String title;
    String vimeoId;
    String ddgUrl;
    long pubDate;
    long lastWatched;

    public Episode(){
    }

    public Episode(String newTitle, String newDdgUrl, Date newPubDate){
        title = newTitle;
        ddgUrl = newDdgUrl;
        pubDate = newPubDate.getTime();
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

    public static void saveEpisodesFromRSS(ArrayList<Article> articles){
        for (Article article: articles) {
            Episode episode = new Episode(article.getTitle(),
                                            article.getLink(),
                                            article.getPubDate());
            boolean savedNewEpisode = episode.saveIfNew();
            if(!savedNewEpisode)  //that is, we're done
                return;
        }
    }

    //Accessors
    public String getTitle(){
        return title;
    }

}
