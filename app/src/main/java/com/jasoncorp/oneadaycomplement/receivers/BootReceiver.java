package com.jasoncorp.oneadaycomplement.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;

import com.jasoncorp.oneadaycomplement.MainActivity;
import com.jasoncorp.oneadaycomplement.UserPreferencesManager;

import java.util.Date;

public class BootReceiver extends BroadcastReceiver {

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private int timerHours, timerMinutes;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            Date alarmTime = UserPreferencesManager.getInstance().getAlarmTime(context);

            //Update the UI to show the alarm time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(alarmTime);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);

            String hoursStr = "" + hours;
            if(hours == 0) hoursStr = "" + (hours + 12);
            else if(hours > 12) hoursStr = "" + (hours - 12);
            timerHours = Integer.parseInt(hoursStr);

            String minutesStr = "" + minutes;
            if(minutes < 10) minutesStr += "0";
            timerMinutes = Integer.parseInt(minutesStr);

            //Set the time for the alarm
            calendar.clear();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, timerHours);
            calendar.set(Calendar.MINUTE, timerMinutes);

            //Start the alarm
            alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

}
