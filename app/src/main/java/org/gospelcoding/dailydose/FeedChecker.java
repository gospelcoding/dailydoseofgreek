package org.gospelcoding.dailydose;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

import java.util.Calendar;

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
        AlarmManager.logAlarmMessage(context, "Received alarm at " + Calendar.getInstance().getTime().toString());
        networkHelper = new DDGNetworkHelper(context);
        networkHelper.fetchNewEpisodesAndNotify();
    }
}
