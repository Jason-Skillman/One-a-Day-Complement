package com.jasoncorp.oneadaycomplement;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private Date timerDate;
    private int timerHours, timerMinutes;

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
                startAlarm();
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

    public void startAlarm() {
        //1 day
        int intervalOneADay = 1000 * 60 * 60 * 24;
        //20 minutes
        //int interval = 1000 * 60 * 20;

        //Set the time for the alarm
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, timerHours);
        calendar.set(Calendar.MINUTE, timerMinutes);

        //Start the alarm
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalOneADay, pendingIntent);

        Toast.makeText(this, "Complements have been turned on", Toast.LENGTH_SHORT).show();
    }

    public void cancelAlarm() {
        alarmManager.cancel(pendingIntent);

        Toast.makeText(this, "Complements have been turned off", Toast.LENGTH_SHORT).show();
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

                String minutesStr = "" + selectedMinute;
                if(selectedMinute < 10) minutesStr += 0;

                //Update the UI to show the alarm time
                tvTime.setText(hour + ":" + minutesStr + " " + AM_PM);

                //Convert the time in string format
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(selectedHour)
                        .append(":")
                        .append(selectedMinute);

                //Save the time in the user preferences
                UserPreferencesManager.getInstance().setAlarmTime(MainActivity.this, stringBuilder.toString());

                Toast.makeText(MainActivity.this, "Alarm has been set for: " + hour + ":" + minutesStr + " " + AM_PM, Toast.LENGTH_SHORT).show();
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
        timerHours = Integer.parseInt(hoursStr);

        String minutesStr = "" + minutes;
        if(minutes < 10) minutesStr += "0";
        timerMinutes = Integer.parseInt(minutesStr);

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
