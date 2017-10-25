package org.gospelcoding.dailydose;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by rick on 10/24/17.
 */

public class BibleBook {
    private static final String FILENAME = "bible_book";

    @Nullable
    public static String getBibleBook(Context context, String possibleBook){
        try {
            BufferedReader reader = reader(context);
            return getBibleBook(reader, possibleBook);
        }
        catch(IOException e){
            Log.e("BibleBook", "IOException in BibleBook.getBibleBook");
            Log.e("BibleBook", e.getMessage());
            return null;
        }
    }

    private static BufferedReader reader(Context context){
        InputStream input = context.getResources().openRawResource(R.raw.bible_books);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        return reader;
    }

    @Nullable
    private static String getBibleBook(BufferedReader reader, String possibleBook) throws IOException {
        possibleBook = possibleBook.toLowerCase();
        String bookName = reader.readLine();
        while(bookName != null){
            if(bookName.toLowerCase().contains(possibleBook)) {
                // Log.e("Match", possibleBook + " to " + bookName);
                return bookName;
            }
            bookName = reader.readLine();
        }
        // Log.e("Match", "None for " + possibleBook);
        return null;

    }
}
