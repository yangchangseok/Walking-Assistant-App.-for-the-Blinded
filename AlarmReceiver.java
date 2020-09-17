package com.yougowegoteam.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private String TAG = "AlarmReceiver";
    static public String ACTION_RESTART_SERVICE = "ACRION RESTART";


    @Override
    public void onReceive(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "AlarmReceiver called() 1");
            Intent in = new Intent(context,GPSService.class);
            context.startForegroundService(in);
        } else {
            Log.d(TAG, "AlarmReceiver called() 2");
            Intent in = new Intent(context, GPSService.class);
            context.startService(in);
        }
    }
}
