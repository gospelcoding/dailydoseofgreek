package org.gospelcoding.dailydose;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rick on 7/29/17.
 */

public class DDGArrayAdapter extends ArrayAdapter<Episode> {

    private Context context;
    private List<Episode> episodes;

    public DDGArrayAdapter(Context context, List<Episode> episodes){
        super(context, -1, episodes);
        this.context = context;
        this.episodes = episodes;
    }

    public List<String> bookNames(){
        List<String> bookNames = new ArrayList<String>();
        for(Episode e : episodes){
            if(e.bibleBook != null && !bookNames.contains(e.bibleBook)){
                bookNames.add(e.bibleBook);
            }
        }
        bookNames = BibleBook.sort(context, bookNames);
        return bookNames;
    }

    public List<Integer> chapterNumbers(String bookName){
        List<Integer> chapters = new ArrayList<Integer>();
        for(Episode e :episodes){
            // String chapter = e.bibleChapter
            Integer chapter = e.bibleChapter;
            if(bookName.equals(e.bibleBook) && !chapters.contains(chapter)){
                int i=0;
                while(i<chapters.size() && chapter.compareTo(chapters.get(i)) > 0)
                    ++i;
                chapters.add(i, chapter);
            }
        }
        return chapters;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View episodeView = inflater.inflate(R.layout.episode_list_view, parent, false);
        TextView titleView = (TextView) episodeView.findViewById(R.id.title_view);
        TextView pubDateView = (TextView) episodeView.findViewById(R.id.pub_date_view);
        Episode episode = episodes.get(position);
        titleView.setText(episode.getTitle());
        Date pubDate = new Date(episode.pubDate);
        if(episode.lastWatched != 0) {
            CheckBox watchedCheckBox = (CheckBox) episodeView.findViewById(R.id.watched_checkbox);
            watchedCheckBox.setChecked(true);
        }
        if(episode.featured){
            TextView continueWatching = (TextView) episodeView.findViewById(R.id.continue_watching);
            continueWatching.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            continueWatching.setText(context.getString(R.string.continue_watching) + " " + episode.bibleBook);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        String dateString = dateFormat.format(pubDate);
        pubDateView.setText(dateString);

        return episodeView;
    }

}
