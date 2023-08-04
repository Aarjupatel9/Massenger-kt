package com.example.mank

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mank.DatabaseAdapter.ContactListAdapter
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.AppDetailsHolder
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.ContactListHolder
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.MassegeHolderForSpecificPurpose
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.contactDetailsHolderForSync
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.userIdEntityHolder
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.LocalDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.LocalDatabaseFiles.entities.MassegeEntity
import com.example.mank.LocalDatabaseFiles.entities.SetupFirstTimeEntity
import com.example.mank.LoginMenagement.Login
import com.example.mank.RecyclerViewClassesFolder.RecyclerViewAdapter
import com.example.mank.TabMainHelper.SectionsPagerAdapter
import com.example.mank.ThreadPackages.IContactSync
import com.example.mank.ThreadPackages.MassegePopSoundThread
import com.example.mank.ThreadPackages.StatusForThread
import com.example.mank.ThreadPackages.SyncContactDetailsThread
import com.example.mank.cipher.MyCipher
import com.example.mank.configuration.GlobalVariables
import com.example.mank.configuration.permissionMain
import com.example.mank.configuration.permission_code
import com.example.mank.profile.AllSettingsActivity
import com.example.mank.profile.BgImageSetForContactPage
import com.example.mank.profile.UserProfileActivity
import com.example.mank.socket.SocketClass
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.example.mank.R
import com.example.mank.databinding.ActivityMainBinding
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : FragmentActivity() {
	private var toStopAppMainThread = false
	private var appOpenFromBackGround = false
	private var EverythingIsOhkInApp = false
	private var MAPSearchView: SearchView? = null
	private var mainProgressBar: ProgressBar? = null
	var LAUNCH_LOGIN_ACTIVITY = 1
	var LoginIntentData: Intent? = null
	private var massegeDao: MassegeDao? = null
	private var binding: ActivityMainBinding? = null
	override fun onStart() {
		super.onStart() //        startBackgroundPractice();
		startNetworkListener()
		if (contactListAdapter != null) {
			contactListAdapter!!.setMyContext(this)
		}
		MainActivityStaticContext = this
		Contact_page_opened_id = "-1"
		FinishCode = 0
		Log.d("log-Contact_page_opened_id", "onStart: in MainActivity Contact_page_opened_id  is  : " + Contact_page_opened_id)
		if (!appOpenFromBackGround) {
			appOpenFromBackGround = true
		} else if (EverythingIsOhkInApp) { //we will send that we are online massege to server
			Log.d("log-Contact_page_opened_id", "EverythingIsOhkInApp is :  $EverythingIsOhkInApp")
		}
	}


	//    private void initContentResolver() {
	//        ContentResolver contentResolver = getContentResolver();
	//        Handler handler = new Handler();
	//        MyContentObserver contactsObserver = new MyContentObserver(handler);
	//        final String[] PERMISSIONS = {android.Manifest.permission.INTERNET, android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.CHANGE_NETWORK_STATE, android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS,};
	//        if (!hasPermissions(this, PERMISSIONS)) {
	//            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_initContentResolver);
	//        } else {
	//            contentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contactsObserver);
	//        }
	//    }
	override fun onResume() {
		super.onResume()
		if (statusForThread == null) {
			statusForThread = StatusForThread(0)
		}
		MainActivityStaticContext = this

	}

	override fun onPause() {
		super.onPause()
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.d("log-onDestroy", "onDestroy: FinishCode is: " + FinishCode)
		if (FinishCode == 0) {
			Log.d("log-onDestroy", "onDestroy: FinishCode is: " + FinishCode)
			if (EverythingIsOhkInApp) {
				Log.d("log-onDestroy", "onPause EverythingIsOhkInApp: enter here")
			}
			toStopAppMainThread = true //for stop appMAinThread
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		MainActivityStaticContext = this
		db = Room.databaseBuilder(applicationContext, MainDatabaseClass::class.java, "MassengerDatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build()

		massegeDao = db!!.massegeDao()
		API_SERVER_API_KEY = getString(R.string.api_server_api_key)
		verifyLogin(0)
	}

	fun verifyLogin(code: Int) {
		val login = Login()
		if (login.isLogIn(db) == 0) {
			Log.d("log-not logined", "onCreate: not login cond. reached")
			val intent = Intent(this, LoginActivity::class.java)
			startActivityForResult(intent, LAUNCH_LOGIN_ACTIVITY)
			Log.d("log-FinishCode", "onCreate: FinishCode is: " + FinishCode)
			FinishCode = 2
		} else { //            initContentResolver();
			startMain()
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		Log.d("log-MainActivity-onNewIntent", "onNewIntent || start")
		verifyLogin(1)
	}

	private fun startMain() {
		if (!permissionMain.hasPermissions(this, *permission_code.PERMISSIONS_MUST_REQUIRED)) {
			ActivityCompat.requestPermissions(this, permission_code.PERMISSIONS_MUST_REQUIRED, permission_code.PERMISSIONS_MUST_REQUIRED_CODE)
		}
		db = databaseBuilder(applicationContext, MainDatabaseClass::class.java, "MassengerDatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build()
		massegeDao = db!!.massegeDao()
		val userIdEntityHolder = userIdEntityHolder(db!!)
		user_login_id = userIdEntityHolder.userLoginId
		UserMobileNumber = userIdEntityHolder.userMobileNumber
		contactListAdapter = ContactListAdapter(db!!)
		contactListAdapter!!.setMyContext(this)
		contactArrayList = contactList
		saveFireBaseTokenToServer(user_login_id.toString())
		CreateSocketConnection()
		statusForThread = StatusForThread(0)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding!!.root)
		mainProgressBar = binding!!.MPMainProgressBar
		MAPSearchView = binding!!.MAPSearchView
		mainProgressBar!!.visibility = View.GONE
		val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
		val viewPager = binding!!.MPViewPager
		viewPager.adapter = sectionsPagerAdapter
		val tabs = binding!!.MainTabs
		tabs.setupWithViewPager(viewPager)
		syncContactAtAppStart()
		EverythingIsOhkInApp = true
		MAPSearchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				Log.d("log-MainActivity", "onQueryTextChange newText:$query")
				return false
			}

			override fun onQueryTextChange(newText: String): Boolean {
				Log.d("log-MainActivity", "onQueryTextChange newText:$newText") //                if (!prevCatchText.equals(newText)) {
				//                    prevCatchText = newText;
				if (newText == null || newText.trim { it <= ' ' } == "") {
					contactArrayListFilter(newText, 0)
				} else {
					contactArrayListFilter(newText, 1)
				} //                }
				return false
			}
		})
	}

	private fun startFirstTimeContactSync() {
		val intent = Intent(this, AllContactOfUserInDeviceView::class.java)
		startActivityForResult(intent, FirstTimeAppSyncAllContactRequestCode)
	}

	private fun startFirstTimeProfileUpdate() {
		val intent = Intent(this, UserProfileActivity::class.java)
		startActivityForResult(intent, FirstTimeProfileUpdateRequestCode)
	}

	private var disConnectedContact: List<AllContactOfUserEntity?>? = ArrayList()
	private var connectedContact: List<AllContactOfUserEntity?>? = ArrayList()
	fun syncContactAtAppStart() {
		if (!permissionMain.hasPermissions(this, *permission_code.CONTACT_STORAGE_PERMISSION)) {
			ActivityCompat.requestPermissions(this, permission_code.PERMISSIONS, permission_code.PERMISSION_CONTACT_SYNC)
			return
		}
		val tf = Thread {
			try {
				val appDetailsHolder = AppDetailsHolder(db!!)
				val insertedAtLastEntity = appDetailsHolder.data
				Log.d("log-MainActivity", "Thread tf || lastOpenTime : " + insertedAtLastEntity?.date)
				if (insertedAtLastEntity?.date!! < Date().time - 300000) { //app is open after 5 min or more break
					// do background contact sync
					Log.d("log-MainActivity", "Thread tf || app is open after 1 min or more break")

					//                        Toast.makeText(MainActivity.this, "app is opened after 1 min break or more", Toast.LENGTH_SHORT).show();
				}
			} catch (e: Exception) {
				Log.d("log-MainActivity", "Thread tf || exception e: $e")
			}
			val new_entity = SetupFirstTimeEntity()
			massegeDao!!.insertLastAppOpenEntity(new_entity)
		}
		tf.start()
		val contactDetailsHolder = contactDetailsHolderForSync(db!!)
		connectedContact = contactDetailsHolder.connectedContact
		disConnectedContact = contactDetailsHolder.disConnectedContact


		val scdt = SyncContactDetailsThread(this, connectedContact, disConnectedContact, object : IContactSync {
			override fun execute(status: Int, massege: String?) {
				Log.d("log-getListOfAllUserContact", "calling getListOfAllUserContact activity")
			}
		})
		scdt.setFromWhere(1)
		scdt.start()
	}

	@SuppressLint("NotifyDataSetChanged")
	fun contactArrayListFilter(newText: String, flag: Int) {
		if (flag == 0) {
			Log.d("log-MainActivity", "contactArrayListFilter start with flag 0")
			contactArrayList?.clear()
			contactArrayList?.addAll(filteredContactArrayList!!)
			recyclerViewAdapter?.notifyDataSetChanged()
			return
		}
		Log.d("log-MainActivity", "contactArrayListFilter start")
		contactArrayList!!.clear()
		contactArrayList!!.addAll(filteredContactArrayList!!)
		for (e in filteredContactArrayList!!) {
			val displayNameMatches = e.DisplayName?.lowercase(Locale.getDefault())?.contains(newText.lowercase(Locale.getDefault())) ?: false
			val mobileNumberMatches = e.MobileNumber.toString().contains(newText) ?: false

			if (!displayNameMatches && !mobileNumberMatches) {
				contactArrayList!!.remove(e)
			}
		}
		recyclerViewAdapter!!.notifyDataSetChanged()
	}

	private val FirstTimeAppSyncAllContactRequestCode = 203
	private val FirstTimeProfileUpdateRequestCode = 202
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == LAUNCH_LOGIN_ACTIVITY) {
			if (resultCode == RESULT_OK) { //1101 is code for this permission checking
				LoginIntentData = data
				startMain()
				startFirstTimeProfileUpdate()
			}
			if (resultCode == RESULT_CANCELED) {
				Log.d("log-onActivityResult", "Activity RESULT_CANCELED")
			}
		} else if (requestCode == FirstTimeProfileUpdateRequestCode) {
			startFirstTimeContactSync()
		} else if (requestCode == FirstTimeAppSyncAllContactRequestCode) { //            startMain();
		}
	} //onActivityResult

	fun getListOfAllUserContact(view: View?) { //        Toast.makeText(this, "you Click all contact details", Toast.LENGTH_SHORT).show();
		val intent = Intent(this@MainActivity, AllContactOfUserInDeviceView::class.java)
		Log.d("log-getListOfAllUserContact", "calling getListOfAllUserContact activity")
		startActivity(intent)
	}

	private fun CreateSocketConnection() {
		Log.d("log-MainActivity", "CreateSocketConnection: start")
		socketOBJ = SocketClass(db)
		socket = socketOBJ?.socket
		if (socket == null) {
			return
		}
		socketOBJ?.joinRoom(user_login_id)
		socket!!.on(Socket.EVENT_CONNECT) {
			Log.d("log-MainActivity", "onJoinAcknowledgement: join success ")
			runOnUiThread { //                        Toast.makeText(MainActivity.this, "onJoinAcknowledgement: join success ", Toast.LENGTH_LONG).show();
			}
			var tmp3: List<MassegeEntity?>?
			var tmp4: List<MassegeEntity?> //we have to get list of all masseges and send them to server at user came online
			val mhsp = MassegeHolderForSpecificPurpose(db!!, -1)
			tmp3 = mhsp.massegeList
			try {
				val massegeData = JSONArray()
				if (tmp3 != null) {
					for (tmp1 in tmp3) {
						try {
							val massegeOBJ = JSONObject()
							massegeOBJ.put("from", tmp1?.senderId)
							massegeOBJ.put("massege", tmp1?.massege)
							massegeOBJ.put("to", tmp1?.receiverId)
							massegeOBJ.put("chatId", tmp1?.chatId)
							massegeOBJ.put("time", tmp1?.timeOfSend)
							massegeOBJ.put("massegeStatus", 0)
							massegeOBJ.put("massegeStatusL", 1)
							massegeOBJ.put("ef1", 1)
							massegeOBJ.put("ef2", 1)
							massegeData.put(massegeOBJ)
						} catch (ex: Exception) {
							Log.d("log-onJoinAcknowledgement", "exception || :$ex")
							Log.d("log-onJoinAcknowledgement", "exception || massegeData : $massegeData")
							Log.d("log-onJoinAcknowledgement", "exception || tmp3.size() : " + massegeData.length())
						}
					}
				}
				if (tmp3?.size!! > 0) {
					socket!!.emit("send_massege_to_server_from_sender", user_login_id, massegeData)
					Log.d("log-MainActivity", "onJoinAcknowledgement || massegeData size :$massegeData")
				}
			} catch (e: Exception) {
				Log.d("log-MainActivity", "onJoinAcknowledgement || exception :$e")
			}

			val mhsp1 = MassegeHolderForSpecificPurpose(db!!, 0)
			tmp3 = mhsp1.massegeList
			try {
				val massegeData = JSONArray()
				if (tmp3 != null) {
					for (tmp1 in tmp3) {
						try {
							val massegeOBJ = JSONObject()
							massegeOBJ.put("from", tmp1?.senderId)
							massegeOBJ.put("massege", tmp1?.massege)
							massegeOBJ.put("to", tmp1?.receiverId)
							massegeOBJ.put("chatId", tmp1?.chatId)
							massegeOBJ.put("time", tmp1?.timeOfSend)
							massegeOBJ.put("massegeStatus", tmp1?.massegeStatus)
							massegeOBJ.put("massegeStatusL", 1)
							massegeOBJ.put("ef1", 1)
							massegeOBJ.put("ef2", 1)
							massegeData.put(massegeOBJ)
						} catch (ex: Exception) {
							Log.d("log-onJoinAcknowledgement", "exception || :$ex")
							Log.d("log-onJoinAcknowledgement", "exception || massegeData : $massegeData")
							Log.d("log-onJoinAcknowledgement", "exception || tmp3.size() : " + massegeData.length())
						}
					}
				}
				if (tmp3?.size!! > 0) {
					socket!!.emit("send_massege_to_server_from_sender", user_login_id, massegeData)
					Log.d("log-MainActivity", "onJoinAcknowledgement || massegeData size :$massegeData")
				}
			} catch (e: Exception) {
				Log.d("log-MainActivity", "onJoinAcknowledgement || exception :$e")
			}

			//updating userProfileImages
			val jsonArray = JSONArray()
			try {
				Log.d("log-MainActivity", "updating userProfileImages : " + contactArrayList!!.size)
				for (e in contactArrayList!!) {
					try {
						val tmp = JSONObject()
						tmp.put("_id", e?.CID)
						tmp.put("Number", e?.MobileNumber)
						tmp.put("ProfileImageVersion", e?.profileImageVersion)
						jsonArray.put(tmp)
					} catch (ex: Exception) {
						Log.d("log-ContactListAdapter-Exception", ex.toString())
					}
				}
				Log.d("log-MainActivity", "profileImage update part : $jsonArray")
				socket!!.emit("updateProfileImages", user_login_id, jsonArray, 1)
			} catch (exception: Exception) {
				Log.d("log-ContactListAdapter-Exception", exception.toString())
			}
		}
		socket?.on("new_massege_from_server", onMassegeArriveFromServer)
		socket?.on("send_massege_to_server_from_sender_acknowledgement", onMassegeReachAtServerFromCMDV)
		socket?.on("massege_reach_read_receipt", onMassegeReachReadReceipt)
		socket?.on("updateSingleContactProfileImage", onUpdateSingleContactProfileImage)
		socket?.on("contact_massege_typing_event", onContactMassegeTypingEvent)
		socket?.on(Socket.EVENT_CONNECT_ERROR) { socket!!.connect() }
		socket?.on(Socket.EVENT_DISCONNECT) {
			Log.d("log-MainActivity", "Socket.EVENT_DISCONNECT socket.isActive() : ")
		}
	}

	//socket event listener define here
	//completed
	private val onUpdateSingleContactProfileImage: Emitter.Listener = object : Emitter.Listener {
		@SuppressLint("NotifyDataSetChanged")
		override fun call(vararg args: Any) {
			Log.d("log-onUpdateSingleContactProfileImage", "onUpdateSingleContactProfileImage || start ")
			val userId = args[0].toString()
			val id = args[1].toString()
			val ProfileImageVersion = args[3].toString().toLong()
			val profileImageBase64: String
			try {
				profileImageBase64 = args[2] as String
				val profileImageByteArray = Base64.decode(profileImageBase64, Base64.DEFAULT)
				if (profileImageByteArray.isNotEmpty()) {
					synchronized(this) {
						val bitmapImage = BitmapFactory.decodeByteArray(profileImageByteArray, 0, profileImageByteArray.size)
						Log.d("log-saveImageToInternalStorage", "Saved image of size : " + profileImageByteArray.size + " and resolution : " + bitmapImage.width + "*" + bitmapImage.height)
						contactListAdapter!!.practiceMethod(id, profileImageByteArray) // to update contactList
						if (saveContactProfileImageToStorage(id, profileImageByteArray)) {
							massegeDao!!.updateProfileImageVersion(id, ProfileImageVersion, user_login_id)
						}
					}
				}
				Log.d("log-onUpdateSingleContactProfileImage", "ProfileImageVersion : " + ProfileImageVersion + " and for cid : " + id + " bytearray : " + Arrays.toString(profileImageByteArray))
			} catch (ex: Exception) {
				Log.d("log-onUpdateSingleContactProfileImage-Exception", ex.toString())
			}
		}
	}
	private val onContactMassegeTypingEvent: Emitter.Listener = Emitter.Listener { args ->
		Log.d("log-onContactMassegeTypingEvent", "onUpdateSingleContactProfileImage || start ")
		try {

			val userId = args[0].toString()
			val CID = args[1].toString()

			//have to check userId ids present in contactList or not
			thread {
				var e: ContactWithMassengerEntity? = massegeDao?.checkCIDInConnectedSavedContact(CID, user_login_id);
				if (e != null) {
					if (Contact_page_opened_id == userId) {
						Log.d("log-onContactMassegeTypingEvent", "sending broadcast event for typing start")
						val intent = Intent(ACTION_RUN_FUNCTION)
						sendBroadcast(intent)
					} else {
						contactListAdapter?.setTypingStatus(userId)
						Log.d("log-onContactMassegeTypingEvent", "have to update in contact list lastMassege place")
					}
				} else {
					Log.d("log-onContactMassegeTypingEvent", "Contact not found in database")
				}
			}
		} catch (e: Exception) {
			Log.d("log-onUpdateSingleContactProfileImage", "Exception : $e")
		}
	}
	private val onMassegeReachReadReceipt = Emitter.Listener { args ->
		val requestCode = args[0].toString().toInt()
		Log.d("log-onMassegeReachReadReceipt", "onMassegeReachReadReceipt || start requestCode:$requestCode")
		if (requestCode == 1) {
			val data = args[1] as JSONObject
			try {
				val viewStatus = data["massegeStatus"].toString().toInt()
				val massege_sent_time = data["time"].toString().toLong()
				val sender_id = data["from"].toString()
				val receiver_id = data["to"].toString()
				if (Contact_page_opened_id == receiver_id) {
					Log.d("log-onMassegeReachReadReceipt", "page is opened obj$data")
					ContactMassegeDetailsView.massegeListAdapter?.updateMassegeStatus(receiver_id, massege_sent_time, viewStatus)
				}                // update view status into database
				val t = Thread {
					val massegeStatus = massegeDao?.getMassegeStatus(sender_id, receiver_id, massege_sent_time, user_login_id)
					if (massegeStatus != null) {
						if (massegeStatus < viewStatus) {
							massegeDao?.updateMassegeStatus(sender_id, receiver_id, massege_sent_time, viewStatus, user_login_id)
						}
					}
				}
				t.start()
				val x: JSONArray
				try {
					x = JSONArray()
					x.put(data)
					socket!!.emit("massege_reach_read_receipt_acknowledgement", 1, user_login_id, x)
					Log.d("log-onMassegeReachReadReceipt", "massege_reach_read_receipt_acknowledgement event emitted")
				} catch (e: Exception) {
					Log.d("log-onMassegeReachReadReceipt", "Exception || e:$e")
				}
			} catch (e: Exception) {
				Log.d("log-MA-onMassegeReachReadReceipt", "Exception || e:$e")
			}
		} else {
			Log.d("log-MainActivity", "onMassegeReachReadReceipt || request code :$requestCode")
		}
	}

	//completed
	@SuppressLint("NotifyDataSetChanged")
	private val onMassegeArriveFromServer = Emitter.Listener { args ->
		val acknowledgement_id = args[0] as Int
		var requestCode = -1
		try {
			requestCode = args[2] as Int
		} catch (e: Exception) {
			Log.d("log-exception-in-massege-arrive", "call: Exception is : $e")
		}
		Log.d("log-onMassegeArriveFromServer3", "onMassegeArriveFromServer || requestCode is : $requestCode")

		//at send by user imidiate
		if (requestCode == 0) {
			var new_massege_time_of_send: Long = -1
			try {
				val new_massege = args[1] as JSONObject
				Log.d("log-onMassegeArriveFromServer3", "args : $new_massege")
				val new_massege_sender_id = new_massege["from"].toString()
				new_massege_time_of_send = new_massege["time"] as Long
				val contactArrayList1: ArrayList<ContactWithMassengerEntity?>? = contactArrayList
				val massege = new_massege["massege"].toString()
				var viewStatus = 2
				val newMassegeEntity1 = MassegeEntity(new_massege_sender_id, user_login_id, massege, new_massege_time_of_send, viewStatus)
				Log.d("log-onMassegeArriveFromServer3", "time : $new_massege_time_of_send")
				for (i in contactArrayList1!!.indices) {
					if (contactArrayList1[i]?.CID == new_massege_sender_id) { //means massege arrive from known or saved contacts
						Log.d("log-onMassegeArriveFromServer3", "massege arrive from known sources : ")
						if (new_massege_sender_id == Contact_page_opened_id) {
							Log.d("log-onMassegeArriveFromServer3", "Contact page is opened")
							viewStatus = 3
							val newMassegeEntity2 = MassegeEntity(new_massege_sender_id, user_login_id, new_massege["massege"].toString(), new_massege_time_of_send, viewStatus)
							ContactMassegeDetailsView.massegeListAdapter?.addMassege(newMassegeEntity2, 1) //1 contact page is opened
							val massegePopSoundThread = MassegePopSoundThread(this@MainActivity, 0)
							massegePopSoundThread.start()
						} else {
							Log.d("log-onMassegeArriveFromServer3", "Contact page is not opened")
							val contactView = contactArrayList1[i]
							val prev_value = contactView?.newMassegeArriveValue
							if (prev_value != null) {
								contactView?.newMassegeArriveValue = prev_value + 1
							}
							contactView?.lastMassege = massege
							contactArrayList!![i] = contactView
							runOnUiThread {
								recyclerViewAdapter!!.notifyDataSetChanged()
								ChatsRecyclerView!!.scrollToPosition(recyclerViewAdapter!!.itemCountMyOwn)
							}
							val massegePopSoundThread = MassegePopSoundThread(this@MainActivity, 1)
							massegePopSoundThread.start()
							val t = Thread {
								massegeDao!!.incrementNewMassegeArriveValue(new_massege_sender_id, user_login_id)
							}
							t.start()
							val massegeInsertIntoDatabase = Thread {
								val massegeDao = db!!.massegeDao()
								try {
									massegeDao.insertMassegeIntoChat(newMassegeEntity1)
									Log.d("log-onMassegeArriveFromServer3", "massege is inserted into database successfully")
								} catch (e: Exception) {
									showPopUpMessage(e.toString())
									Log.d("log-sql-exception",
										e.toString() + " for massege:" + newMassegeEntity1.massege + ", s_id:" + newMassegeEntity1.senderId + ", r_id:" + newMassegeEntity1.receiverId + ", time:" + newMassegeEntity1.timeOfSend + ", status:" + newMassegeEntity1.massegeStatus)
									updateMassegeStatusFromException(new_massege)
								}
							}
							massegeInsertIntoDatabase.start()
						}
					} else { //means massege arrive from unknown or new contact
						Log.d("log-onMassegeArriveFromServer3", "massege arrive from known sources : ")
					}
				}
				val checkContactSavedInDB = Thread {
					val x = massegeDao!!.getContactWith_CID(newMassegeEntity1.senderId, user_login_id)
					if (x == null) {
						Log.d("log-onMassegeArriveFromServer3", "setPriorityRankThread1")
						FetchDataFromServerAndSaveIntoDB(newMassegeEntity1.senderId)
					} else {
						val setPriorityRankThread = Thread {
							val HighestPriority = massegeDao!!.getHighestPriorityRank(user_login_id)
							massegeDao!!.setPriorityRank(newMassegeEntity1.senderId, HighestPriority + 1, user_login_id)
							if (contactListAdapter != null) {
								contactListAdapter!!.updatePositionOfContact(newMassegeEntity1.senderId, this@MainActivity)
							}
						}
						setPriorityRankThread.start()
					}
				}
				checkContactSavedInDB.start()
				val jsonArray = JSONArray()
				try {
					val tmpOBJ = JSONObject()
					tmpOBJ.put("to", newMassegeEntity1.receiverId)
					tmpOBJ.put("from", new_massege_sender_id)
					tmpOBJ.put("time", new_massege_time_of_send)
					tmpOBJ.put("massegeStatus", viewStatus)
					jsonArray.put(tmpOBJ)
					if (new_massege_sender_id != user_login_id) {
						socket!!.emit("massege_reach_read_receipt", 3, user_login_id, jsonArray) // ViewStatus 2 means at contact's database and 3 means read by contact
					}
					Log.d("log-onMassegeArriveFromServer3", "massege_reach_read_receipt_acknowledgement socket emit :$jsonArray")
				} catch (e: JSONException) {
					e.printStackTrace()
				}
			} catch (e: Exception) {
				Log.d("log-onMassegeArriveFromServer-Exception", "call: error while parsing data : $e")
			}
		} else if (requestCode == 1) {
			var sentTime: Long = -1
			try {
				val new_massege = args[1] as JSONObject
				Log.d("log-onMassegeArriveFromServer1", "args : $new_massege")
				val senderId = new_massege["from"].toString()
				sentTime = new_massege["time"] as Long
				val contactArrayList1: ArrayList<ContactWithMassengerEntity?>? = contactArrayList
				val massege = new_massege["massege"].toString()
				var viewStatus = new_massege["massegeStatus"] as Int
				val newMassegeEntity1 = MassegeEntity(senderId, user_login_id, massege, sentTime, viewStatus)
				Log.d("log-onMassegeArriveFromServer1", "time : $sentTime")
				for (i in contactArrayList1?.indices!!) {

					Log.d("log-onMassegeArriveFromServer1", "massege arrive from known sources : ")
					if (senderId == Contact_page_opened_id) {
						Log.d("log-onMassegeArriveFromServer1", "Contact page is opened")
						viewStatus = 3
						val newMassegeEntity2 = MassegeEntity(senderId, user_login_id, new_massege["massege"].toString(), sentTime, viewStatus)
						ContactMassegeDetailsView.massegeListAdapter?.addMassege(newMassegeEntity2, 1) //1 contact page is opened
						val massegePopSoundThread = MassegePopSoundThread(this@MainActivity, 0)
						massegePopSoundThread.start()
					} else {
						Log.d("log-onMassegeArriveFromServer1", "Contact page is not opened")
						val contactView = contactArrayList1[i]

						contactListAdapter?.setLastMassege(contactView?.CID)

						runOnUiThread {
							recyclerViewAdapter!!.notifyDataSetChanged()
							ChatsRecyclerView!!.scrollToPosition(recyclerViewAdapter!!.itemCountMyOwn)
						}

						val massegeInsertIntoDatabase = Thread {
							val massegeDao = db!!.massegeDao()
							try {
								massegeDao.insertMassegeIntoChat(newMassegeEntity1)
								Log.d("log-onMassegeArriveFromServer1", "massege is inserted into database successfully")
							} catch (e: Exception) {
								showPopUpMessage(e.toString())
								Log.d("log-sql-exception",
									e.toString() + " for massege:" + newMassegeEntity1.massege + ", s_id:" + newMassegeEntity1.senderId + ", r_id:" + newMassegeEntity1.receiverId + ", time:" + newMassegeEntity1.timeOfSend + ", status:" + newMassegeEntity1.massegeStatus)
								updateMassegeStatusFromException(new_massege)
							}
						}
						massegeInsertIntoDatabase.start()
					}
				}
				val jsonArray = JSONArray()
				try {
					val tmpOBJ = JSONObject()
					tmpOBJ.put("to", newMassegeEntity1.receiverId)
					tmpOBJ.put("from", senderId)
					tmpOBJ.put("time", sentTime)

					jsonArray.put(tmpOBJ)
					socket!!.emit("massege_reach_read_receipt", 1, user_login_id, jsonArray)
					Log.d("log-onMassegeArriveFromServer1", "massege_reach_read_receipt_acknowledgement socket emit :$jsonArray")
				} catch (e: JSONException) {
					e.printStackTrace()
				}

			} catch (e: Exception) {
				Log.d("log-onMassegeArriveFromServer-Exception", "call: error while parsing data : $e")
			}
		} else {
			showPopUpMessage("onMassegeArriveFromServer unHandled requestCode arrive : $requestCode") //                Toast.makeText(MainActivity.this, "onMassegeArriveFromServer unHandled requestCode arrive : "+requestCode, Toast.LENGTH_LONG).show();
			Log.d("log-onMassegeArriveFromServer", "onMassegeArriveFromServer unHandled requestCode arrive : $requestCode")
		}

	}

	//check if status is updatable or not and update
	private fun updateMassegeStatusFromException(new_massege: JSONObject) {
		try {
			val massegeStatus = new_massege["massegeStatus"] as Int
			val s = new_massege["from"].toString()
			val r = new_massege["to"].toString()
			val t = new_massege["time"] as Long
			val vs = massegeDao!!.getMassegeStatus(s, r, t, user_login_id)
			if (vs < massegeStatus) {
				massegeDao!!.updateMassegeStatus(s, r, t, massegeStatus, user_login_id)
			}
		} catch (e: Exception) {
			Log.d("log-updateMassegeStatusFromException-function", e.toString())
		}
	}

	//completed
	private val onMassegeReachAtServerFromCMDV = Emitter.Listener { args ->
		val new_massege: JSONObject
		try {
			new_massege = args[1] as JSONObject
			val time = new_massege["time"].toString().toLong()
			val sender_id = new_massege["from"].toString()
			val receiver_id = new_massege["to"].toString()

			//massegeListHolder update required;
			val massegeStatus = massegeDao!!.getMassegeStatus(sender_id, receiver_id, time, user_login_id)
			if (massegeStatus < 1) {
				massegeDao!!.updateMassegeStatus(sender_id, receiver_id, time, 1, user_login_id)
			}
			Log.d("log-onMassegeReachAtServerFromCMDV", new_massege.toString())
		} catch (e: Exception) {
			Log.d("log-onMassegeReachAtServerFromCMDV", "Exception || e:$e")
		}
	}

	private fun showPopUpMessage(message: String) {
		val builder = AlertDialog.Builder(this)
		val popUpView = layoutInflater.inflate(R.layout.popup_message, null)
		builder.setView(popUpView)
		val messageTextView = popUpView.findViewById<TextView>(R.id.messageTextView)
		val closeButton = popUpView.findViewById<Button>(R.id.closeButton)
		messageTextView.text = message
		runOnUiThread {
			val alertDialog = builder.create()
			closeButton.setOnClickListener { alertDialog.dismiss() }
			alertDialog.show()
		}
	}

	fun getMainSideMenu(view: View?) {
		Log.d("log-enter", "getMainSideMenu: enter here")
		val popup = PopupMenu(this, view)
		val inflater = popup.menuInflater
		inflater.inflate(R.menu.menu_main_popup, popup.menu)
		popup.show()
		popup.setOnMenuItemClickListener { item ->
			if (item.itemId == R.id.MainMenuSetting) {
				val intent = Intent(this@MainActivity, AllSettingsActivity::class.java)
				startActivity(intent)
			} else if (item.itemId == R.id.MainMenuBG) {
				val intent = Intent(this@MainActivity, BgImageSetForContactPage::class.java)
				startActivity(intent)
			} else if (item.itemId == R.id.MainMenuExtra) {
				Toast.makeText(this@MainActivity, "coming soon...", Toast.LENGTH_SHORT).show()
			}
			false
		}
	}

	private fun saveFireBaseTokenToServer(user_login_id: String) {
		FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
			if (!task.isSuccessful) {
				Log.w("log-saveFireBaseTokenToServer", "Fetching FCM registration token failed", task.exception)
				return@OnCompleteListener
			} // Get new FCM registration token
			val token = task.result // Log and toast
			Log.d("log-saveFireBaseTokenToServer", "token : $token") //                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();

			//store token to server with emailId
			val endpoint = GlobalVariables.URL_MAIN + "SaveFireBaseTokenToServer"
			Log.d("log-endpoint", endpoint)
			val requestQueue = Volley.newRequestQueue(this@MainActivity)
			val request: StringRequest = object : StringRequest(Method.POST, endpoint, Response.Listener<String?> { response ->
				try {
					val respObj = JSONObject(response)
					val status = respObj.getString("status")
					Log.d("log-saveFireBaseTokenToServer-response", "status : $status")
					if (status == "1") {
						Log.d("log-saveFireBaseTokenToServer-response", "token saved successfully")
					} else if (status == "2") {
						Log.d("log-saveFireBaseTokenToServer-response", "token not saved")
					}
				} catch (e: JSONException) {
					e.printStackTrace()
					Log.d("log-error", "onResponse: err in try bracket : $e")
				}
			}, Response.ErrorListener { error ->
				Toast.makeText(this@MainActivity, "Server side error :  $error", Toast.LENGTH_SHORT).show()
				Log.d("volley-error-saveFireBaseTokenToServer", "Server side error : $error")
			}) {
				override fun getParams(): Map<String, String>? {
					val mc = MyCipher()
					val params: MutableMap<String, String> = HashMap()
					params["user_login_id"] = mc.encrypt(user_login_id)
					params["tokenFCM"] = mc.encrypt(token)
					return params
				}

				override fun getHeaders(): Map<String, String> {
					val headers: MutableMap<String, String> = HashMap()
					headers["api_key"] = API_SERVER_API_KEY!!
					return headers
				}
			}
			requestQueue.add(request)
		})
	}

	private fun startNetworkListener() {
		val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
		connectivityManager.requestNetwork(networkRequest, networkCallback)
	}

	private val networkRequest = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
		.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()
	private val networkCallback: NetworkCallback = object : NetworkCallback() {
		override fun onAvailable(network: Network) {
			super.onAvailable(network)
			if (socket != null) {
				if (socket!!.isActive) {
					Log.d("log-ConnectivityManager.NetworkCallback", "onAvailable || if cond isActive : true") //                    socket.connect();
				} else {
					CreateSocketConnection()
					Log.d("log-ConnectivityManager.NetworkCallback", "onAvailable || if cond isActive : false")
				}
			} else {
				Log.d("log-ConnectivityManager.NetworkCallback", "onAvailable || else cond.")
				CreateSocketConnection()
			} // send massege which is not sent due to  internet connection
		}

		override fun onLost(network: Network) {
			super.onLost(network)
			Log.d("log-ConnectivityManager.NetworkCallback", "onLost")
		}

		override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities
		) {
			super.onCapabilitiesChanged(network, networkCapabilities)
			val unMetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
			Log.d("log-ConnectivityManager.NetworkCallback", "onCapabilitiesChanged unMetered:$unMetered")
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == permission_code.CAMERA_PERMISSION_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //                Toast.makeText(MainActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this@MainActivity, "Camera Permission needed to use the massenger", Toast.LENGTH_SHORT).show()
			}
		} else if (requestCode == permission_code.STORAGE_PERMISSION_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //                Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this@MainActivity, "Storage Permission needed to use the massenger", Toast.LENGTH_SHORT).show()
			}
		} else if (requestCode == permission_code.PERMISSION_ALL) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			} else {
				Toast.makeText(this@MainActivity, "To use Massenger please give all permissions", Toast.LENGTH_SHORT).show() //                initContentResolver();
				//                this.finish();
			}
		} else if (requestCode == permission_code.PERMISSIONS_MUST_REQUIRED_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			} else {
				Toast.makeText(this@MainActivity, "contact and storage permissions are must require", Toast.LENGTH_SHORT).show() //                initContentResolver();
				//                this.finish();
			}
		} else if (requestCode == permission_code.PERMISSION_CONTACT_SYNC) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				syncContactAtAppStart()
			} else {
				Toast.makeText(this@MainActivity, "To use Massenger please give Contact and Storage permission", Toast.LENGTH_SHORT).show()
			}
		} else if (requestCode == permission_code.PERMISSION_initContentResolver) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //                initContentResolver();
			} else {
				Toast.makeText(this@MainActivity, "To use Massenger you must give the Contact Read and Write permission, please restart the app", Toast.LENGTH_SHORT)
					.show() //                initContentResolver();
			}
		}
	}

	companion object {
		@SuppressLint("StaticFieldLeak")
		@JvmField
		var contactListAdapter: ContactListAdapter? = null
		var FinishCode = 0

		@JvmField
		var Contact_page_opened_id: String? = "-1"

		@JvmField
		var statusForThread: StatusForThread? = null

		@JvmField
		var user_login_id: String? = null
		var UserMobileNumber: Long = 0

		@JvmField
		var API_SERVER_API_KEY: String? = null

		//global socket variables
		var socketOBJ: SocketClass? = null

		@JvmField
		var socket: Socket? = null

		@JvmField
		var ChatsRecyclerView: RecyclerView? = null

		@SuppressLint("StaticFieldLeak")
		@JvmField
		var recyclerViewAdapter: RecyclerViewAdapter? = null

		@JvmField
		@Volatile
		var contactArrayList: ArrayList<ContactWithMassengerEntity?>? = null

		@JvmField
		var db: MainDatabaseClass? = null

		@JvmField
		var MainContactListHolder: ContactListHolder? = null

		@SuppressLint("StaticFieldLeak")
		@JvmField
		var MainActivityStaticContext: Context? = null

		@JvmField
		var filteredContactArrayList: ArrayList<ContactWithMassengerEntity>? = null

		@JvmStatic
		var contactList: ArrayList<ContactWithMassengerEntity?>? = null

		@JvmField
		var ACTION_RUN_FUNCTION: String = "com.massenger.mank.ACTION_TYPING_STATUS"

		@JvmStatic
		@SuppressLint("NotifyDataSetChanged")
		fun setNewMassegeArriveValueToEmpty(position: Int) {
			val contactView = contactArrayList!![position]
			contactView?.newMassegeArriveValue = 0
			contactArrayList!![position] = contactView
			recyclerViewAdapter!!.notifyDataSetChanged()
		}

		@SuppressLint("NotifyDataSetChanged")
		@JvmStatic
		fun FetchDataFromServerAndSaveIntoDB(CID: String?) {
			val t = Thread {
				val requestQueue = Volley.newRequestQueue(MainActivityStaticContext)
				val endpoint = GlobalVariables.URL_MAIN + "GetContactDetailsOfUserToSaveLocally"
				val mainArray = JSONArray()
				mainArray.put(user_login_id)
				mainArray.put(CID)
				Log.d("log-MainActivity", "mainArray : $mainArray")
				val jsonArrayRequest = JsonArrayRequest(Request.Method.POST, endpoint, mainArray, { response ->
					Log.d("log-MainActivity", "onResponse: response length : " + response.length())
					try {
						Log.d("log-MainActivity", "onResponse: response[0] : " + response[0])
						val CID = response[0].toString()
						val Number = response[1].toString().toLong()
						val Name = response[2] as String
						val DisplayName = response[5] as String
						val massegeDao = db!!.massegeDao()
						val rank = massegeDao.getHighestPriorityRank(user_login_id)
						val newContact = ContactWithMassengerEntity(Number, null, CID, rank + 1)
						if (massegeDao.getContactWith_CID(CID, user_login_id) == null) {
							massegeDao.SaveContactDetailsInDatabase(newContact)
							massegeDao.setPriorityRank(CID, massegeDao.getHighestPriorityRank(user_login_id), user_login_id)
							Log.d("log-MainActivity", "onResponse: newContact saved into with rank :" + (rank + 1)) // now we have to add contact into recyclerViewAdapter
							contactArrayList!!.add(0, newContact)
							recyclerViewAdapter!!.notifyDataSetChanged()
						}
					} catch (e: JSONException) {
						e.printStackTrace()
					}
				}) { error ->
					Log.d("log-AllContactOfUserDeviceView", "onErrorResponse: setChatDetails error: $error")
				}
				jsonArrayRequest.retryPolicy = DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
				requestQueue.add(jsonArrayRequest)
			}
			t.start()
		}

		@JvmStatic
		fun saveContactProfileImageToStorage(CID: String, profileImageByteArray: ByteArray
		): Boolean {

			//        Bitmap bitmapImage = BitmapFactory.decodeByteArray(profileImageByteArray, 0, profileImageByteArray.length);
			val bitmapImage = BitmapFactory.decodeByteArray(profileImageByteArray, 0, profileImageByteArray.size)
			val directory = File(Environment.getExternalStorageDirectory(), "Android/media/com.massenger.mank.main/Pictures/Profiles")
			if (!directory.exists()) {
				val success = directory.mkdirs()
				if (!success) {
					Log.d("log-saveByteArrayToInternalStorage", "Failed to create directory")
					return false
				}
			}

			// Create the file path
			val imagePath = File(directory, "" + CID + user_login_id + ".png") // Save the bitmap image to the file
			try {
				FileOutputStream(imagePath).use { outputStream ->
					bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
					outputStream.flush()
					Log.d("log-saveImageToInternalStorage", "Saved image at path: " + imagePath.absolutePath)
					Log.d("log-saveImageToInternalStorage", "Saved image of size : " + bitmapImage.width + "*" + bitmapImage.height)
					return true
				}
			} catch (e: IOException) {
				e.printStackTrace()
				Log.d("log-saveImageToInternalStorage", "Image Save failed $e")
			}
			return false
		}
	}
}