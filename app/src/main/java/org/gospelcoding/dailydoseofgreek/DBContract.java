package org.gospelcoding.dailydoseofgreek;

import android.provider.BaseColumns;

/**
 * Created by rick on 6/11/17.
 */

public final class DBContract{

    private DBContract(){}

    public static class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String NAME_COLUMN = "name";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                NAME_COLUMN + " TEXT)";
    }

    public static class EpisodeEntry implements BaseColumns {
        public static final String TABLE_NAME = "episodes";
        public static final String TITLE_COLUMN = "title";
        public static final String PUBDATE_COLUMN = "pub_date";
        public static final String VIMEO_ID_COLUMN = "vimeo_id";
        public static final String DDG_URL_COLUMN = "ddg_url";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                TITLE_COLUMN + " TEXT, " +
                PUBDATE_COLUMN +  " INTEGER, " +
                VIMEO_ID_COLUMN + " TEXT, " +
                DDG_URL_COLUMN + " TEXT)";
    }

    public static class CategoryEpisodeEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories_episodes";
        public static final String CATEGORY_ID_COLUMN = "category_id";
        public static final String EPISODE_ID_COLUMN = "episode_id";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                CATEGORY_ID_COLUMN + " INTEGER, " +
                EPISODE_ID_COLUMN + " INTEGER)";
    }
}
