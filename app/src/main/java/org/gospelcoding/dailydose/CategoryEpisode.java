package org.gospelcoding.dailydose;

import com.orm.SugarRecord;

/**
 * Created by rick on 6/12/17.
 */

public class CategoryEpisode extends SugarRecord<CategoryEpisode> {
    Category category;
    Episode episode;

    public CategoryEpisode(){
    }

    public CategoryEpisode(Category c, Episode e){
        category = c;
        episode = e;
    }
}
