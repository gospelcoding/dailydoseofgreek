package org.gospelcoding.dailydose;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        String message = "Set Alarm for " + alarmCal.getTime().toString();
        logAlarmMessage(context, message);
        Log.d("DDG Alarm", message);
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

    public static void logAlarmMessage(Context context, String message){
        try {
            File dir = Environment.getExternalStoragePublicDirectory("daily-dose-logs");
            dir.mkdirs();
            String filename = context.getPackageName() + ".alarm-log";
            String fullPath = dir.getPath() + File.separator + filename;
            BufferedWriter out = new BufferedWriter(new FileWriter(fullPath, true));
            out.newLine();
            out.write(message);
            out.close();
        }
        catch (IOException e){
            Log.e("DDG-Alarm", e.getStackTrace().toString());
        }
    }
}
