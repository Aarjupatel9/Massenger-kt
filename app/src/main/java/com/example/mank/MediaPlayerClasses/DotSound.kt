package com.example.mank.MediaPlayerClasses

import android.content.Context
import android.media.MediaPlayer
import com.example.mank.R


class DotSound(private val context: Context, id: Int) {
    var mp: MediaPlayer? = null

    init {
        if (id == 0) {
            mp = MediaPlayer.create(context, R.raw.massege_pop_alert)
        } else if (id == 1) {
            mp = MediaPlayer.create(context, R.raw.massege_pop_pup_notification_alert)
        }
    }

    fun massegePopPlay() {
        mp!!.start()
    }

    fun massegePopStart() {
        mp!!.stop()
    }
}