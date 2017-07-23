package org.gospelcoding.dailydoseofgreek;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class FeedChecker extends IntentService {

    DDGNetworkHelper networkHelper;

    public FeedChecker() {
        super("FeedChecker");
        networkHelper = new DDGNetworkHelper(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Download the latest RSS!
        networkHelper.fetchNewEpisodesAndNotify();
    }
}
