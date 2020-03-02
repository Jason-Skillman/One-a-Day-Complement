package com.jasoncorp.oneadaycomplement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String WORK_TAG_NOTIFICATION = "work_tag_notification";
    private String DBEventIDTag = "id_tag";
    private int DBEventID = 0;

    private Button btnStart, btnStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);

        setupNotificationChannels();
    }

    @Override
    protected void onResume() {
        super.onResume();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWork();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelWork();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        btnStart.setOnClickListener(null);
        btnStop.setOnClickListener(null);
    }

    void startWork() {
        Log.d(TAG, "startWork: ");

        Data inputData = new Data.Builder().putInt(DBEventIDTag, DBEventID).build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(1000, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG_NOTIFICATION)
                .build();

        WorkManager.getInstance(this).enqueue(notificationWork);
    }

    void cancelWork() {
        Operation operation = WorkManager.getInstance(this).cancelAllWorkByTag(WORK_TAG_NOTIFICATION);

        Toast.makeText(this, "Complements have been turned off", Toast.LENGTH_SHORT).show();
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
