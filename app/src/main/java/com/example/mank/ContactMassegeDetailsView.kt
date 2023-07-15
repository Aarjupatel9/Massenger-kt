package com.example.mank

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.mank.DatabaseAdapter.MassegeListAdapter
import com.example.mank.FunctionalityClasses.ContactDetailsFromMassegeViewPage
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.entities.MassegeEntity
import com.example.mank.MediaPlayerClasses.SoundThread
import com.example.mank.RecyclerViewClassesFolder.ContactMassegeRecyclerViewAdapter
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Timer
import java.util.TimerTask

import android.content.BroadcastReceiver
import android.content.IntentFilter
import com.example.mank.MainActivity.Companion.ACTION_RUN_FUNCTION


class ContactMassegeDetailsView : Activity() {
	private var lastChatId = 0
	private var CDMVNewUserConstraintLayout: ConstraintLayout? = null
	private var CMDVConstraintLayoutMain: ConstraintLayout? = null
	private var massege_field: EditText? = null
	private var send_massege_button: ImageButton? = null
	private var OtherActivityButton: ImageButton? = null
	private var CID: String? = null
	private var ContactMobileNumber: Long = 0
	private var ContactName: String? = null
	private var Contact_name_of_user: TextView? = null
	private var online_status_text_area: TextView? = null

	var keyboardPass = true
	private var massegeDao: MassegeDao? = null
	private var online_status_checker_timer: Timer? = null
	private var onlineStatusArgs: Array<Any?>? = null

	private val onCheckContactOnlineStatus_return = Emitter.Listener { args ->
		onlineStatusArgs = args;
		updateOnlineStatusInUI(args);
	}

	fun updateOnlineStatusInUI(args: Array<Any?>?) {
		if (args != null) {
			try {
				val user_id = args[0] as String
				val contact_id = args[1] as String
				val type = args[4] as Int
				val onlineStatusPolicy = args[2] as Int
				val online_status = args[3] as Long
				runOnUiThread {
					if (type == 1) {
						online_status_text_area!!.text = "online"
					} else if (type == 0) { //                            Log.d("log-onCheckContactOnlineStatus_return", "call: enter in offline cond.");
						if (onlineStatusPolicy == 0) { //if user don't want to show their last seen
							online_status_text_area!!.text = ""
						} else if (onlineStatusPolicy == 1) {
							val date = Date(online_status) //here we have to implement some function
							val current_date = Date()
							if (current_date.year == date.year && current_date.month == date.month && current_date.date == date.date) { //                                    Log.d("log-onCheckContactOnlineStatus_return", "date: " + date);
								val formatted = SimpleDateFormat("HH:mm").format(date) //                                    Log.d("log-onCheckContactOnlineStatus_return", "formatted: " + formatted);
								online_status_text_area!!.text = "last seen at $formatted"
							} else if (current_date.year == date.year && current_date.month == date.month && current_date.date == date.date + 1) { //                                    Log.d("log-onCheckContactOnlineStatus_return", "enter in else  if cond.");
								//                                    Log.d("log-onCheckContactOnlineStatus_return", "date: " + date);
								val formatted = SimpleDateFormat("HH:mm").format(date) //                                    Log.d("log-onCheckContactOnlineStatus_return", "formatted: " + formatted);
								online_status_text_area!!.text = "last seen yesterday at  $formatted"
							} else {
								val formatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
								val cur_formatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(current_date)
								online_status_text_area!!.text = "last seen at $formatted"
							}
						}
					} else {
						Log.d("log-updateOnlineStatusInUI", "call: enter in other cond.")
					}
				}
			} catch (e: Exception) {
				Log.d("log-updateOnlineStatusInUI-exception", e.toString())
			}
		} else {
			Log.d("log-updateOnlineStatusInUI", "onlineStatusArgs is null")
		}
	}

	var typingCounter = 0
	fun updateOnlineStatusToTypingInUI() {
		Log.d("log-ContactMassegeDetailsView", "updateOnlineStatusToTypingInUI start")
		online_status_text_area!!.text = "typing..."
		// Increment the counter
		typingCounter++
		val handler = Handler()
		// Calculate the delay based on the counter
		val delayMillis: Long = 2000 // Multiply by 2000 milliseconds (2 seconds)
		handler.postDelayed({
			Log.d("log-ContactMassegeDetailsView", "updateOnlineStatusToTypingInUI updateOnlineStatusInUI call after $delayMillis seconds")
			// Decrement the counter after the last run
			typingCounter--
			// Check if it's the last run
			if (typingCounter == 0) {
				updateOnlineStatusInUI(onlineStatusArgs)
			}
		}, delayMillis)

	}

	private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent?.action == ACTION_RUN_FUNCTION) {                // Call the desired function in the MessageActivity
				Log.d("log-ContactMassegeDetailsView", "BroadcastReceiver messageReceiver onReceive  registerReceiver initialized")
				updateOnlineStatusToTypingInUI();
			}
		}
	}

	override fun onResume() {
		super.onResume()
		val filter = IntentFilter(ACTION_RUN_FUNCTION)
		registerReceiver(messageReceiver, filter)
		Log.d("log-ContactMassegeDetailsView", "onResume registerReceiver initialized")
	}

	override fun onPause() {
		super.onPause()
		unregisterReceiver(messageReceiver)
		Log.d("log-ContactMassegeDetailsView", "onResume registerReceiver initialized")
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val intent = intent
		CID = intent.getStringExtra("CID")
		if (CID == null) {
			CID = ""
		}
		ContactMobileNumber = intent.getLongExtra("ContactMobileNumber", -2)
		ContactName = intent.getStringExtra("ContactName")
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		massegeListAdapter = MassegeListAdapter(MainActivity.db)
		Log.d("log-ContactMassegeDetailsView-onCreate", "contact_id:" + CID + ", user_login_id:" + MainActivity.user_login_id)
		setContentView(R.layout.activity_contact_massege_details_view)
		online_status_checker_timer = Timer()
		val timerTask: TimerTask = object : TimerTask() {
			override fun run() {
				MainActivity.socket!!.emit("CheckContactOnlineStatus", MainActivity.user_login_id, MainActivity.Contact_page_opened_id)
			}
		}
		online_status_checker_timer!!.scheduleAtFixedRate(timerTask, 1, 1500)
		online_status_text_area = findViewById(R.id.Contact_last_come_in_app_status)
		Contact_name_of_user = findViewById(R.id.Contact_name_of_user)
		CDMVNewUserConstraintLayout = findViewById(R.id.CDMVNewUserConstraintLayout)
		if (ContactName == null) {
			CDMVNewUserConstraintLayout?.setVisibility(View.VISIBLE)
			Contact_name_of_user?.setText(ContactMobileNumber.toString())
		} else {
			Contact_name_of_user?.setText(ContactName)
		}
		massege_field = findViewById<View>(R.id.write_massege) as EditText
		OtherActivityButton = findViewById<View>(R.id.OtherActivityButton) as ImageButton
		send_massege_button = findViewById<View>(R.id.send_massege_button) as ImageButton
		send_massege_button!!.isClickable = false
		ContactMassegeRecyclerView = findViewById(R.id.ContactMassegeRecyclerView)
		CMDVConstraintLayoutMain = findViewById(R.id.CMDVConstraintLayoutMain)
		setLocationButtonColor(true)
		MainActivity.socket!!.on("CheckContactOnlineStatus_return", onCheckContactOnlineStatus_return)
		massegeDao = MainActivity.db!!.massegeDao()
		setAllMassege(CID!!)
		setLastChatId()
		setNewMassegeArriveValue(CID!!)
		massege_field!!.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
				Log.d("log-ContactMassegeDetailsView", "beforeTextChanged ")
			}

			override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
				Log.d("log-ContactMassegeDetailsView", "onTextChanged ")
				MainActivity.socket!!.emit("contact_massege_typing_event", MainActivity.user_login_id, MainActivity.Contact_page_opened_id)
				if (massege_field!!.text.toString() != "") {
					send_massege_button!!.isClickable = !massege_field!!.text.toString().trim { it <= ' ' }.isBlank()
				} else {
					send_massege_button!!.isClickable = false
				}
			}

			override fun afterTextChanged(editable: Editable) {
				Log.d("log-ContactMassegeDetailsView", "afterTextChanged ")
			}
		})
		val activityRootView = findViewById<View>(R.id.contact_massege_details_view_root)
		activityRootView.viewTreeObserver.addOnGlobalLayoutListener {
			val heightDiff = activityRootView.rootView.height - activityRootView.height
			if (heightDiff > dpToPx(this@ContactMassegeDetailsView, 200f)) { // if more than 200 dp, it's probably a keyboard...
				if (keyboardPass) {
					keyboardPass = false
					Log.d("log-addOnGlobalLayoutListener", "keyboard is visible")
					ContactMassegeRecyclerView?.scrollToPosition(massegeRecyclerViewAdapter!!.itemCountMyOwn)
				}
			} else {
				if (!keyboardPass) {
					keyboardPass = true
					Log.d("log-addOnGlobalLayoutListener", "keyboard is not visible")
				}
			}
		}
		setBackgroundImage()
	}


	private fun setBackgroundImage() {
		val ti = Thread {
			val imagePath = "/storage/emulated/0/Android/media/com.massenger.mank.main/bg/bgImages/" + MainActivity.user_login_id + ".png"
			var byteArray: ByteArray? = null
			try {
				val imageFile = File(imagePath)
				val fis = FileInputStream(imageFile)
				val bos = ByteArrayOutputStream()
				val buffer = ByteArray(1024)
				var bytesRead: Int
				while (fis.read(buffer).also { bytesRead = it } != -1) {
					bos.write(buffer, 0, bytesRead)
				}
				fis.close()
				bos.close()
				byteArray = bos.toByteArray()
			} catch (e: IOException) {
				e.printStackTrace()
			}
			if (byteArray != null) {
				val selfImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
				Log.d("log-ContactListAdapter", "setUserImage : after fetch image form file system : " + byteArray.size)
				val drawable = BitmapDrawable(resources, selfImage)
				runOnUiThread {
					if (CMDVConstraintLayoutMain != null) {
						CMDVConstraintLayoutMain!!.background = drawable
					} else {
						Log.d("log-ContactMassegeDetailsView", "ContactMassegeRecyclerView is null")
					}
				}
			}
		}
		ti.start()
	}

	fun addNewUserIntoContact(view: View?) {
		val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
		contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
		contactIntent.putExtra(ContactsContract.Intents.Insert.NAME, "").putExtra(ContactsContract.Intents.Insert.PHONE, ContactMobileNumber.toString())
		startActivityForResult(contactIntent, 106)
	}

	public override fun onDestroy() {
		super.onDestroy() //        online_status_checker.interrupt();
		online_status_checker_timer!!.cancel()
	}


	private fun setNewMassegeArriveValue(cId: String) {
		val t = Thread { massegeDao!!.updateNewMassegeArriveValue(cId, 0, MainActivity.user_login_id) }
		t.start()
	}

	private fun setLastChatId() {
		lastChatId = massegeDao!!.getLastInsertedMassegeChatId(MainActivity.user_login_id) + 1
		Log.d("log-lastchatid", "setLastChatId: last chat is : $lastChatId")
	}

	private fun setAllMassege(CID: String) {
		massegeArrayList = ArrayList()
		massegeListAdapter!!.setMyContext(this)
		massegeListAdapter!!.fillMassegeListOfUser(CID)
		ContactMassegeRecyclerView!!.setHasFixedSize(true)
		ContactMassegeRecyclerView!!.layoutManager = LinearLayoutManager(this)
		massegeRecyclerViewAdapter = ContactMassegeRecyclerViewAdapter(this, massegeArrayList!!)
		ContactMassegeRecyclerView!!.adapter = massegeRecyclerViewAdapter //        Log.d("log-list-size", "setAllMassege: list size is :" + massegeRecyclerViewAdapter.getItemCountMyOwn());
		ContactMassegeRecyclerView!!.scrollToPosition(massegeRecyclerViewAdapter!!.itemCountMyOwn)
	}

	fun getContactDetailsOfUser(view: View?) {
		Log.d("log-getContactDetailsOfUser", "getContactDetailsOfUser: enter here")
		val intent = Intent(this, ContactDetailsFromMassegeViewPage::class.java)
		intent.putExtra("ContactMobileNumber", ContactMobileNumber)
		intent.putExtra("CID", CID)
		intent.putExtra("ContactName", ContactName)
		startActivity(intent) //        Toast.makeText(this, "you Clicked in User name", Toast.LENGTH_SHORT).show();
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
		super.onActivityResult(requestCode, resultCode, data)
		Log.d("log-onActivityResult", "onActivityResult: activity finished with code $requestCode")
		if (requestCode == 106) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Added Contact", Toast.LENGTH_SHORT).show()
			}
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Cancelled Added Contact", Toast.LENGTH_SHORT).show()
			}
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	fun SendMassege(view: View?) {
		counter++
		val user_massege = massege_field!!.text.toString().trim { it <= ' ' }
		Log.d("log-SendMassege", "user_massege is : $user_massege")
		Log.d("log-SendMassege", "user_login_id is : " + MainActivity.user_login_id)
		massege_field!!.setText("")
		var massege_status = -1
		val current_date = Date()
		val setPriorityRankThread = Thread {
			val HighestPriority = massegeDao!!.getHighestPriorityRank(user_massege)
			massegeDao!!.setPriorityRank(CID, HighestPriority + 1, user_massege)
			if (MainActivity.contactListAdapter != null) {
				MainActivity.contactListAdapter!!.updatePositionOfContact(CID, this@ContactMassegeDetailsView)
			}
		}
		setPriorityRankThread.start()
		MainActivity.contactListAdapter!!.setLastMassege(CID, user_massege)
		Log.d("log-try", "Send_massege_of_user: internet connection is : " + check_internet_connectivity())
		if (check_internet_connectivity()) {
			massege_status = if (MainActivity.user_login_id == MainActivity.Contact_page_opened_id) {
				3
			} else {
				0
			}
			val new_massege = MassegeEntity(MainActivity.user_login_id,
				CID,
				user_massege,
				current_date.time,
				massege_status) //now wwe have to store it into database			//notify adapter for add massege into recycler view
			massegeListAdapter!!.addMassege(new_massege)
			val ma = SoundThread(this@ContactMassegeDetailsView, 0)
			ma.start()
			try {
				val massegeOBJ = JSONObject()
				massegeOBJ.put("from", MainActivity.user_login_id)
				massegeOBJ.put("to", CID)
				massegeOBJ.put("massege", user_massege)
				massegeOBJ.put("chatId", lastChatId)
				massegeOBJ.put("time", current_date.time)
				massegeOBJ.put("massegeStatus", massege_status)
				massegeOBJ.put("massegeStatusL", 1)
				massegeOBJ.put("ef1", 1)
				massegeOBJ.put("ef2", 1)
				lastChatId++
				val massegeArray = JSONArray()
				massegeArray.put(massegeOBJ)
				MainActivity.socket!!.emit("send_massege_to_server_from_sender", MainActivity.user_login_id, massegeArray)
			} catch (e: JSONException) {
				e.printStackTrace()
			}
		} else {
			val new_massege = MassegeEntity(MainActivity.user_login_id, CID, user_massege, current_date.time, massege_status)
			massegeListAdapter!!.addMassege(new_massege)
		}
	}

	fun check_internet_connectivity(): Boolean {
		val conMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
		return conMgr.activeNetworkInfo != null && conMgr.activeNetworkInfo!!.isAvailable && conMgr.activeNetworkInfo!!.isConnected
	}

	private val toogelColor = false
	fun OtherActivityButtonOnCLick(view: View?) {
		Log.d("log-ContactMassegeDetailsView", "OtherActivityButtonOnCLick || start")
		askPermissionToShareLocation()
	}

	private var LocationSharingEnable = false
	fun askPermissionToShareLocation() {
		val builder = AlertDialog.Builder(this)
		builder.setMessage("Start sharing your Location with $ContactName")
		builder.setPositiveButton("Start sharing") { dialog, which -> //                Log.d("log-ContactMassegeDetailsView", "");
			if (!LocationSharingEnable) { //enable location sharing
				startLocationShareWithContact()
				LocationSharingEnable = true
			}
			setLocationButtonColor(false)
		}
		builder.setNegativeButton("No") { dialog, which ->
			if (LocationSharingEnable) {
				startLocationShareWithContact()
				LocationSharingEnable = false
				stopLocationShareWithContact()
			}
			dialog.dismiss()
			setLocationButtonColor(true)
		}
		builder.show()
	}

	private fun startLocationShareWithContact() {}
	private fun stopLocationShareWithContact() {}
	private fun setLocationButtonColor(value: Boolean) {
		val wrapper: ContextThemeWrapper
		wrapper = if (value) {
			ContextThemeWrapper(this, R.style.LocationButtonDefaultScene)
		} else {
			ContextThemeWrapper(this, R.style.LocationButtonUpdatedScene)
		}
		val drawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.baseline_location_on_24, wrapper.theme)
		OtherActivityButton!!.setImageDrawable(drawable)
	}

	fun FinishCMDVActivity(view: View?) {
		finish()
	}

	companion object {
		@JvmField
		var ContactMassegeRecyclerView: RecyclerView? = null

		@SuppressLint("StaticFieldLeak")
		@JvmField
		var massegeRecyclerViewAdapter: ContactMassegeRecyclerViewAdapter? = null

		@JvmField
		var massegeArrayList: ArrayList<MassegeEntity>? = null

		@SuppressLint("StaticFieldLeak")
		var massegeListAdapter: MassegeListAdapter? = null
		var counter = 0
		fun dpToPx(context: Context, valueInDp: Float): Float {
			val metrics = context.resources.displayMetrics
			return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
		}
	}
}