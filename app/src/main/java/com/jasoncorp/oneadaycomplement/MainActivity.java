package com.jasoncorp.oneadaycomplement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String NOTIFICATION_CHANNEL_ID_COMPLEMENT = "channel_complement";
    public static final String NOTIFICATION_CHANNEL_NAME_COMPLEMENT = "Complement";
    public static final String WORK_TAG = "notificationWork";
    private String DBEventIDTag = "id_tag";
    private int DBEventID = 0;

    private Button btnStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btn_start);

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
    }

    void startWork() {
        Log.d(TAG, "startWork: ");

        Data inputData = new Data.Builder().putInt(DBEventIDTag, DBEventID).build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(1000, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG)
                .build();

        WorkManager.getInstance(this).enqueue(notificationWork);
    }

    /**
     * Sets up the default notification channel for API 26+
     */
    public void setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = NOTIFICATION_CHANNEL_ID_COMPLEMENT;
            CharSequence name = NOTIFICATION_CHANNEL_NAME_COMPLEMENT;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(id, name, importance);

            //Register the channel with the system, you can't change the importance or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
