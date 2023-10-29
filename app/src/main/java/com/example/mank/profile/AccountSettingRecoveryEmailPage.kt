package com.example.mank.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mank.localDatabaseFiles.daoClasses.UserDao
import com.example.mank.MainActivity
import com.example.mank.R
import com.example.mank.configuration.GlobalVariables.URL_MAIN
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class AccountSettingRecoveryEmailPage : Activity() {
	private var userDao: UserDao? = null
	private var loadingPB: ProgressBar? = null
	private var otpLayoutShow: Boolean = false
	private var ASHEmailAddressOtp: ConstraintLayout? = null
	var countDownTimer: CountDownTimer? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_account_settings_page_recovery_email_edit_)
		loadingPB = findViewById(R.id.AASPREELoadingAnimation)
		ASHEmailAddressOtp = findViewById<ConstraintLayout>(R.id.ASHEmailAddressOtp)
		userDao = MainActivity.db!!.userDao()
		val handler = Handler()


		val loginDetailsEntity = userDao?.loginDetailsFromDatabase
		if (loginDetailsEntity != null) {
			findViewById<EditText>(R.id.ASHEmailAddressField).setText(loginDetailsEntity.es1)
			if (loginDetailsEntity.elf1 == 1L) {
				findViewById<ConstraintLayout>(R.id.ASHEmailAddressVerifyField).visibility = View.VISIBLE
			}
		}

		findViewById<Button>(R.id.ASHEmailAddressSendOtpButton).setOnClickListener {
			sendOtp()
			findViewById<Button>(R.id.ASHEmailAddressSendOtpButton).isEnabled = false //			val delayMillis: Long = 30000

			val delayMillis: Long = 30000
			val intervalMillis: Long = 1000

			countDownTimer = object : CountDownTimer(delayMillis, intervalMillis) {
				@SuppressLint("SetTextI18n")
				override fun onTick(millisUntilFinished: Long) {
					val secondsRemaining = millisUntilFinished / 1000

					Log.d("log-AccountSettingRecoveryEmailPage", "countDownTimer onTick : $secondsRemaining")
					findViewById<Button>(R.id.ASHEmailAddressSendOtpButton).text = "Retry in $secondsRemaining seconds"
				}

				@SuppressLint("CutPasteId")
				override fun onFinish() {
					findViewById<Button>(R.id.ASHEmailAddressSendOtpButton).isEnabled = true
					findViewById<Button>(R.id.ASHEmailAddressSendOtpButton).text = "Re Send Otp"
				}
			}
			countDownTimer?.start()

		}
		findViewById<Button>(R.id.ASHEmailAddressVerifyOtpButton).setOnClickListener {
			verifyOtp()
			findViewById<Button>(R.id.ASHEmailAddressVerifyOtpButton).isEnabled = false
		}

		findViewById<TextView>(R.id.ASHEmailAddressSendOtpAlreadyButton).setOnClickListener { showOtpArea() }

	}

	fun finishAccountSettingRecoveryEmail(view: View?) {
		finish()
	}

	private fun isValidEmail(email: String): Boolean {
		val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
		return email.matches(emailRegex.toRegex())
	}

	private fun sendOtp() {
		val emailFieldValue: String = findViewById<EditText>(R.id.ASHEmailAddressField).text.toString()
		if (!isValidEmail(emailFieldValue)) {
			Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
			return
		}
		loadingPB!!.visibility = View.VISIBLE // creating a new variable for our request queue
		val endpoint = URL_MAIN + "RecoveryEmailOtpSend"
		Log.d("log-e", endpoint)


		val requestQueue = Volley.newRequestQueue(this)
		val request: StringRequest = object : StringRequest(Method.POST, endpoint, object : Response.Listener<String?> {
			@SuppressLint("ResourceAsColor")
			override fun onResponse(response: String?) {
				loadingPB!!.visibility = View.GONE
				try {
					val respObj = JSONObject(response)
					val status = respObj.getString("status")
					Log.d("log-response-status", status)
					if (status == "1") {
						showOtpArea()
						Toast.makeText(this@AccountSettingRecoveryEmailPage, "Otp successfully sent", Toast.LENGTH_SHORT).show()
					} else if (status == "5") {
						Toast.makeText(this@AccountSettingRecoveryEmailPage, "This email already register with other account", Toast.LENGTH_SHORT).show()
					} else {
						Toast.makeText(this@AccountSettingRecoveryEmailPage, "Error while sending otp", Toast.LENGTH_SHORT).show()
					}
				} catch (e: JSONException) {
					e.printStackTrace()
					Log.d("log-error", "onResponse: err in try bracet : $e")
				}
			}
		}, Response.ErrorListener { error ->
			loadingPB!!.visibility = View.GONE
			otpLayoutShow = true;
			val conMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
			val x: Boolean = conMgr.activeNetworkInfo != null && conMgr.activeNetworkInfo!!.isAvailable && conMgr.activeNetworkInfo!!.isConnected
			if (x) { // Network is available, proceed with the HTTP request
				Toast.makeText(this@AccountSettingRecoveryEmailPage, "Server side error :  $error", Toast.LENGTH_SHORT).show()
			} else { // Network is not available, display a Toast message
				Toast.makeText(this@AccountSettingRecoveryEmailPage, "Network is not available.", Toast.LENGTH_SHORT).show()
			}

		}) {
			override fun getParams(): Map<String, String>? {
				val params: MutableMap<String, String> = HashMap()
				params["email"] = emailFieldValue
				params["id"] = MainActivity.user_login_id.toString()
				Log.d("log-AccountSettingRecoveryEmailPage", "RecoveryEmailOtpSend || enter in getParam")
				return params
			}

			override fun getHeaders(): Map<String, String> {
				val headers: MutableMap<String, String> = HashMap()
				headers["api_key"] = MainActivity.API_SERVER_API_KEY!!
				return headers
			}
		}
		request.retryPolicy = DefaultRetryPolicy(TimeUnit.SECONDS.toMillis(20).toInt(), 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
		requestQueue.add(request)

	}

	private fun verifyOtp() {
		val otpFieldValue: String = findViewById<EditText>(R.id.ASHEmailAddressVerifyOtp).text.toString()
		val emailFieldValue: String = findViewById<EditText>(R.id.ASHEmailAddressField).text.toString()
		if (!isValidEmail(emailFieldValue)) {
			Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
			return
		}
		if (otpFieldValue.length != 6) {
			Toast.makeText(this, "Enter valid OTP, $otpFieldValue", Toast.LENGTH_SHORT).show()
			return
		}
		loadingPB!!.visibility = View.VISIBLE // creating a new variable for our request queue
		val endpoint = URL_MAIN + "RecoveryEmailOtpVerify"
		Log.d("log-e", endpoint)

		val requestQueue = Volley.newRequestQueue(this)
		val request: StringRequest = object : StringRequest(Method.POST, endpoint, Response.Listener<String?> { response ->
			loadingPB!!.visibility = View.GONE
			findViewById<Button>(R.id.ASHEmailAddressVerifyOtpButton).isEnabled = true

			try {
				val respObj = JSONObject(response)
				val status = respObj.getString("status")
				Log.d("log-response-status", status)
				when (status) {
					"1" -> {
						handleEmailVerificationSuccess(respObj)
						hideOtpArea()
						Toast.makeText(this@AccountSettingRecoveryEmailPage, "Email is verified ", Toast.LENGTH_SHORT).show()
					}

					"0" -> {
						handleEmailVerificationSuccess(respObj)
						Toast.makeText(this@AccountSettingRecoveryEmailPage, "wrong otp", Toast.LENGTH_SHORT).show()
					}

					else -> {
						Toast.makeText(this@AccountSettingRecoveryEmailPage, "error while verifying email", Toast.LENGTH_SHORT).show()
					}
				}
			} catch (e: JSONException) {
				e.printStackTrace()
				Log.d("log-error", "onResponse: err in try bracet : $e")
			}
		}, Response.ErrorListener { error ->
			loadingPB!!.visibility = View.GONE
			findViewById<Button>(R.id.ASHEmailAddressVerifyOtpButton).isEnabled = true

			otpLayoutShow = true;
			val conMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
			val x: Boolean = conMgr.activeNetworkInfo != null && conMgr.activeNetworkInfo!!.isAvailable && conMgr.activeNetworkInfo!!.isConnected
			if (x) { // Network is available, proceed with the HTTP request
				Toast.makeText(this@AccountSettingRecoveryEmailPage, "Server side error :  $error", Toast.LENGTH_SHORT).show()
			} else { // Network is not available, display a Toast message
				Toast.makeText(this@AccountSettingRecoveryEmailPage, "Network is not available.", Toast.LENGTH_SHORT).show()
			}

		}) {
			override fun getParams(): Map<String, String>? {
				val params: MutableMap<String, String> = HashMap()
				params["email"] = emailFieldValue
				params["otp"] = otpFieldValue
				params["id"] = MainActivity.user_login_id.toString()
				Log.d("log-AccountSettingRecoveryEmailPage", "RecoveryEmailOtpSend || enter in getParam")
				return params
			}

			override fun getHeaders(): Map<String, String> {
				val headers: MutableMap<String, String> = HashMap()
				headers["api_key"] = MainActivity.API_SERVER_API_KEY!!
				return headers
			}
		}
		request.retryPolicy = DefaultRetryPolicy(TimeUnit.SECONDS.toMillis(20).toInt(), 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
		requestQueue.add(request)

	}


	private fun showOtpArea() {
		ASHEmailAddressOtp?.visibility = View.VISIBLE; }

	private fun hideOtpArea() {
		ASHEmailAddressOtp?.visibility = View.GONE; }

	private fun handleEmailVerificationSuccess(respObj: JSONObject) {
		try {
			val email = respObj.getString("email")

			userDao?.updateUserLoginRecoveryEmail(email, 1L, MainActivity.user_login_id.toString())
			findViewById<ConstraintLayout>(R.id.ASHEmailAddressVerifyField).visibility = View.VISIBLE

		} catch (e: JSONException) {
			e.printStackTrace()
			Log.d("log-error", "onResponse: err in try bracet : $e")
		}
	}

}