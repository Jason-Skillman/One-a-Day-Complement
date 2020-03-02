package com.jasoncorp.oneadaycomplement;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Random;

public class NotificationWorker extends Worker {

    private static final String TAG = "NotificationWorker";

    public static final int NOTIFICATION_ID = 0;

    private Context context;
    private Random random;


    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        random = new Random();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: ");

        //Pull the complements from the xml
        Resources resources = context.getResources();
        String[] complements = resources.getStringArray(R.array.complements_array);

        //Choose the complement of the day
        int randomIndex = random.nextInt(complements.length);

        String title = "Complement of the Day";
        String text = complements[randomIndex];

        //Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID_COMPLEMENT)
                .setSmallIcon(R.drawable.ic_favorite_white_24dp)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Start the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        return Result.success();
    }

}
