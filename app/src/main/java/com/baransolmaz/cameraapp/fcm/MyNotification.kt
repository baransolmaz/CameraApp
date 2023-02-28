package com.baransolmaz.cameraapp.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.baransolmaz.cameraapp.R

class MyNotification(
    var context: Context,
    var title:String,
    var msg:String
) {
    private val channelID:String="FCM100"
    private val channelName:String="FCMMessage"
    private val notificationManager=context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    lateinit var notificationChannel : NotificationChannel
    lateinit var notificationBuilder: NotificationCompat.Builder

    fun fireNotification(){
        notificationChannel=NotificationChannel(channelID,channelName,NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
        notificationBuilder = NotificationCompat.Builder(context,channelID)
        notificationBuilder.setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(msg)
            .setAutoCancel(true)
        notificationManager.notify(100,notificationBuilder.build())
    }
}