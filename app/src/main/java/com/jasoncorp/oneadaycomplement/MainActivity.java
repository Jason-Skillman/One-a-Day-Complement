package com.jasoncorp.oneadaycomplement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    public static final String NOTIFICATION_CHANNEL_ID_COMPLEMENT = "channel_complement";
    public static final String NOTIFICATION_CHANNEL_NAME_COMPLEMENT = "Complement";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNotificationChannels();
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
