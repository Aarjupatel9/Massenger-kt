package com.example.mank.FunctionalityClasses

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.mank.MainActivity
import com.example.mank.MainActivity.Companion.saveContactProfileImageToStorage
import com.example.mank.R
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Arrays

class ContactDetailsFromMassegeViewPage() : Activity() {
    private var contact_display_name: TextView? = null
    private var contact_about_details: TextView? = null
    private var contact_mobile_number: TextView? = null
    private var imageView: ImageView? = null
    private var CID: String? = null
    private var ContactMobileNumber: Long = 0
    private var ContactName: String? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contact_details_from_massege_view_page)
        val intent = intent
        CID = intent.getStringExtra("CID")
        ContactMobileNumber = intent.getLongExtra("ContactMobileNumber", -2)
        ContactName = intent.getStringExtra("ContactName")
        //we have to fetch data from server
        updateUserDetailsFromServer()
        MainActivity.socket!!.on(
            "getContactDetailsForContactDetailsFromMassegeViewPage_return",
            onGetContactDetailsForContactDetailsFromMassegeViewPage_return
        )
        MainActivity.socket!!.on(
            "updateSingleContactProfileImageToUserProfilePage",
            onUpdateSingleContactProfileImageToUserProfilePage
        )

        contact_display_name = findViewById<View>(R.id.contact_display_name) as TextView
        contact_about_details = findViewById<View>(R.id.contact_about_details) as TextView
        contact_mobile_number = findViewById<View>(R.id.contact_phone_number) as TextView
        imageView = findViewById<View>(R.id.CDFMVP_imageView) as ImageView
        contact_mobile_number!!.text = ContactMobileNumber.toString()
        contact_display_name!!.text = ContactName
        setUpProfileImage()
    }

    private fun updateUserDetailsFromServer() {
        val ts = Thread(object : Runnable {
            override fun run() {
                val massegeDao = MainActivity.db!!.massegeDao()
                val profileImageVersion =
                    massegeDao?.getContactProfileImageVersion(CID, MainActivity.user_login_id)
                Log.d(
                    "log-ContactDetailsFromMassegeViewPage",
                    "image update part : $CID and : $profileImageVersion"
                )
                if (MainActivity.socket != null) {
                    MainActivity.socket!!.emit(
                        "getContactDetailsForContactDetailsFromMassegeViewPage",
                        MainActivity.user_login_id,
                        MainActivity.Contact_page_opened_id,
                        profileImageVersion
                    )
                }
            }
        })
        ts.start()
    }

    private fun setUpProfileImage() {
        val ts = Thread(object : Runnable {
            override fun run() {
                var pass = true
                for (i in MainActivity.contactList!!.indices) {
//                    Log.d("log-ContactDetailsFromMassegeViewPage", "setUpProfileImage : contactList : " + contactList.get(i).CID + " and CID : " + CID);
                    if ((MainActivity.contactList!![i]!!.CID == CID)) {
                        val byteArray = MainActivity.contactList!![i]?.userImage
                        Log.d(
                            "log-ContactDetailsFromMassegeViewPage",
                            "setUpProfileImage : contactList image found " + MainActivity.contactList!![i].toString()
                        )
                        val displayName = MainActivity.contactList!![i]?.DisplayName
                        val about = MainActivity.contactList!![i]?.about
                        var bitmapImage: Bitmap? = null
                        if (byteArray != null) {
                            bitmapImage =
                                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        }
                        val finalBitmapImage = bitmapImage
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                if (finalBitmapImage != null) {
                                    imageView!!.setImageBitmap(finalBitmapImage)
                                }
                                contact_display_name!!.text = displayName
                                contact_about_details!!.text = about
                            }
                        })
                        pass = false
                        break
                    }
                }
                Log.d("log-ContactDetailsFromMassegeViewPage", "setUpProfileImage :pass : $pass")
                if (pass) {
                    val imagePath =
                        "/storage/emulated/0/Android/media/com.massenger.mank.main/Pictures/profiles/" + CID + MainActivity.user_login_id + ".png"
                    var byteArray: ByteArray? = null
                    try {
                        val imageFile = File(imagePath)
                        val fis = FileInputStream(imageFile)
                        val bos = ByteArrayOutputStream()
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        while ((fis.read(buffer).also { bytesRead = it }) != -1) {
                            bos.write(buffer, 0, bytesRead)
                        }
                        fis.close()
                        bos.close()
                        byteArray = bos.toByteArray()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    if (byteArray != null) {
                        Log.d(
                            "log-ContactDetailsFromMassegeViewPage",
                            "setUpProfileImage : after fetch image form file system : " + byteArray.size
                        )
                        val bitmapImage =
                            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        runOnUiThread(object : Runnable {
                            override fun run() {
                                imageView!!.setImageBitmap(bitmapImage)
                            }
                        })
                    }
                }
            }
        })
        ts.start()
    }

    private val onGetContactDetailsForContactDetailsFromMassegeViewPage_return: Emitter.Listener =
        object : Emitter.Listener {
            override fun call(vararg args: Any) {
                Log.d(
                    "log-onCheckContactOnlineStatus_return",
                    "call: onMassegeReachReceiptFromServer enter"
                )
                try {
                    val CID = args[0].toString()
                    val display_name: String? = args[1] as String
                    val contact_about = args[2] as String
                    val ProfileImageUpdatable = args[3].toString().toInt()
                    Log.d("log-onCheckContactOnlineStatus_return", "contact_id: $CID")
                    Log.d("log-onCheckContactOnlineStatus_return", "contact_id: $contact_about")
                    Log.d("log-onCheckContactOnlineStatus_return", "display_name: $display_name")
                    runOnUiThread(object : Runnable {
                        override fun run() {
                            contact_about_details!!.text = contact_about
                            contact_display_name!!.text = display_name
                            if (display_name == null) {
                                contact_display_name!!.text = "not set"
                            }
                        }
                    })
                    if (ProfileImageUpdatable == 1) {
                        val massegeDao = MainActivity.db!!.massegeDao()
                        val profileImageVersion = massegeDao?.getContactProfileImageVersion(
                            CID,
                            MainActivity.user_login_id
                        )
                        val jsonArray = JSONArray()
                        try {
                            val tmp = JSONObject()
                            tmp.put("_id", CID)
                            tmp.put("Number", ContactMobileNumber)
                            tmp.put("ProfileImageVersion", profileImageVersion)
                            jsonArray.put(tmp)
                            Log.d(
                                "log-ContactDetailsFromMassegeViewPage",
                                "image update part : $jsonArray"
                            )
                            MainActivity.socket!!.emit(
                                "updateProfileImages",
                                MainActivity.user_login_id,
                                jsonArray,
                                2
                            )
                        } catch (exception: Exception) {
                            Log.d("log-ContactListAdapter-Exception", exception.toString())
                        }
                    }
                } catch (e: Exception) {
                    Log.d("log-onCheckContactOnlineStatus_return-exception", "Exception arive : $e")
                }
            }
        }
    private val onUpdateSingleContactProfileImageToUserProfilePage: Emitter.Listener =
        object : Emitter.Listener {
            override fun call(vararg args: Any) {
                Log.d(
                    "log-onUpdateSingleContactProfileImage",
                    "onUpdateSingleContactProfileImage || start "
                )
                val userId = args[0].toString()
                val id = args[1].toString()
                val ProfileImageVersion = args[3].toString().toLong()
                val profileImageBase64 = args[2] as String
                try {
                    val profileImageByteArray = Base64.decode(profileImageBase64, Base64.DEFAULT)
                    if (profileImageByteArray.size > 0) {
                        synchronized(this) {
                            val bitmapImage: Bitmap = BitmapFactory.decodeByteArray(
                                profileImageByteArray,
                                0,
                                profileImageByteArray.size
                            )
                            MainActivity.contactListAdapter!!.practiceMethod(
                                id,
                                profileImageByteArray
                            ) // to update contactList
                            if (saveContactProfileImageToStorage(id, profileImageByteArray)) {
                                Log.d(
                                    "log-saveImageToInternalStorage",
                                    "Saved image of size : " + profileImageByteArray.size + " and resolution : " + bitmapImage.getWidth() + "*" + bitmapImage.getHeight()
                                )
                            }
                            runOnUiThread(object : Runnable {
                                override fun run() {
                                    imageView!!.setImageBitmap(bitmapImage)
                                }
                            })
                        }
                    }
                    Log.d(
                        "log-onUpdateSingleContactProfileImage",
                        "ProfileImageVersion : " + ProfileImageVersion + " and for cid : " + id + " bytearray : " + Arrays.toString(
                            profileImageByteArray
                        )
                    )
                } catch (ex: Exception) {
                    Log.d("log-onUpdateSingleContactProfileImage-Exception", ex.toString())
                }
            }
        }
}