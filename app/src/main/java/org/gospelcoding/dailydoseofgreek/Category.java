package org.gospelcoding.dailydoseofgreek;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by rick on 6/11/17.
 */

public class Category {
    private int id;
    private String name;

    public Category(String newName){
        id = -1;
        name = newName;
    }

    public Category(int newID, String newName){
        id = newID;
        name = newName;
    }

    public static Category getFromDB(SQLiteDatabase db, int searchID){
        String where = DBContract.CategoryEntry._ID + "=?";
        String[] whereVars = new String[]{Integer.toString(searchID)};
        Cursor cursor = db.query(DBContract.CategoryEntry.TABLE_NAME, null, where,
                whereVars, null, null, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            return new Category(cursor.getInt(cursor.getColumnIndex(DBContract.CategoryEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(DBContract.CategoryEntry.NAME_COLUMN)));
        }else{
            return null;
        }
    }

    public int static int getIdByName(String searchName){
        //search for the record, return id or -1 if nonexistant
    }

    public static int addIfNecessary(String newName){
        if(getIdByName(newName) >= 0){
            //save the new record
        }
    }



    public String getName(){
        return name;
    }
}
