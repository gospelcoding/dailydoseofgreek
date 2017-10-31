package org.gospelcoding.dailydose;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rick on 10/24/17.
 */

public class BibleBook {
    private static final String FILENAME = "bible_book";

    private List<String> bookNames;

    public BibleBook(Context context){
        bookNames = readBookNamesFromFile(context);
    }

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
        String secondChoice = null;
        String bookName = reader.readLine();
        while(bookName != null){
            String bookNameDowncase = bookName.toLowerCase();
            if(bookNameDowncase.contains(possibleBook))
                return bookName;
            if(possibleBook.contains(bookNameDowncase))
                secondChoice = bookName;
            bookName = reader.readLine();
        }
        if(secondChoice != null)
            return secondChoice;
        return null;

    }
    
    public static List<String> readBookNamesFromFile(Context context) {
        try {
            List<String> bookNames = new ArrayList<String>();
            BufferedReader reader = reader(context);
            String bookName = reader.readLine();
            while (bookName != null) {
                bookNames.add(bookName);
                bookName = reader.readLine();
            }
            return bookNames;
        }
        catch(IOException e){
            Log.e("BibleBook", "IOException in BibleBook.getBookNames");
            Log.e("BibleBook", e.getMessage());
            return null;
        }
    }

    public List<String> sort(List<String> bookNamesToInclude){
        List<String> sortedList = new ArrayList<>(bookNamesToInclude.size());
        for(String bookName : bookNames){
            if(bookNamesToInclude.contains(bookName))
                sortedList.add(bookName);
        }
        return sortedList;
    }

    public void insertBookName(ArrayAdapter<String> bookNamesAdapter, String insertBook){
        int index = 1; //After "All"
        for(String checkBook : bookNames){
            if(checkBook.equals(insertBook)){
                bookNamesAdapter.insert(insertBook, index);
                return;
            }
            if(bookNamesAdapter.getItem(index).equals(checkBook))
                ++index;
        }
    }
}
