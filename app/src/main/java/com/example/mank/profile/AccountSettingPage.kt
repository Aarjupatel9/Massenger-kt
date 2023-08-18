package com.example.mank.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.DAoFiles.UserDao
import com.example.mank.MainActivity
import com.example.mank.R

class AccountSettingPage : Activity() {
	private var userDao: UserDao? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_account_settings_page)
		userDao = MainActivity.db?.userDao()
	}

	fun finishAccountSetting(view: View?) {
		finish()
	}

	fun ACSPLogout(view: View?) {
		userDao?.LogOutFromAppForThisUser(MainActivity.user_login_id)
		Toast.makeText(this, "Restart the app after logout", Toast.LENGTH_LONG).show()
		finishAffinity();
		Log.d("log-AccountSettingPage", "After intent creation")
	}

	fun addRecoveryEmailActivityStart(view: View?) {

		val intent = Intent(this, AccountSettingRecoveryEmailPage::class.java)
		startActivity(intent)
		Log.d("log-addRecoveryEmailActivityStart", "After intent creation")
	}
}
