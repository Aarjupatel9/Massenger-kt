package com.example.mank.socket

import android.util.Log
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.holdLoginData
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.MainActivity
import com.example.mank.configuration.GlobalVariables
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException
import java.util.Collections

class SocketClass(db: MainDatabaseClass?) {
    var socket: Socket? = null

    init {
        val hold_LoginData = holdLoginData()
        val userDetails = hold_LoginData.data
        if (userDetails != null) {
            val arr = arrayOf(MainActivity.API_SERVER_API_KEY, userDetails.UID)
            val options = IO.Options.builder()
                .setAuth(
                    Collections.singletonMap(
                        "token",
                        MainActivity.API_SERVER_API_KEY + userDetails.UID
                    )
                )
                .setPath("/socket.io/")
                .build()
            try {
                socket = IO.socket(GlobalVariables.SOCKET_URL, options)
            } catch (e: URISyntaxException) {
                Log.d(
                    "log-HomePageWithContactActivity-SocketClass",
                    "Exception error connecting to socket: $e"
                )
            }
            socket!!.connect()
        }
    }

    fun joinRoom(user_login_id: String?) {
        if (socket != null) {
            socket?.emit("join", user_login_id)
        }
    }
}