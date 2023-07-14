package com.example.mank.ThreadPackages

import android.content.Context
import com.example.mank.MediaPlayerClasses.DotSound

class MassegePopSoundThread(private val context: Context, private val id: Int) : Thread() {
    override fun run() {
        val ma = DotSound(context, id)
        ma.massegePopPlay()
    }
}