package com.example.mank.profile

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.mank.R
import java.io.IOException

class SettingsOptionPage : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activtity_settings_option_page)
    }

    fun ProfilePageMainLabelOnClick(view: View?) {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
    }

    fun SetBbForContactPageLabelOnClick(view: View?) {
        val intent = Intent(this, BgImageSetForContactPage::class.java)
        startActivity(intent)
    }

    private var recorder: MediaRecorder? = null
    private val player: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    fun startRecordingButton(view: View?) {

//        File MyFile = new File(ge);
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder!!.setOutputFile(fileName)
        //        recorder.setOutputFile(MyFile);
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            Log.d("log-SettingsOptionPage", "prepare() failed e:$e")
        }
        recorder!!.start()
    }

    private fun stopRecordingButton(view: View) {
        recorder!!.stop()
        recorder!!.release()
        recorder = null
    }

    companion object {
        private const val LOG_TAG = "AudioRecordTest"
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private val fileName: String? = null
    }
}