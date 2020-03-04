package com.jasoncorp.oneadaycomplement;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;

import java.text.ParseException;
import java.util.Date;

public class UserPreferencesManager {

    private static UserPreferencesManager INSTANCE;

    private static final String FILE_NAME = "UserPreferences";

    private static final String ALARM_TIME = "alarm_time";


    private UserPreferencesManager() { }


    public static UserPreferencesManager getInstance() {
        if(INSTANCE == null) INSTANCE = new UserPreferencesManager();
        return INSTANCE;
    }

    public Date getAlarmTime(Context context) {
        //Create the shared prefs
        SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

        //Read key value
        String result = prefs.getString(ALARM_TIME, "9:00");

        //Format String to Date
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            date = format.parse(result);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public void setAlarmTime(Context context, String value) {
        //Create the shared prefs
        SharedPreferences prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        //Write key value
        editor.putString(ALARM_TIME, value);
        editor.apply();
    }

}
