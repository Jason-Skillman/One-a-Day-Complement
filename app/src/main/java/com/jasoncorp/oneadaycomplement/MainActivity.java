package com.jasoncorp.oneadaycomplement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jasoncorp.oneadaycomplement.receivers.AlarmReceiver;
import com.jasoncorp.oneadaycomplement.receivers.BootReceiver;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    private Date alarmDate;
    private boolean alarmStatus;

    private Button btnSetTime, btnStart, btnStop;
    private TextView tvStatus, tvTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tv_status);
        tvTime = findViewById(R.id.tv_time);
        btnSetTime = findViewById(R.id.btn_time);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);

        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        setupActivityDefaults();
        setupNotificationChannels();
    }

    @Override
    protected void onResume() {
        super.onResume();

        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSetTime();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlarm();
                Toast.makeText(MainActivity.this, "Complements have been turned on", Toast.LENGTH_SHORT).show();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
                Toast.makeText(MainActivity.this, "Complements have been turned off", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        btnSetTime.setOnClickListener(null);
        btnStart.setOnClickListener(null);
        btnStop.setOnClickListener(null);
    }

    /**
     * Setup the activity based on the user preferences
     */
    public void setupActivityDefaults() {
        //Get and set the alarm status
        alarmStatus = UserPreferencesManager.getInstance().getAlarmStatus(MainActivity.this);
        setAlarmStatus(alarmStatus);

        //Get and set the alarm time
        alarmDate = UserPreferencesManager.getInstance().getAlarmTime(MainActivity.this);
        setAlarmTime(getAlarmHour(), getAlarmMinute());
    }

    /**
     * On click function for setting the alarm time
     */
    void onClickSetTime() {
        final Calendar calendar = Calendar.getInstance();
        int hourDisplay = calendar.get(Calendar.HOUR_OF_DAY) + 1;

        TimePickerDialog timePicker;
        timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                setAlarmTime(selectedHour, selectedMinute);

                Toast.makeText(MainActivity.this, "Alarm has been set for: " + normalizeHour(selectedHour) + ":" +
                        getReadableMinute(selectedMinute) + " " + getAM_PM(selectedHour), Toast.LENGTH_SHORT).show();

                boolean value = UserPreferencesManager.getInstance().getAlarmStatus(MainActivity.this);
                if(value) cancelAlarm();
                startAlarm();
            }
        }, hourDisplay, 0, false);
        timePicker.setTitle("Select Time");
        timePicker.show();
    }

    /**
     * Starts the alarm
     */
    public void startAlarm() {
        if(alarmStatus)
            cancelAlarm();

        setAlarmStatus(true);

        //Set the time for the alarm
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        //Calculate the day of the alarm
        if(calendar.get(Calendar.HOUR_OF_DAY) > getAlarmHour()) {   //Hour has already passed
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        } else if(calendar.get(Calendar.HOUR_OF_DAY) == getAlarmHour()) {   //Hours are the same
            if(calendar.get(Calendar.MINUTE) > getAlarmMinute()) {
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
            } else {
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
            }
        } else {    //Hour is in the future
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
        }

        //Set the hour and minute of the alarm
        calendar.set(Calendar.HOUR_OF_DAY, getAlarmHour());
        calendar.set(Calendar.MINUTE, getAlarmMinute());

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        //Turn on boot receiver
        ComponentName receiver = new ComponentName(MainActivity.this, BootReceiver.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Cancels the alarm
     */
    public void cancelAlarm() {
        setAlarmStatus(false);

        if(alarmManager != null)
            alarmManager.cancel(pendingIntent);

        //Turn off boot receiver
        ComponentName receiver = new ComponentName(MainActivity.this, BootReceiver.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Sets the alarm status in user preferences and updates the UI
     * @param value the status
     */
    private void setAlarmStatus(boolean value) {
        UserPreferencesManager.getInstance().setAlarmStatus(MainActivity.this, value);

        //Update UI with alarm status
        String statusStr = value ? "On" : "Off";
        tvStatus.setText(statusStr);
    }

    /**
     * Sets the alarm time in user preferences and updates the UI
     * @param hour the hour in military time
     * @param minute
     */
    private void setAlarmTime(int hour, int minute) {
        UserPreferencesManager.getInstance().setAlarmTime(MainActivity.this, hour + ":" + minute);

        alarmDate = UserPreferencesManager.getInstance().getAlarmTime(MainActivity.this);

        //Update UI with alarm time
        tvTime.setText("" + normalizeHour(hour) + ":" +
                getReadableMinute(minute) + " " +
                getAM_PM(hour));
    }

    /**
     * Converts military hour to a normal hour
     * @param hourMilitary the military hour to convert
     * @return normalized hour
     */
    private int normalizeHour(int hourMilitary) {
        int hour = hourMilitary;
        if(hour == 0) hour = 12;
        else if(hour > 12) hour -= 12;
        return hour;
    }

    /**
     * Converts minute to a readable minute
     * @param minute the minute to convert
     * @return readable minute as a String
     */
    private String getReadableMinute(int minute) {
        String minuteStr = "" + minute;
        if(minute == 0) minuteStr += "0";
        return minuteStr;
    }

    /**
     * Finds if hour is in AM or PM time
     * @param hourMilitary the military hour
     * @return AM or PM as a String
     */
    private String getAM_PM(int hourMilitary) {
        String AM_PM = "AM";
        if(hourMilitary >= 12) AM_PM = "PM";
        return AM_PM;
    }

    /**
     * Gets the current hour the alarm is set for
     * @return the hour in military time
     */
    private int getAlarmHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(alarmDate);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Gets the current minute the alarm is set for
     * @return the minute
     */
    private int getAlarmMinute() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(alarmDate);
        return calendar.get(Calendar.MINUTE);
    }

    /**
     * Setup the default notification channel for API 26+
     */
    public void setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Set the notification data
            String id = getString(R.string.notification_channel_id_complement);
            CharSequence name = getString(R.string.notification_channel_name_complement);
            int importance = NotificationManager.IMPORTANCE_LOW;

            //Setup the notification channel
            NotificationChannel channel = new NotificationChannel(id, name, importance);

            //Register the channel with the system, you can't change the importance or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
