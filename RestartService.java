package com.yougowegoteam.demo;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class RestartService extends Service {

    private String TAG = "RestartService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Log.d(TAG, "RestartService called()");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        Notification notification = builder.build();
        startForeground(9, notification);

        Intent in = new Intent(this, GPSService.class);
        startService(in);

        stopForeground(true);
        stopSelf();

        return START_NOT_STICKY;
    }
}
