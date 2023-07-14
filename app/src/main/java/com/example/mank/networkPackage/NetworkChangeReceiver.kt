package com.example.mank.networkPackage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("log-NetworkChangeReceiver", "onReceive run start")
        val status = NetworkUtil.getConnectivityStatusString(context)
        Log.d("log-NetworkChangeReceiver", "status:$status")
        if ("android.net.conn.CONNECTIVITY_CHANGE" == intent.action) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
//                new ForceExitPause(context).execute();
                Log.d("log-NetworkChangeReceiver", "onReceive: network is not connected")
            } else {
//                new ResumeForceExitPause(context).execute();
                Log.d("log-NetworkChangeReceiver", "onReceive: network is  connected")
            }
        }
    }
}