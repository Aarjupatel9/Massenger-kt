package com.example.mank.ThreadPackages

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.MainActivity
import com.example.mank.cipher.MyCipher
import com.example.mank.configuration.GlobalVariables
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.TreeSet

class SyncContactDetailsThread(
    private val context: Context,
    connectedContact: List<AllContactOfUserEntity?>?,
    disConnectedContact: List<AllContactOfUserEntity?>?,
    callback: IContactSync
) : Thread() {
    private var type = 0
    private var allContactOfUserEntity: AllContactOfUserEntity? = null
    private var allContactOfUserEntityList: MutableList<AllContactOfUserEntity?> = ArrayList()
    private val connectedContact: List<AllContactOfUserEntity?>?
    private val disConnectedContact: List<AllContactOfUserEntity?>?
    private val massegeDao: MassegeDao
    private val mc = MyCipher()
    private val callback: IContactSync
    fun setFromWhere(type: Int) {
        this.type = type
    }

    val contactComparator = Comparator<AllContactOfUserEntity> { contact1, contact2 ->
        try {
            val displayName1 = contact1.DisplayName ?: ""
            val displayName2 = contact2.DisplayName ?: ""
            displayName1.compareTo(displayName2, ignoreCase = true)
        } catch (e: Exception) {
            Log.d("log-AllContactOfUserDeviceView", "Exception in comparator: $e")
            1
        }
    }

    init {
        massegeDao = MainActivity.db!!.massegeDao()
        this.connectedContact = connectedContact
        this.disConnectedContact = disConnectedContact
        this.callback = callback
    }

    override fun run() {
        synchronized(this) {
            val y = MainActivity.user_login_id
            val mainArray = JSONArray()
            val ContactDetails = JSONArray()
            val endpoint = GlobalVariables.URL_MAIN + "syncContactOfUser"
            val requestQueue = Volley.newRequestQueue(context)
            val contentResolver = context.contentResolver
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val cursor = contentResolver.query(uri, null, null, null, null)
            Log.d("log-SyncContactDetailsThread", "getContacts: total contact is " + cursor!!.count)
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
                                "log-SyncContactDetailsThread",
                                "IndexOutOfBoundsException: for $number || $e"
                            )
                        } catch (e: Exception) {
                            Log.d("log-SyncContactDetailsThread", "Exception: for $number || $e")
                        }
                        val jsonParam = JSONArray()
                        jsonParam.put(mc.encrypt(counter))
                        jsonParam.put(mc.encrypt(display_name))
                        jsonParam.put(number)
                        ContactDetails.put(jsonParam)
                        if (number == "1111111111") {
                            Log.d("log-SyncContactDetailsThread-D1", "D1 found : $jsonParam")
                        }
                        allContactOfUserEntityList.add(allContactOfUserEntity)
                    }
                    counter++
                }
            }
//            val uniqueContacts: MutableSet<AllContactOfUserEntity?> = TreeSet(contactComparator)
//            uniqueContacts.addAll(allContactOfUserEntityList)
//            allContactOfUserEntityList = ArrayList(uniqueContacts)


            val uniqueContacts = TreeSet(contactComparator)
            uniqueContacts.addAll(allContactOfUserEntityList)
            allContactOfUserEntityList = ArrayList(uniqueContacts)


            val tx = Thread(object : Runnable {
                override fun run() {
                    synchronized(this) {
                        for (entity in allContactOfUserEntityList) {
                            val x = entity!!.MobileNumber?.let {
                                massegeDao.getSelectedAllContactOfUserEntity(
                                    it, MainActivity.user_login_id
                                )
                            }
                            if (x?.size == 0) {
                                massegeDao.addAllContactOfUserEntity(entity)
                            }
                        }
                    }
                }
            })
            tx.start()
            mainArray.put(y)
            mainArray.put(ContactDetails)
            Log.d(
                "log-AllContactOfUserDeviceView",
                "sending json array of contact : $ContactDetails"
            )
            val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
                Method.POST,
                endpoint,
                mainArray,
                Response.Listener { response -> //                        Log.d("log-AllContactOfUserDeviceView", "onResponse: json array list : " + response.toString());
                    Log.d(
                        "log-AllContactOfUserDeviceView",
                        "onResponse: response length : " + response.length()
                    )
                    var tmp: JSONObject
                    var isUpdatable: Boolean
                    var isUpdatableCount = 0
                    for (i in 0 until response.length()) {
                        isUpdatable = true
                        try {
                            tmp = response.getJSONObject(i)
                            val tnum = tmp.getString("Number").toLong()
                            val tCID = tmp.getString("_id")
                            val name = tmp.getString("Name")
                            Log.d(
                                "log-AllContactOfUserDeviceView",
                                "onResponse: id:$tCID name:$name num:$tnum"
                            )
                            //
                            if (connectedContact != null) {
                                for (j in connectedContact) {
                                    if (connectedContact[0]?.MobileNumber == tnum) {
                                        isUpdatable = false
                                        break
                                    }
                                }
                            }
                            if (isUpdatable) {
                                //update CID
                                massegeDao.updateAllContactOfUserEntityCID(
                                    tnum,
                                    tCID,
                                    MainActivity.user_login_id
                                )
                                //                                        long x = massegeDao.getHighestPriorityRank();
//                                        ContactWithMassengerEntity new_entity = new ContactWithMassengerEntity(tnum, mc.decrypt(tmp.get(1).toString()), tCID, x+1);
//                                        massegeDao.SaveContactDetailsInDatabase(new_entity);
                                isUpdatableCount++
                            }
                        } catch (e: JSONException) {
                            Log.d(
                                "log-AllContactOfUserDeviceView",
                                "onResponse: jsonException : $e"
                            )
                        } catch (e: Exception) {
                            Log.d(
                                "log-AllContactOfUserDeviceView",
                                "onResponse: Simple Exception : $e"
                            )
                        }
                    }
                    Log.d(
                        "log-AllContactOfUserDeviceView",
                        "onResponse: isUpdatableCount i s$isUpdatableCount"
                    )
                    callback.execute(1, "sync completed")
                },
                Response.ErrorListener { error ->
                    Log.d(
                        "log-AllContactOfUserDeviceView",
                        "onErrorResponse: setChatDetails error: $error"
                    )
                    //                    Toast.makeText(AllContactOfUserInDeviceView.this, "sync failed , Server side error: " + error, Toast.LENGTH_SHORT).show();
                    callback.execute(0, "sync failed , Server side error : $error")
                }) {
                override fun getHeaders(): Map<String, String> {
                    val headers: MutableMap<String, String> = HashMap()
                    //                     Log.d("log-SyncContactDetailsThread", "apiKey : " + API_SERVER_API_KEY);
                    headers["api_key"] = MainActivity.API_SERVER_API_KEY!!
                    return headers
                }
            }
            jsonArrayRequest.retryPolicy = DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            requestQueue.add(jsonArrayRequest)
        }
    }

    companion object {
        private const val PERMISSION_ALL = 108
    }
}