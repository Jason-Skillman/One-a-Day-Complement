package com.jasoncorp.oneadaycomplement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.Operation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String WORK_TAG_NOTIFICATION = "work_tag_notification";
    private String DBEventIDTag = "id_tag";
    private int DBEventID = 0;

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private Date timerDate;

    private Button btnStart, btnStop, btnTime;
    private TextView tvTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnTime = findViewById(R.id.btn_time);
        tvTime = findViewById(R.id.tv_time);

        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        setDefaultTime();
        setupNotificationChannels();
    }

    @Override
    protected void onResume() {
        super.onResume();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAt10();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
            }
        });
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarmTime();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        btnStart.setOnClickListener(null);
        btnStop.setOnClickListener(null);
        btnTime.setOnClickListener(null);
    }

    void startWork() {
        Log.d(TAG, "startWork: ");

        Data inputData = new Data.Builder().putInt(DBEventIDTag, DBEventID).build();

        /*OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(1000, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG_NOTIFICATION)
                .build();*/

        PeriodicWorkRequest workRequestNotification = new PeriodicWorkRequest.Builder(NotificationWorker.class,15, TimeUnit.MINUTES)
                .setInitialDelay(1000, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG_NOTIFICATION)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_TAG_NOTIFICATION, ExistingPeriodicWorkPolicy.REPLACE, workRequestNotification);
    }

    void cancelWork() {
        Operation operation = WorkManager.getInstance(this).cancelAllWorkByTag(WORK_TAG_NOTIFICATION);

        Toast.makeText(this, "Complements have been turned off", Toast.LENGTH_SHORT).show();
    }

    void startTimer() {
        int interval = 8000;
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

        Toast.makeText(this, "Alarm has been Set", Toast.LENGTH_SHORT).show();
    }

    public void startAt10() {
        //20 minutes
        int interval = 1000 * 60 * 20;
        //1 day
        int intervalOneADay = 1000 * 60 * 60 * 24;

        /* Set the alarm to start at 10:30 AM */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 41);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalOneADay, pendingIntent);

        Toast.makeText(this, "Alarm has been Set", Toast.LENGTH_SHORT).show();
    }

    //https://www.stacktips.com/tutorials/android/repeat-alarm-example-in-android

    public void cancelAlarm() {
        alarmManager.cancel(pendingIntent);

        Toast.makeText(this, "Alarm canceled", Toast.LENGTH_SHORT).show();
    }

    void setAlarmTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) + 1;

        TimePickerDialog timePicker;
        timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Log.d(TAG, "onTimeSet: " + selectedHour);
                //Find AM or PM
                String AM_PM = "AM";
                int hour = selectedHour;
                if(hour >= 12) {
                    AM_PM = "PM";
                    if(hour != 12) hour -= 12;
                }
                if(hour == 0) hour = 12;

                //Update the UI to show the alarm time
                tvTime.setText(hour + ":" + selectedMinute + "0 " + AM_PM);

                //Convert the time in string format
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(selectedHour)
                        .append(":")
                        .append(selectedMinute);

                //Save the time in the user preferences
                UserPreferencesManager.getInstance().setAlarmTime(MainActivity.this, stringBuilder.toString());
            }
        }, hour, 0, false);
        timePicker.setTitle("Select Time");
        timePicker.show();
    }

    public void setDefaultTime() {
        Date alarmTime = UserPreferencesManager.getInstance().getAlarmTime(MainActivity.this);

        timerDate = alarmTime;

        //Update the UI to show the alarm time
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timerDate);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        String AM_PM = "AM";
        if(hours >= 12) AM_PM = "PM";

        String hoursStr = "" + hours;
        if(hours == 0) hoursStr = "" + (hours + 12);
        else if(hours > 12) hoursStr = "" + (hours - 12);

        String minutesStr = "" + minutes;
        if(minutes < 10) minutesStr += "0";

        tvTime.setText(hoursStr + ":" + minutesStr + " " + AM_PM);
    }

    /**
     * Sets up the default notification channel for API 26+
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
