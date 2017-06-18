package org.gospelcoding.dailydoseofgreek;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;

/**
 * Created by rick on 6/18/17.
 */

public class DDGNetworkHelper {

    private DDGNetworkHelper(){}

//    public static void fetchVimeoUrl(Context context, Episode episode){
//       new FetchVimeoUrlTask(context).execute(episode);
//    }

    public class FetchVimeoUrlTask extends AsyncTask<Episode, Void, String[]> {
        private Context context;

        public FetchVimeoUrlTask(Context cxt){
            super();
            context = cxt;
        }

        protected String[] doInBackground(Episode... episodes){
            String[] vimeoUrls = new String[episodes.length];
            for (int i=0; i<episodes.length; i++) {
                vimeoUrls[i] = fetchVimeoUrl(episodes[i]);
            }
            return vimeoUrls;
        }

        private String fetchVimeoUrl(Episode episode){
            try {
                Document doc = Jsoup.connect(episode.ddgUrl).get();
                Element iframe = doc.select("iframe").first();
                String vimeoUrl = iframe.attr("src");
                episode.vimeoUrl = vimeoUrl;
                episode.save();
                return vimeoUrl;
            } catch (IOException e) {
                Log.e("DDG IO Error", e.getMessage());
                return null;
            }
        }

        protected void onProgressUpdate(){
            //Do nothing
        }

        protected void onPostExecute(String[] vimeoUrls){
            //Just going to show the first
            String msg;
            if(vimeoUrls[0] != null)
                msg = "Vimeo Url: " + vimeoUrls[0];
            else
                msg = "Unable to fetch vimeoUrl";
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();
        }


    }
}
