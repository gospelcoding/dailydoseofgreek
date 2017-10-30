package org.gospelcoding.dailydose;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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

public class DDGArrayAdapter extends ArrayAdapter<Episode> implements Filterable {

    private Context context;
    private List<Episode> episodes;
    private List<Episode> allEpisodes;
    private ItemFilter itemFilter = new ItemFilter();
    public final static String ALL = "All";
    public final static String SPECIALS = "Specials";

    public DDGArrayAdapter(Context context, List<Episode> episodes){
        super(context, -1, episodes);
        this.context = context;
        this.episodes = episodes;
        this.allEpisodes = episodes;
    }

    public List<String> bookNames(){
        List<String> bookNames = new ArrayList<String>();
        for(Episode e : allEpisodes){
            if(e.bibleBook != null && !bookNames.contains(e.bibleBook)){
                bookNames.add(e.bibleBook);
            }
        }
        bookNames = BibleBook.sort(context, bookNames);
        bookNames.add(0, ALL);
        bookNames.add(SPECIALS);
        return bookNames;
    }

    public List<String> chapterNumbers(String bookName){
        List<String> chapters = new ArrayList<String>();
        for(Episode e :allEpisodes){
            // String chapter = e.bibleChapter
            String chapter = String.valueOf(e.bibleChapter);
            if(bookName.equals(e.bibleBook) && !chapters.contains(chapter)){
                int i=0;
                while(i<chapters.size() &&  e.bibleChapter > Integer.parseInt(chapters.get(i)))
                    ++i;
                chapters.add(i, chapter);
            }
        }
        chapters.add(0, ALL);
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

    @Override
    public int getCount(){
        return episodes.size();
    }

    public Filter getFilter(){
        return itemFilter;
    }

    private class ItemFilter extends Filter {
        private String bookName;
        private int chapter;

        @Override
        protected FilterResults performFiltering(CharSequence constraint){
            if(constraint.equals(ALL))
                return removeFilter();

            processConstraintString((String) constraint);

            final List<Episode> episodesToFilter = allEpisodes;
            final ArrayList<Episode> filteredEpisodes = new ArrayList<Episode>();

            for(Episode episode : episodesToFilter){
                if(passesFilter(episode))
                    filteredEpisodes.add(episode);
            }
            FilterResults results = new FilterResults();
            results.values = filteredEpisodes;
            results.count = filteredEpisodes.size();
            return results;
        }

        private boolean passesFilter(Episode episode){
            if(bookName == SPECIALS && episode.bibleBook == null)
                return true;
            if(bookName.equals(episode.bibleBook))
                if(chapter < 1 || chapter == episode.bibleChapter)
                    return true;
            return false;
        }

        private FilterResults removeFilter(){
            FilterResults results = new FilterResults();
            results.count = allEpisodes.size();
            results.values = allEpisodes;
            return results;
        }

        private void processConstraintString(String constraint){
            int colon = constraint.indexOf(':');
            if(colon < 0){
                bookName = constraint;
                chapter = 0;
            }
            else{
                bookName = constraint.substring(0, colon);
                chapter = Integer.parseInt(constraint.substring(colon + 1));
                Log.e("Spinners", "Constraint: " + constraint + "; book: " + bookName);
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results){
            episodes = (ArrayList<Episode>) results.values;
            notifyDataSetChanged();
        }
    }

}
