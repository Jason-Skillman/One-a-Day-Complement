package com.jasoncorp.oneadaycomplement.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jasoncorp.oneadaycomplement.R;

import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        //Pull the complements from the xml
        Resources resources = context.getResources();
        String[] complements = resources.getStringArray(R.array.complements_array);

        //Choose the complement of the day
        Random random = new Random();
        int randomIndex = random.nextInt(complements.length);

        String title = "Complement of the Day";
        String text = complements[randomIndex];

        //Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id_complement))
                .setSmallIcon(R.drawable.ic_favorite_white_24dp)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setPriority(NotificationCompat.PRIORITY_LOW);

        //Start the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(Integer.parseInt(context.getString(R.string.notification_id_complement)), builder.build());
    }

}
