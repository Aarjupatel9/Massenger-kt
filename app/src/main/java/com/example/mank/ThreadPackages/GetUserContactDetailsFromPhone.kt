package com.example.mank.ThreadPackages

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.MainActivity
import com.example.mank.cipher.MyCipher
import org.json.JSONArray

class GetUserContactDetailsFromPhone(var context: Context, db: MainDatabaseClass) : Thread() {
    var allContactOfUserEntity: AllContactOfUserEntity? = null
    var contactDetails: JSONArray
    var mc: MyCipher
    var db: MainDatabaseClass
    var massegeDao: MassegeDao
    var isCompleted = false

    //tmp

    init {
        mc = MyCipher()
        this.db = db
        massegeDao = db.massegeDao()
        contactDetails = JSONArray()
    }

    override fun run() {
        val contentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val cursor = contentResolver.query(uri, null, null, null, null)
        Log.d(
            "log-GetUserContactDetailsFromPhone",
            "getContacts: total contact is " + cursor!!.count
        )
        if (cursor.count > 0) {
            var counter = 0
            while (cursor.moveToNext()) {
                @SuppressLint("Range") val display_name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                @SuppressLint("Range") var number =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                number = number.replace("\\s".toRegex(), "")
                number = number.replace("-".toRegex(), "")
                number = number.replace("\\)".toRegex(), "")
                number = number.replace("\\(".toRegex(), "")
                if (number.length > 9) {
                    try {
                        if (number[0] == '+') {
                            number = number.substring(3)
                        }
                        allContactOfUserEntity =
                            AllContactOfUserEntity(number.toLong(), display_name, "-1")
                    } catch (e: IndexOutOfBoundsException) {
                        Log.d(
                            "log-GetUserContactDetailsFromPhone",
                            "IndexOutOfBoundsException: for $number || $e"
                        )
                    } catch (e: Exception) {
                        Log.d("log-GetUserContactDetailsFromPhone", "Exception: for $number || $e")
                    }
                    //makeing jsonArray
                    val jsonParam = JSONArray()
                    jsonParam.put(mc.encrypt(counter))
                    jsonParam.put(mc.encrypt(display_name))
                    jsonParam.put(mc.encrypt(number))
                    contactDetails.put(jsonParam)
                    val x = allContactOfUserEntity!!.MobileNumber?.let {
                        massegeDao.getSelectedAllContactOfUserEntity(
                            it, MainActivity.user_login_id
                        )
                    }
                    if (x?.size == 0) massegeDao.addAllContactOfUserEntity(allContactOfUserEntity)
                }
                counter++
            }
        }
        isCompleted = true
    }

}