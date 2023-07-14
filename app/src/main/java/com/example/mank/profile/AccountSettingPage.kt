package com.example.mank.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.MainActivity
import com.example.mank.R

class AccountSettingPage : Activity() {
    private var massegeDao: MassegeDao? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings_page)
        massegeDao = MainActivity.db!!.massegeDao()
    }

    fun finishAccountSetting(view: View?) {
        finish()
    }

    fun ACSPLogout(view: View?) {
        massegeDao!!.LogOutFromAppForThisUser(MainActivity.user_login_id)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        Log.d("log-AccountSettingPage", "After intent creation")
    }
}