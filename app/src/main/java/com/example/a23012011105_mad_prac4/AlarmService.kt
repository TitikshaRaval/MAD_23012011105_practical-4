package com.example.a23012011105_mad_prac4

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat

class AlarmServices : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val CHANNEL_ID = "alarm_channel"
    private val NOTIFICATION_ID = 1001

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("Service1")
        when (action) {
            "Start" -> {
                startAsForeground()
                startAlarmSound()
                // Keep service running until explicitly stopped
                return START_STICKY
            }
            "Stop" -> {
                stopAlarmSound()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                // Don't restart after stop
                return START_NOT_STICKY
            }
            else -> {
                // Unknown command; stop service
                stopAlarmSound()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }
    }

    private fun startAsForeground() {
        createNotificationChannelIfNeeded()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm")
            .setContentText("Alarm is ringing")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            )
            // channel sound disabled here because we play the alarm ourselves
            channel.setSound(null, null)
            nm.createNotificationChannel(channel)
        }
    }

    private fun startAlarmSound() {
        if (mediaPlayer?.isPlaying == true) return

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmServices, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to play alarm sound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAlarmSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
        Toast.makeText(this, "Alarm Cancelled!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        stopAlarmSound()
        super.onDestroy()
    }
}