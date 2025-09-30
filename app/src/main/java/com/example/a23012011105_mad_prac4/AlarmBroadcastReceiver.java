package com.example.a23012011105_mad_prac4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;


public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Use the constants from AlarmServices to avoid magic strings
        String action = intent.getStringExtra(AlarmServices.ACTION_KEY);

        if (action != null) {
            Intent serviceIntent;

            if (AlarmServices.ACTION_START.equals(action)) {
                serviceIntent = AlarmServices.getStartIntent(context);
                ContextCompat.startForegroundService(context, serviceIntent);

            } else if (AlarmServices.ACTION_STOP.equals(action)) {
                serviceIntent = AlarmServices.getStopIntent(context);
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
    }
}
