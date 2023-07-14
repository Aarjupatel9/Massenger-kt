package com.example.mank

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mank.cipher.MyCipher
import com.example.mank.configuration.GlobalVariables
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    var nameOfUserRegister: EditText? = null
    var userPhoneRegister: EditText? = null
    var userPassword1: EditText? = null
    var userPassword2: EditText? = null
    var register: Button? = null
    private var loadingPB: ProgressBar? = null
    var mc = MyCipher()
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("log-not logined", "onCreate: inside Oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registeractivity_main)
        nameOfUserRegister = findViewById(R.id.nameOfUserRegister)
        userPhoneRegister = findViewById(R.id.userPhoneRegister)
        userPassword1 = findViewById(R.id.userPassword1)
        userPassword2 = findViewById(R.id.userPassword2)
        register = findViewById(R.id.register_button)
        loadingPB = findViewById(R.id.LoadingPBOfLoginPage)
        register?.setOnClickListener(View.OnClickListener {
            val user_password1 = userPassword1?.getText().toString()
            val user_password2 = userPassword2?.getText().toString()
            val user_name = nameOfUserRegister?.getText().toString()
            val user_number = userPhoneRegister?.getText().toString()
            if (user_name == "" || user_name.length < 2) {
                Toast.makeText(
                    this@RegisterActivity,
                    "user name can not be empty Or less than 2 Charcator please enter appropriate user name",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (user_number.length != 10) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Please enter proper phone number",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (user_password1 == user_password2) {
                Log.d("log-loginbutton hit ", "$user_number and $user_password1")
                if (user_password1 != "") {
                    Register(user_number, user_password1, user_name)
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "password can not be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@RegisterActivity,
                    "Please enter both password same",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun Register(user_number: String, user_password: String, user_name: String) {
        loadingPB!!.visibility = View.VISIBLE
        // creating a new variable for our request queue
        val endpoint = url + "RegisterNewUser"
        Log.d("log-e", endpoint)
        val requestQueue = Volley.newRequestQueue(this)
        val request: StringRequest =
            object : StringRequest(Method.POST, endpoint, Response.Listener { response ->
                loadingPB!!.visibility = View.GONE
                Toast.makeText(
                    this@RegisterActivity,
                    "Login succsesfull enjoy it!",
                    Toast.LENGTH_SHORT
                ).show()
                try {
                    val respObj = JSONObject(response)
                    val status = respObj.getString("status")
                    Log.d("log-response-status", status)
                    if (status == "1") {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Now You Can Login With Your User Acoount... Thanks For Signup In Massenger",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else if (status == "2") {
                        Toast.makeText(
                            this@RegisterActivity,
                            "server error!!! please try again later",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("log-e", "server error ")
                    } else if (status == "0") {
                        Toast.makeText(this@RegisterActivity, "Wrong Password!!", Toast.LENGTH_LONG)
                            .show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.d("log-error", "onResponse: err in try bracet : $e")
                }
            }, Response.ErrorListener { error ->
                loadingPB!!.visibility = View.GONE
                Toast.makeText(
                    this@RegisterActivity,
                    "Server side error :  $error",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    params["number"] = mc.encrypt(user_number)
                    params["password"] = mc.encrypt(user_password)
                    params["name"] = mc.encrypt(user_name)
                    return params
                }
            }
        requestQueue.add(request)
    }

    companion object {
        //    private static final String url = "http://192.168.43.48:10000/";
        private const val url = GlobalVariables.URL_MAIN
    }
}