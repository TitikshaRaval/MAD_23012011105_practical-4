package com.example.a23012011105_mad_prac4

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val str1 = intent.getStringExtra("Service1")
        if (str1 == "Start" || str1 == "Stop") {
            val intentServices = Intent(context, AlarmServices::class.java)
            intentServices.putExtra("Service1", str1)

            // On Android O+ starting a foreground service must use startForegroundService
            if (str1 == "Start") {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intentServices)
                } else {
                    context.startService(intentServices)
                }
            } else { // "Stop"
                // Stop the service; sending stop to service so it can clean up its resources
                context.stopService(intentServices)
            }
        }
    }
}