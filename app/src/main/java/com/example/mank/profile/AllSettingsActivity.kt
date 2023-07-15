package com.example.mank.profile

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.mank.MainActivity
import com.example.mank.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class AllSettingsActivity : Activity() {
    private var username: TextView? = null
    private var aboutInfo: TextView? = null
    private var userProfileImage: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_settings)
        username = findViewById<View>(R.id.ASUsername) as TextView
        aboutInfo = findViewById<View>(R.id.ASAboutInfo) as TextView
        userProfileImage = findViewById<View>(R.id.ASUserProfileImage) as ImageView
    }

    override fun onResume() {
        super.onResume()
        setUserDetails()
    }

    private fun setUserDetails() {
        val ts = Thread(object : Runnable {
            override fun run() {
                synchronized(this) {
                    val massegeDao = MainActivity.db!!.massegeDao()
                    val loginDetailsEntity = massegeDao.loginDetailsFromDatabase
                    runOnUiThread {
                        username!!.text = loginDetailsEntity?.displayUserName
                        aboutInfo!!.text = loginDetailsEntity?.about
                    }
                }

                //setup image
                val imagePath =
                    "/storage/emulated/0/Android/media/com.massenger.mank.main/Pictures/profiles/" + MainActivity.user_login_id + MainActivity.user_login_id + ".png"
                var byteArray: ByteArray? = null
                try {
                    val imageFile = File(imagePath)
                    val fis = FileInputStream(imageFile)
                    val bos = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        bos.write(buffer, 0, bytesRead)
                    }
                    fis.close()
                    bos.close()
                    byteArray = bos.toByteArray()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (byteArray != null) {
                    val selfImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    Log.d(
                        "log-AllSettingsActivity",
                        "setUserImage : after fetch image form file system : " + byteArray.size
                    )
                    runOnUiThread { userProfileImage!!.setImageBitmap(selfImage) }
                }
            }
        })
        ts.start()
    }

    fun SetBbForContactPageLabelOnClick(view: View?) {
        val intent = Intent(this, BgImageSetForContactPage::class.java)
        startActivity(intent)
    }

    fun LaunchUserProfileActivity(view: View?) {
        val intent = Intent(this@AllSettingsActivity, UserProfileActivity::class.java)
        startActivity(intent)
    }

    fun FinishSettingActivity(view: View?) {
        finish()
    }

    fun ProfilePageMainLabelOnClick(view: View?) {
        val intent = Intent(this, UserProfileActivity::class.java)
        startActivity(intent)
    }

    fun ChatsPageLabelOnClick(view: View?) {
//        Intent intent = new Intent(this, UserProfileActivity.class);
//        startActivity(intent);
        Toast.makeText(this@AllSettingsActivity, "this option coming soon...", Toast.LENGTH_SHORT)
            .show()
    }

    fun AccountPageLabelOnClick(view: View?) {
        val intent = Intent(this, AccountSettingPage::class.java)
        startActivity(intent)
        //        Toast.makeText(AllSettingsActivity.this, "this option coming soon...", Toast.LENGTH_SHORT).show();
    }

    fun PrivacyPageLabelOnClick(view: View?) {
//        Intent intent = new Intent(this, UserProfileActivity.class);
//        startActivity(intent);
        Toast.makeText(this@AllSettingsActivity, "this option coming soon...", Toast.LENGTH_SHORT)
            .show()
    }
}