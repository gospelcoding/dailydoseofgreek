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
    private BibleBook bibleBook;
    public final static String ALL = "All";
    public final static String SPECIALS = "Specials";

    public DDGArrayAdapter(Context context, List<Episode> episodes){
        super(context, -1, episodes);
        this.context = context;
        this.episodes = episodes;
        this.allEpisodes = episodes;
        this.bibleBook = new BibleBook(context);
    }

    public List<String> bookNames(){
        List<String> bookNames = new ArrayList<String>();
        for(Episode e : allEpisodes){
            if(e.bibleBook != null && !bookNames.contains(e.bibleBook)){
                bookNames.add(e.bibleBook);
            }
        }
        bookNames = bibleBook.sort(bookNames);
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
    public Episode getItem(int position){
        return episodes.get(position);
    }

    @Override
    public int getCount(){
        return episodes.size();
    }

    public Episode getNewestById(){
        Episode newest = allEpisodes.get(0);
        for(Episode e : allEpisodes)
            if(newest.getId().compareTo(e.getId()) < 0)
                newest = e;
        return newest;
    }
//    public void insert(Episode e, int i){
//        super.insert(e, i);
//        if(e.bibleBook != null)
//            ((VideoListActivity) context).updateSpinners(e.bibleBook, String.valueOf(e.bibleChapter));
//    }
    public void insert(Episode e){
        if(episodes != allEpisodes && itemFilter.passesFilter(e)){
            int i=0;
            while(i<episodes.size() && (episodes.get(i).featured || e.olderThan(episodes.get(i))))
                ++i;
            episodes.add(i, e);
        }
        int i=0;
        while(i<allEpisodes.size() && (allEpisodes.get(i).featured || e.olderThan(allEpisodes.get(i))))
            ++i;
        allEpisodes.add(i, e);
        notifyDataSetChanged();

        if(e.bibleBook != null)
            ((VideoListActivity) context).updateSpinners(e.bibleBook, String.valueOf(e.bibleChapter));
    }

    public void setFeaturedEpisode(Episode featured) {
        removeFeatured(allEpisodes);
        removeFeatured(episodes);

        if (featured != null) {
            setFeaturedEpisode(allEpisodes, featured);
            if(episodes != allEpisodes && itemFilter.passesFilter(featured))
                setFeaturedEpisode(episodes, featured);
        }

        notifyDataSetChanged();
    }

    private void removeFeatured(List<Episode> eList){
        Episode first = eList.get(0);
        if (first.featured) {
            if (eList.size() > 1 && first.olderThan(eList.get(1)))
                eList.remove(0);
            else
                first.featured = false;
        }
    }

    private void setFeaturedEpisode(List<Episode> eList, Episode featured){
        if(eList.get(0).equals(featured))
            eList.get(0).featured = true;
        else
            eList.add(0, featured);
    }

    public void markWatched(Episode watched){
        for(Episode e : allEpisodes)
            if(e.equals(watched))
                e.lastWatched = System.currentTimeMillis();
        if(episodes != allEpisodes)
            for(Episode e : episodes)
                if(e.equals(watched))
                    e.lastWatched = System.currentTimeMillis();
    }

    public void insertBookName(ArrayAdapter<String> bookNamesAdapter, String bookName){
        bibleBook.insertBookName(bookNamesAdapter, bookName);
    }

    public void insertChapter(ArrayAdapter<String> chaptersAdapter, String chapter){
        int i=1; // After "All"
        int chapterInt = Integer.parseInt(chapter);
        while(i < chaptersAdapter.getCount() && Integer.parseInt(chaptersAdapter.getItem(i)) < chapterInt)
            ++i;
        chaptersAdapter.insert(chapter, i);
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

            // This can happen with featured episodes
            if(filteredEpisodes.size() > 1 && filteredEpisodes.get(1).equals(filteredEpisodes.get(0)))
                filteredEpisodes.remove(1);

            FilterResults results = new FilterResults();
            results.values = filteredEpisodes;
            results.count = filteredEpisodes.size();
            return results;
        }

        public boolean passesFilter(Episode episode){
            if(bookName == null)
                return true;
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
