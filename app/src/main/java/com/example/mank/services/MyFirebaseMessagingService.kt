package com.example.mank.services

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import com.example.mank.ContactMassegeDetailsView
import com.example.mank.functionalityClasses.MyStaticFunctions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("log-MyFirebaseMessagingService", "notification From: " + remoteMessage.from)
        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.d("log-MyFirebaseMessagingService", "Message data payload: " + remoteMessage.data)
            if (remoteMessage.data["massege_type"] == "1") {
                val massege_from = remoteMessage.data["massege_from"]
                val massegeOBJ = remoteMessage.data["massegeOBJ"]
                //                Log.d("log-MyFirebaseMessagingService", "massegeOBJ: " + massegeOBJ);
                MyStaticFunctions.showPushNotification(
                    this,
                    "Massenger",
                    "massege form $massege_from",
                    Intent(this, ContactMassegeDetailsView::class.java),
                    201
                )
            } else {
                Log.d("log-MyFirebaseMessagingService", "call_type is other than 1")
            }
        }
        val notification = remoteMessage.notification
        if (notification != null) {
            val title = notification.title
            val body = notification.body
            Log.d("log-MyFirebaseMessagingService", "notification :  title-$title and body-$body")
        }
    }
}