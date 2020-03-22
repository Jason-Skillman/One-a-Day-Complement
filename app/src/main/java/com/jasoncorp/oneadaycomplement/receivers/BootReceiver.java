package com.jasoncorp.oneadaycomplement.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.widget.Toast;

import com.jasoncorp.oneadaycomplement.UserPreferencesManager;

import java.util.Date;

public class BootReceiver extends BroadcastReceiver {

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

            //Fetch the time of the alarm
            Date alarmTime = UserPreferencesManager.getInstance().getAlarmTime(context);
            int hour = getHour(alarmTime);
            int minute = getMinute(alarmTime);

            //Set the time for the alarm
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            //Calculate the day of the alarm
            if(calendar.get(Calendar.HOUR_OF_DAY) > hour) {   //Hour has already passed
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
            } else if(calendar.get(Calendar.HOUR_OF_DAY) == hour) {   //Hours are the same
                if(calendar.get(Calendar.MINUTE) > minute) {
                    calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
                } else {
                    calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
                }
            } else {    //Hour is in the future
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
            }

            //Set the hour and minute of the alarm
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    private int getMinute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

}
