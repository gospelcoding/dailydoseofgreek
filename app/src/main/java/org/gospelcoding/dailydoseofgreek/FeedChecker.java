package org.gospelcoding.dailydoseofgreek;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

public class FeedChecker extends BroadcastReceiver {

    DDGNetworkHelper networkHelper;

//    public FeedChecker() {
//        super("FeedChecker");
//        networkHelper = new DDGNetworkHelper(this);
//    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO Does this need a wake lock to be correct?

//        Log.e("DDG Alarm", "Alarm went off now!");
        networkHelper = new DDGNetworkHelper(context);
        networkHelper.fetchNewEpisodesAndNotify();
    }
}
