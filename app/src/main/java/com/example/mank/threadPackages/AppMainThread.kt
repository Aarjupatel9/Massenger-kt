package com.example.mank.threadPackages

import com.example.mank.MainActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

class AppMainThread(var toStopAppMainThread: Boolean) : Thread() {
    var timerTask: TimerTask? = null
    var online_status_checker_timer: Timer? = null
    var appConnectedObj: JSONObject

    init {
        appConnectedObj = JSONObject()
        try {
            appConnectedObj.put("user_id", MainActivity.user_login_id)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        if (toStopAppMainThread) {
            online_status_checker_timer!!.cancel()
            online_status_checker_timer!!.purge()
        } else {
            online_status_checker_timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    MainActivity.socket!!.emit("user_app_connected_status", appConnectedObj)
                }
            }
            online_status_checker_timer!!.scheduleAtFixedRate(timerTask, 1, 1000)
        }
    }
}