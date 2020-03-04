package com.jasoncorp.oneadaycomplement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
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
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String WORK_TAG_NOTIFICATION = "work_tag_notification";
    private String DBEventIDTag = "id_tag";
    private int DBEventID = 0;

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    private Button btnStart, btnStop, btnTime;
    private EditText etTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnTime = findViewById(R.id.btn_time);
        etTime = findViewById(R.id.et_time);

        Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

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
                dialogTime();
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
        int interval = 1000 * 60 * 20;
        int interval2 = 1000 * 60 * 60 * 24;

        /* Set the alarm to start at 10:30 AM */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 41);

        /* Repeating on every 20 minutes interval */
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval, pendingIntent);

        Toast.makeText(this, "Alarm has been Set", Toast.LENGTH_SHORT).show();
    }

    public void cancelAlarm() {
        alarmManager.cancel(pendingIntent);

        Toast.makeText(this, "Alarm canceled", Toast.LENGTH_SHORT).show();
    }

    void dialogTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker;
        timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                etTime.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, false);
        timePicker.setTitle("Select Time");
        timePicker.show();
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
