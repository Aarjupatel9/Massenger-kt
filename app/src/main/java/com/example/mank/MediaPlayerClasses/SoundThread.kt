package com.example.mank.MediaPlayerClasses

import android.content.Context
import android.media.MediaPlayer
import com.example.mank.R

class SoundThread(private val context: Context, id: Int) : Thread() {
    var mp: MediaPlayer? = null

    init {
        if (id == 0) {
            mp = MediaPlayer.create(context, R.raw.massege_pop_alert)
        } else if (id == 1) {
            mp = MediaPlayer.create(context, R.raw.massege_pop_pup_notification_alert)
        } else if (id == 10) {
            //long music will play
            mp = MediaPlayer.create(context, R.raw.dil_meri_na_sune)
        }
    }

    override fun run() {
        mp!!.start()
    }

    fun massegePopPlay() {
        mp!!.start()
    }

    fun massegePopStart() {
        mp!!.stop()
    }
}