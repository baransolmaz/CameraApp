package com.baransolmaz.cameraapp.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    private val TAG= "FCMTOKEN"

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            val notice=
                MyNotification(applicationContext,
                it.title.toString(),
                it.body.toString())
            notice.fireNotification()
        }
    }

}