package org.gospelcoding.dailydose;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by rick on 6/11/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "daily_dose.db";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(DBContract.CategoryEntry.SQL_CREATE_TABLE);
        db.execSQL(DBContract.EpisodeEntry.SQL_CREATE_TABLE);
        db.execSQL(DBContract.CategoryEpisodeEntry.SQL_CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }


}
