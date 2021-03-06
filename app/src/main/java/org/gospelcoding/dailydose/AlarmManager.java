package org.gospelcoding.dailydose;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by rick on 10/13/17.
 */

public class AlarmManager {
    public static void setAlarmIfNecessary(Context context){
        Intent intent = new Intent(context, FeedChecker.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar alarmCal = nextCalendarAtTime(9, 0);
        alarmManager.setInexactRepeating(android.app.AlarmManager.RTC_WAKEUP,
                alarmCal.getTimeInMillis(),
                android.app.AlarmManager.INTERVAL_DAY,
                alarmIntent);
        Log.d("DDG Alarm", "Set Alarm for " + alarmCal.getTime().toString());
    }

    private static Calendar nextCalendarAtTime(int hour, int minute){
        Calendar now = Calendar.getInstance();
        Calendar rVal = Calendar.getInstance();
        rVal.set(Calendar.HOUR_OF_DAY, hour);
        rVal.set(Calendar.MINUTE, minute);
        int offset = rVal.getTimeZone().getRawOffset();
        rVal.add(Calendar.MILLISECOND, offset);
        if(now.getTimeInMillis() > rVal.getTimeInMillis())
            rVal.add(Calendar.DAY_OF_MONTH, 1);
        return rVal;
    }
}
