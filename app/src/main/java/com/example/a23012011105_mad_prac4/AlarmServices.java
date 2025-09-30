package com.example.a23012011105_mad_prac4;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class AlarmServices extends Service {

    private static final String TAG = "AlarmService";

    // Use these constants to create intents, preventing typo-related bugs
    public static final String ACTION_KEY = "ServiceAction";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";

    private static final String CHANNEL_ID = "alarm_channel";
    private static final int NOTIFICATION_ID = 1001;

    private MediaPlayer mediaPlayer;


    /**
     * Creates an Intent to start the alarm service.
     * @param context The context from which to start the service.
     * @return A configured Intent to start the service.
     */
    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, AlarmServices.class);
        intent.putExtra(ACTION_KEY, ACTION_START);
        return intent;
    }

    /**
     * Creates an Intent to stop the alarm service.
     * @param context The context from which to start the service.
     * @return A configured Intent to stop the service.
     */
    public static Intent getStopIntent(Context context) {
        Intent intent = new Intent(context, AlarmServices.class);
        intent.putExtra(ACTION_KEY, ACTION_STOP);
        return intent;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getStringExtra(ACTION_KEY) : null;

        if (ACTION_START.equals(action)) {
            startAlarm();
            return START_STICKY; // If killed, the system should try to restart it.
        }

        // For any other action (including ACTION_STOP or a null intent), stop the service.
        stopAlarm();
        return START_NOT_STICKY; // Do not restart the service if it's explicitly stopped.
    }

    @Override
    public void onDestroy() {
        // Ensure all resources are released when the service is destroyed.
        releaseMediaPlayer();
        super.onDestroy();
        Log.d(TAG, "Service destroyed.");
    }

    private void startAlarm() {
        startAsForeground();
        startAlarmSound();
    }

    private void stopAlarm() {
        Log.d(TAG, "Stopping service...");
        stopAlarmSound();
        // Allow the notification to be removed
        stopForeground(true);
        // Stop the service itself
        stopSelf();
        Toast.makeText(this, "Alarm Cancelled!", Toast.LENGTH_SHORT).show();
    }

    private void startAsForeground() {
        createNotificationChannelIfNeeded();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Alarm")
                .setContentText("Alarm is ringing")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setOngoing(true) // Makes the notification non-dismissible
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void startAlarmSound() {
        // Initialize if not already done
        if (mediaPlayer == null) {
            initializeMediaPlayer();
        }

        // Start playing if initialization was successful and it's not already playing
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
                Log.d(TAG, "Alarm sound started.");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error starting MediaPlayer: ", e);
            }
        }
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            Log.d(TAG, "Alarm sound stopped.");
            // After stopping, you must prepare() again to replay.
            // Or simply release and re-create, which is safer.
            releaseMediaPlayer();
        }
    }

    private void initializeMediaPlayer() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            // Fallback to notification sound if alarm sound is not available
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        if (alarmUri == null) {
            Toast.makeText(this, "No alarm sound found on device.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Cannot start alarm sound, no default URI found.");
            return;
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, alarmUri);
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
            );
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare(); // Prepare the player for playback
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize MediaPlayer", e);
            Toast.makeText(this, "Failed to prepare alarm sound.", Toast.LENGTH_SHORT).show();
            // Release the player if preparation fails
            releaseMediaPlayer();
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer released.");
        }
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarms",
                    NotificationManager.IMPORTANCE_HIGH
            );
            // Disable the channel's sound since we manage it ourselves.
            channel.setSound(null, null);
            nm.createNotificationChannel(channel);
        }
    }
}
