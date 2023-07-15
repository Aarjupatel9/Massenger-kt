package com.example.mank

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.room.Room.databaseBuilder
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.loginDetailsEntity
import com.example.mank.cipher.MyCipher
import com.example.mank.configuration.GlobalVariables
import com.example.mank.configuration.permissionMain
import com.example.mank.configuration.permission_code
import com.example.mank.R
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LoginActivity : Activity() {
	var mc = MyCipher()
	var userPasswordInLogin: EditText? = null
	var userMobileNumberInLogin: EditText? = null
	var userNameInRegister: EditText? = null
	var userMobileNumberInRegister: EditText? = null
	var userPass1InRegister: EditText? = null
	var userPass2InRegister: EditText? = null
	var login: Button? = null
	var signUp: Button? = null
	private var loadingPB: ProgressBar? = null
	private var massegeBox: TextView? = null
	private var registerRedirectLink: TextView? = null
	private var loginRedirectLink: TextView? = null
	override fun onDestroy() {
		super.onDestroy()
		Log.d("log-onDestroy", "onDestroy: in LoginActivity")
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d("log-not logined", "onCreate: inside Oncreate")
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login_page_master)
		massegeBox = findViewById(R.id.massegeBoxInLogin)
		loadingPB = findViewById(R.id.LoadingPBOfLoginPage)
		if (!permissionMain.hasPermissions(this, *permission_code.CONTACT_STORAGE_PERMISSION)) {
			ActivityCompat.requestPermissions(this, permission_code.PERMISSIONS, permission_code.PERMISSION_CONTACT_SYNC)
		} else {
			loginRedirect()
		}
	}

	@SuppressLint("ResourceAsColor")
	private fun registerRedirect() {
		val includeLayout = findViewById<ViewGroup>(R.id.includeInLoginMaster)
		val newLayout = LayoutInflater.from(this).inflate(R.layout.activity_login_page_register, null)
		includeLayout.removeAllViews()
		includeLayout.addView(newLayout)
		loginRedirectLink = findViewById(R.id.loginRedirectLink)
		signUp = findViewById(R.id.cirRegisterButton)
		userNameInRegister = findViewById(R.id.userNameInRegister)
		userMobileNumberInRegister = findViewById(R.id.userMobileNumberInRegister)
		userPass1InRegister = findViewById(R.id.userPass1InRegister)
		userPass2InRegister = findViewById(R.id.userPass2InRegister)
		signUp?.setOnClickListener(View.OnClickListener {
			val user_password1 = userPass1InRegister?.getText().toString()
			val user_password2 = userPass2InRegister?.getText().toString()
			val user_number = userMobileNumberInRegister?.getText().toString()
			val user_name = userNameInRegister?.getText().toString()
			Log.d("log-signUpButton hit ", "$user_number and $user_password1")
			if (user_name.isEmpty()) {
				massegeBox!!.text = "please enter your name"
				massegeBox!!.setTextColor(R.color.MassegeBoxWarning)
				return@OnClickListener
			}
			if (user_name.length < 2) {
				massegeBox!!.text = "user name should be at least 3 character long "
				massegeBox!!.setTextColor(R.color.MassegeBoxWarning)
				return@OnClickListener
			}
			if (user_number.length < 10) {
				massegeBox!!.text = "please enter valid mobile number"
				massegeBox!!.setTextColor(R.color.MassegeBoxWarning)
				return@OnClickListener
			}
			if (user_password1.length == 0) {
				massegeBox!!.text = "please enter password"
				massegeBox!!.setTextColor(R.color.MassegeBoxWarning)
				return@OnClickListener
			}
			if (user_password1 != user_password2) {
				massegeBox!!.text = "please enter same password"
				massegeBox!!.setTextColor(R.color.MassegeBoxWarning)
				return@OnClickListener
			}
			loadingPB!!.visibility = View.VISIBLE
			SignUp(user_number, user_password1, user_name)
		})
		loginRedirectLink?.setOnClickListener(View.OnClickListener { loginRedirect() })
	}

	private fun loginRedirect() {
		val includeLayout = findViewById<ViewGroup>(R.id.includeInLoginMaster)
		val newLayout = LayoutInflater.from(this).inflate(R.layout.activity_login_page_login, null)
		includeLayout.removeAllViews()
		includeLayout.addView(newLayout)
		login = findViewById(R.id.cirLoginButton)
		registerRedirectLink = findViewById(R.id.registerRedirectLink)
		userMobileNumberInLogin = findViewById(R.id.userMobileNumberInLogin)
		userPasswordInLogin = findViewById(R.id.userPasswordInLogin)
		login?.setOnClickListener(View.OnClickListener {
			val user_password = userPasswordInLogin?.getText().toString()
			val user_number = userMobileNumberInLogin?.getText().toString()
			Log.d("log-loginButton hit ", "$user_number and $user_password")
			if (user_number.length < 10) {
				massegeBox!!.text = "please enter valid phone number"
				return@OnClickListener
			}
			if (user_password.length == 0) {
				massegeBox!!.text = "please enter your password"
				return@OnClickListener
			}
			checkHaveToRegister(user_number, user_password)
		})
		registerRedirectLink?.setOnClickListener(View.OnClickListener { registerRedirect() })
	}

	private fun checkHaveToRegister(user_number: String, user_password: String) {
		loadingPB!!.visibility = View.VISIBLE // creating a new variable for our request queue
		val endpoint = url + "checkHaveToRegister"
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
					when (status) {
						"1" -> {
							val user_id = respObj.getString("user_id") as String
							var displayName = ""
							var about = ""
							var ProfileImageVersion: Long = 0
							var profileImageBase64: String? = null
							try {
								displayName = respObj.getString("displayName") as String
								about = respObj.getString("about") as String
								ProfileImageVersion = respObj.getString("ProfileImageVersion").toString().toLong()
								profileImageBase64 = respObj.getString("ProfileImage") as String
								val profileImageByteArray = Base64.decode(profileImageBase64, Base64.DEFAULT)
								if (profileImageByteArray.size > 0) {
									synchronized(this) {
										val bitmapImage = BitmapFactory.decodeByteArray(profileImageByteArray, 0, profileImageByteArray.size)
										Log.d("log-loginActivity", "Saved image of size : " + profileImageByteArray.size + " and resolution : " + bitmapImage.width + "*" + bitmapImage.height)
										saveContactProfileImageToStorage(user_id, bitmapImage)
									}
								}
							} catch (e: Exception) {
								Log.d("log-LoginActivity ", "expected image not found at login time : $e")
							}
							massegeBox!!.setTextColor(R.color.MassegeBoxSuccess)
							massegeBox!!.text = "Login successful"
							login(user_number, user_password, user_id, displayName, about, ProfileImageVersion)
						}

						"2" -> {
							massegeBox!!.setTextColor(R.color.MassegeBoxWarning)
							massegeBox!!.text = "You have to register with this number, first!!"
						}

						"0" -> {
							massegeBox!!.setTextColor(R.color.MassegeBoxAlert)
							massegeBox!!.text = "Wrong password"
						}
					}
				} catch (e: JSONException) {
					e.printStackTrace()
					Log.d("log-error", "onResponse: err in try bracet : $e")
				}
			}
		}, Response.ErrorListener { error ->
			loadingPB!!.visibility = View.GONE

			val conMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
			val x: Boolean = conMgr.activeNetworkInfo != null && conMgr.activeNetworkInfo!!.isAvailable && conMgr.activeNetworkInfo!!.isConnected
			if (x) { // Network is available, proceed with the HTTP request
				Toast.makeText(this@LoginActivity, "Server side error :  $error", Toast.LENGTH_SHORT).show()
			} else { // Network is not available, display a Toast message
				Toast.makeText(this@LoginActivity, "Network is not available.", Toast.LENGTH_SHORT).show()
			}

		}) {
			override fun getParams(): Map<String, String>? {
				val params: MutableMap<String, String> = HashMap()
				params["number"] = mc.encrypt(user_number)
				params["password"] = mc.encrypt(user_password)
				return params
			}

			override fun getHeaders(): Map<String, String> {
				val headers: MutableMap<String, String> = HashMap()
				Log.d("log-LoginActivity", "apiKey : " + MainActivity.API_SERVER_API_KEY)
				headers["api_key"] = MainActivity.API_SERVER_API_KEY!!
				return headers
			}
		}
		requestQueue.add(request)
	}

	fun login(user_number: String, userPassword: String?, user_id: String, displayName: String?, about: String?, ProfileImageVersion: Long
	) { //here we are storing login details to local database
		Log.d("LoginActivity", "login method start")
		val number = user_number.toLong()
		val login_details = loginDetailsEntity(user_id, userPassword!!, number, displayName, about)
		Log.d("log-reached", "before db initialize : " + login_details.MobileNumber + " qnd " + login_details.Password)
		val db = databaseBuilder(applicationContext, MainDatabaseClass::class.java, "MassengerDatabase").allowMainThreadQueries().build()
		Log.d("log-reached", "after db initialize")
		val massegeDao = db.massegeDao()
		massegeDao.SaveLoginDetailsInDatabase(login_details)
		Log.d("log-login details saved", "after saved Details")
		val returnIntent = Intent()
		returnIntent.putExtra("result", user_id)
		setResult(RESULT_OK, returnIntent)
		finish()
	}

	fun ResetPassword(view: View?) {
		Log.d("log-register", "ResetPassword : in method enter")
		setContentView(R.layout.activity_app_reset_password)
	}

	private fun SignUp(user_number: String, user_password: String, user_name: String) {
		loadingPB!!.visibility = View.VISIBLE // creating a new variable for our request queue
		val endpoint = url + "RegisterNewUser"
		Log.d("log-e", endpoint)
		val requestQueue = Volley.newRequestQueue(this)
		val request: StringRequest = @SuppressLint("ResourceAsColor") object : StringRequest(Method.POST, endpoint, Response.Listener { response ->
			loadingPB!!.visibility = View.GONE
			try {
				val respObj = JSONObject(response)
				val status = respObj.getString("status")
				Log.d("log-response-status", status)
				if (status == "1") {
					massegeBox!!.text = "SighUp is successFull! Login with your account"
					massegeBox!!.setTextColor(R.color.MassegeBoxSuccess) //                        loginRedirect();
				} else if (status == "2") {
					massegeBox!!.text = "server error!!! please try again later"
					massegeBox!!.setTextColor(R.color.MassegeBoxAlert)
					Log.d("log-e", "server error ")
				} else if (status == "0") {
					massegeBox!!.setTextColor(R.color.MassegeBoxSuccess)
					massegeBox!!.text = "You have already an account with this phone number"
				} else {
					massegeBox!!.text = "enter in else condition"
					massegeBox!!.setTextColor(R.color.MassegeBoxAlert)
				}
			} catch (e: JSONException) {
				e.printStackTrace()
				Log.d("log-error", "onResponse: err in try bracet : $e")
				massegeBox!!.text = "enter in catch block"
			}
		}, Response.ErrorListener { error ->
			loadingPB!!.visibility = View.GONE
			massegeBox!!.text = "Server side error :  $error"
			massegeBox!!.setTextColor(R.color.MassegeBoxAlert)
		}) {
			override fun getParams(): Map<String, String>? {
				val params: MutableMap<String, String> = HashMap()
				params["number"] = mc.encrypt(user_number)
				params["password"] = mc.encrypt(user_password)
				params["name"] = mc.encrypt(user_name)
				return params
			}

			override fun getHeaders(): Map<String, String> {
				val headers: MutableMap<String, String> = HashMap()
				headers["api_key"] = MainActivity.API_SERVER_API_KEY!!
				return headers
			}
		}
		requestQueue.add(request)
	}

	private fun saveContactProfileImageToStorage(id: String, bitmapImage: Bitmap) {
		val directory = File(Environment.getExternalStorageDirectory(), "Android/media/com.massenger.mank.main/Pictures/Profiles")

		// Create the directory if it doesn't exist
		if (!directory.exists()) {
			val x = directory.mkdirs()
		}

		// Create the file path
		val imagePath = File(directory, "" + MainActivity.user_login_id + MainActivity.user_login_id + ".png")

		// Save the bitmap image to the file
		try {
			FileOutputStream(imagePath).use { outputStream ->
				bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
				outputStream.flush()
			}
		} catch (e: IOException) {
			e.printStackTrace()
			Log.d("log-saveImageToInternalStorage", "Image Save failed $e")
		} // Print the absolute path of the saved image
		Log.d("log-saveImageToInternalStorage", "Saved image path: " + imagePath.absolutePath)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == permission_code.PERMISSION_CONTACT_SYNC) {
			if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				loginRedirect()
			} else {
				Toast.makeText(this@LoginActivity, "To use Massenger please give Contact and Storage permission", Toast.LENGTH_SHORT).show()
			}
		}
	}

	companion object {
		//    private static final String url = "http://192.168.43.48:10000/";
		private const val url = GlobalVariables.URL_MAIN
	}
}