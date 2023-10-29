package com.example.mank.threadPackages

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.mank.localDatabaseFiles.daoClasses.ContactsDao
import com.example.mank.localDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.MainActivity
import com.example.mank.MainActivity.Companion.contactListAdapter
import com.example.mank.MainActivity.Companion.user_login_id
import com.example.mank.cipher.MyCipher
import com.example.mank.configuration.GlobalVariables
import com.example.mank.configuration.GlobalVariables.URL_MAIN
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.TreeSet



class SyncContactDetailsThread(private val context: Context, connectedContact: List<AllContactOfUserEntity?>?, disConnectedContact: List<AllContactOfUserEntity?>?, callback: IContactSync
) : Thread() {
	private var type = 0
	private var allContactOfUserEntity: AllContactOfUserEntity? = null
	private var allContactOfUserEntityList: MutableList<AllContactOfUserEntity?> = ArrayList()
	private val connectedContact: List<AllContactOfUserEntity?>?
	private val disConnectedContact: List<AllContactOfUserEntity?>?
	private var contactDao: ContactsDao? = null
	private val mc = MyCipher()
	private val callback: IContactSync


	fun setFromWhere(type: Int) {
		this.type = type
	}

	private val contactComparator = Comparator<AllContactOfUserEntity> { contact1, contact2 ->
		try {
			val displayName1 = contact1.DisplayName ?: ""
			val displayName2 = contact2.DisplayName ?: ""
			displayName1.compareTo(displayName2, ignoreCase = true)
		} catch (e: Exception) {
			Log.d("log-SyncContactDetailsThread", "Exception in comparator: $e")
			1
		}
	}

	init {
		contactDao = MainActivity.db?.contactDao()
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
					@SuppressLint("Range") val display_name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
					@SuppressLint("Range") var number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
					number = number.replace("\\s".toRegex(), "")
					number = number.replace("-".toRegex(), "")
					number = number.replace("\\)".toRegex(), "")
					number = number.replace("\\(".toRegex(), "")
					if (number.length > 9) {
						try {
							if (number[0] == '+') {
								number = number.substring(3)
							}
							allContactOfUserEntity = AllContactOfUserEntity(number.toLong(), display_name, "-1")
						} catch (e: IndexOutOfBoundsException) {
							Log.d("log-SyncContactDetailsThread", "IndexOutOfBoundsException: for $number || $e")
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


			val uniqueContacts = TreeSet(contactComparator)
			uniqueContacts.addAll(allContactOfUserEntityList)
			allContactOfUserEntityList = ArrayList(uniqueContacts)


			val tx = Thread(object : Runnable {
				override fun run() {
					synchronized(this) {
						for (entity in allContactOfUserEntityList) {
							val x = entity?.MobileNumber?.let {
								contactDao?.getSelectedAllContactOfUserEntity(it, user_login_id)
							}
							if (x?.size == 0) {
								contactDao?.addAllContactOfUserEntity(entity)
							} else {
								contactListAdapter?.updateContactSavedName(entity?.MobileNumber ?: 0L, entity?.DisplayName?:"")
							}
						}
					}
				}
			})
			tx.start()
			mainArray.put(y)
			mainArray.put(ContactDetails)
			Log.d("log-SyncContactDetailsThread", "sending json array of contact : $ContactDetails")
			val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(Method.POST, endpoint, mainArray, Response.Listener { response ->
				Log.d("log-SyncContactDetailsThread", "onResponse: response length : " + response.length())
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
						Log.d("log-SyncContactDetailsThread", "onResponse: id:$tCID name:$name num:$tnum") //
						if (connectedContact != null) {
							for (j in connectedContact) {
								if (connectedContact[0]?.MobileNumber == tnum) {
									isUpdatable = false
									break
								}
							}
						}
						if (isUpdatable) { //update CID
							contactDao?.updateAllContactOfUserEntityCID(tnum, tCID, MainActivity.user_login_id)
							isUpdatableCount++
						}
					} catch (e: JSONException) {
						Log.d("log-SyncContactDetailsThread", "onResponse: jsonException : $e")
					} catch (e: Exception) {
						Log.d("log-SyncContactDetailsThread", "onResponse: Simple Exception : $e")
					}
				}
				Log.d("log-SyncContactDetailsThread", "onResponse: isUpdatableCount i s$isUpdatableCount")
				callback.execute(1, "sync completed")
			}, Response.ErrorListener { error ->
				Log.d("log-SyncContactDetailsThread",
					"onErrorResponse: setChatDetails error: $error") //Toast.makeText(AllContactOfUserInDeviceView.this, "sync failed , Server side error: " + error, Toast.LENGTH_SHORT).show();
				callback.execute(0, "sync failed , Server side error : $error")
			}) {
				override fun getHeaders(): Map<String, String> {
					val headers: MutableMap<String, String> = HashMap() //Log.d("log-SyncContactDetailsThread", "apiKey : " + API_SERVER_API_KEY);
					headers["api_key"] = MainActivity.API_SERVER_API_KEY!!
					return headers
				}
			}
			jsonArrayRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
			requestQueue.add(jsonArrayRequest)


			val mainArray1 = JSONArray()
			val contactDetails1 = JSONArray()
			if (connectedContact != null) {
				for (entity in connectedContact) {
					val jsonParam = JSONArray()
					jsonParam.put(entity?.CID!!)
					jsonParam.put(entity.MobileNumber!!)
					jsonParam.put(entity.DisplayName!!)
					contactDetails1.put(jsonParam)
					if (entity.DisplayName!! == "D1 Org") {
						Log.d("log-SyncContactDetailsThread", "d1 org : ${jsonParam.toString()}")
					}
				}
			}

			mainArray1.put(y)
			mainArray1.put(contactDetails1)
			val endpoint1 = URL_MAIN + "updateContactNameOfUser"
			val jsonArrayRequest1: JsonArrayRequest = object : JsonArrayRequest(Method.POST, endpoint1, mainArray1, Response.Listener { response ->
				Log.d("log-SyncContactDetailsThread", "r1 onResponse: response length : " + response.length())

			}, Response.ErrorListener { error ->
				Log.d("log-SyncContactDetailsThread", "r1 onErrorResponse: setChatDetails error: $error")
			}) {
				override fun getHeaders(): Map<String, String> {
					val headers: MutableMap<String, String> = HashMap()
					headers["api_key"] = MainActivity.API_SERVER_API_KEY!!
					return headers
				}
			}
			requestQueue.add(jsonArrayRequest1)

		}
	}

	companion object {
		private const val PERMISSION_ALL = 108
	}
}