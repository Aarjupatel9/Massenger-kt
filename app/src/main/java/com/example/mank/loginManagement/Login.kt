package com.example.mank.loginManagement

import android.util.Log
import com.example.mank.localDatabaseFiles.dataContainerClasses.holdLoginData
import com.example.mank.localDatabaseFiles.MainDatabaseClass

class Login {
    fun isLogIn(db: MainDatabaseClass?): Int {
        Log.d("log-login-check method", "myfilereadmethod: enter in logIned check  method")
        val hold_LoginData = holdLoginData()
        val dataFromDatabase = hold_LoginData.data
        if (dataFromDatabase != null) {
            Log.d(
                "log-in login check method  database data is ",
                " : " + dataFromDatabase.Password + " qnd " + dataFromDatabase.MobileNumber
            )
            var l = 0
            var num = dataFromDatabase.MobileNumber
            while (num != 0L) {
                if (num != null) {
                    num = num / 10
                }
                l++
            }
            Log.d("log-in login check method  database data is ", " : $l")
            return if (l == 10) {
                1
            } else {
                0
            }
        }
        Log.d("log-login-check method", " not logIned cond. ")
        return 0
    }

    fun getUserLoginId(db: MainDatabaseClass?): Int {
        Log.d("log-login-check method", "myfilereadmethod: enter in logIned check  method")
        val hold_LoginData = holdLoginData()
        val dataFromDatabase = hold_LoginData.data
        if (dataFromDatabase != null) {
            Log.d(
                "log-in login check method  database data is ",
                " : " + dataFromDatabase.Password + " qnd " + dataFromDatabase.MobileNumber
            )
            val user_login_id = dataFromDatabase.UID
        }
        Log.d("log-login-check method", " not logIned cond. ")
        return 0
    }
}