package com.example.mank

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mank.localDatabaseFiles.daoClasses.ContactsDao
import com.example.mank.localDatabaseFiles.daoClasses.MassegeDao
import com.example.mank.localDatabaseFiles.dataContainerClasses.contactDetailsHolderForSync
import com.example.mank.localDatabaseFiles.MainDatabaseClass
import com.example.mank.localDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.recyclerViewClassesFolder.ContactSyncMainRecyclerViewAdapter
import com.example.mank.threadPackages.GetUserContactDetailsFromPhone
import com.example.mank.threadPackages.IContactSync
import com.example.mank.threadPackages.SyncContactDetailsThread
import com.example.mank.cipher.MyCipher
import com.example.mank.configuration.permission_code.CONTACTS_PERMISSION_CODE
import com.example.mank.configuration.permission_code.STORAGE_PERMISSION_CODE
import com.example.mank.configuration.permission_code.ADD_NEW_CONTACT_REQUEST_CODE
import java.util.Locale
import java.util.TreeSet

class AllContactOfUserInDeviceView : Activity() {
	private var loadingPB: ProgressBar? = null
	private var contactArrayList: ArrayList<AllContactOfUserEntity?> = ArrayList()
	private var filteredContactArrayList: ArrayList<AllContactOfUserEntity?> = ArrayList()
	private var recyclerView1: RecyclerView? = null
	private var contactSyncMainRecyclerViewAdapter: ContactSyncMainRecyclerViewAdapter = ContactSyncMainRecyclerViewAdapter(this, contactArrayList)
	private var getUserContactDetailsFromPhone: GetUserContactDetailsFromPhone? = null
	private var mc = MyCipher()
	private var ACSPSearchView: SearchView? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Log.d("log-AllContactOfUserInDeviceView", "onCreate: enter here")
		setContentView(R.layout.activity_all_contact_of_user_in_device_view)
		loadingPB = findViewById(R.id.idLoadingPB_of_AllContactView)
		recyclerView1 = findViewById(R.id.ContactSyncRecyclerViewMain)
		ACSPSearchView = findViewById(R.id.ACSPSearchView)
		contactArrayList = ArrayList()
		start()
		ACSPSearchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String): Boolean {
				Log.d("log-MainActivity", "onQueryTextChange newText:$query")
				return false
			}

			override fun onQueryTextChange(newText: String): Boolean {
				Log.d("log-MainActivity", "onQueryTextChange newText:$newText") //                if (!prevCatchText.equals(newText)) {
				//                    prevCatchText = newText;
				if (newText.trim { it <= ' ' } == "") {
					contactArrayListFilter(newText, 0)
				} else {
					contactArrayListFilter(newText, 1)
				} //                }
				return false
			}
		})
	}

	private fun start() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
			syncContactDetails(MainActivity.db)
		} else {
			askPermission(Manifest.permission.READ_CONTACTS)
		}
	}

	fun FinishThisActivity(view: View?) {
		finish()
	}

	private fun askPermission(Manifest_request: String) {
		if (Manifest_request == Manifest.permission.READ_CONTACTS) {
			ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.READ_CONTACTS), CONTACTS_PERMISSION_CODE)
		}
	}

	private var disConnectedContact: List<AllContactOfUserEntity?>? = ArrayList()
	private var connectedContact: List<AllContactOfUserEntity?>? = ArrayList()
	private var allContactOfUser: List<AllContactOfUserEntity?>? = ArrayList()
	fun syncContactDetails(db: MainDatabaseClass?) {
		getUserContactDetailsFromPhone = GetUserContactDetailsFromPhone(this@AllContactOfUserInDeviceView, db!!)
		getUserContactDetailsFromPhone?.start()

		//fetch existing data from database and display it
		val contactDetailsHolder = contactDetailsHolderForSync(db)
		connectedContact = contactDetailsHolder.connectedContact
		disConnectedContact = contactDetailsHolder.disConnectedContact
		allContactOfUser = contactDetailsHolder.allContact
		setContentDetailsInView()
		syncContactListToServer(connectedContact, disConnectedContact)

	}

	private fun syncContactListToServer(connectedContact: List<AllContactOfUserEntity?>?, disConnectedContact: List<AllContactOfUserEntity?>?
	) {
		loadingPB!!.visibility = View.VISIBLE
		val scdt = SyncContactDetailsThread(this, connectedContact, disConnectedContact, object : IContactSync {
			override fun execute(status: Int, massege: String?) {
				loadingPB!!.visibility = View.GONE
				Toast.makeText(this@AllContactOfUserInDeviceView, massege.toString(), Toast.LENGTH_LONG).show()
				if (status == 1) {
					syncContactListToServerCallBack()
				}
			}
		})
		scdt.setFromWhere(1)
		scdt.start()
	}

	fun syncContactListToServerCallBack() {
		synchronized(this) {
			val newContactDetailsHolder = contactDetailsHolderForSync(MainActivity.db!!)
			connectedContact = newContactDetailsHolder.connectedContact
			disConnectedContact = newContactDetailsHolder.disConnectedContact
			for (e in connectedContact!!) {
				Log.d("log-AllContactOfUserDeviceView", "onResponse: connected contact is :" + e!!.DisplayName + ", " + e.CID + " , " + e.MobileNumber)
			}
			runOnUiThread { setContentDetailsInView() }
		}
	}


	//    val contactComparator = Comparator<AllContactOfUserEntity> { contact1, contact2 ->
	//        try {
	//            val displayName1 = contact1.DisplayName ?: ""
	//            val displayName2 = contact2.DisplayName ?: ""
	//            displayName1.compareTo(displayName2, ignoreCase = true)
	//        } catch (e: Exception) {
	//            Log.d("log-AllContactOfUserDeviceView", "Exception in comparator: $e")
	//            1
	//        }
	//    }

	//    val contactComparator = compareBy<AllContactOfUserEntity> {
	//        try {
	//            it.DisplayName?.toLowerCase()
	//        } catch (e: Exception) {
	//            Log.d("log-AllContactOfUserDeviceView", "Exception in comparator: $e")
	//            ""
	//        }
	//    }

	private val contactComparator = object : Comparator<AllContactOfUserEntity?> {
		override fun compare(contact1: AllContactOfUserEntity?, contact2: AllContactOfUserEntity?): Int {
			return try {
				contact1?.DisplayName?.compareTo(contact2?.DisplayName ?: "", ignoreCase = true) ?: 0
			} catch (e: Exception) {
				Log.d("log-AllContactOfUserDeviceView", "Exception in comparator: $e")
				1
			}
		}
	}


	@SuppressLint("NotifyDataSetChanged")
	private fun setContentDetailsInView() {
		synchronized(this) {
			Log.d("log-AllContactOfUserDeviceView", "setContentDetailsInView: enter")
			Log.d("log-AllContactOfUserDeviceView", "before sorting and remove duplicates disConnectedContact.size() = " + disConnectedContact!!.size)

			// Create a TreeSet with the custom comparator to store the sorted, unique contacts
			//            val uniqueContacts: MutableSet<AllContactOfUserEntity?> =
			//                TreeSet(contactComparator)

			val uniqueContacts: MutableSet<AllContactOfUserEntity?> = TreeSet(contactComparator)
			uniqueContacts.addAll(disConnectedContact!!)
			disConnectedContact = ArrayList(uniqueContacts)

			val uniqueContacts2: MutableSet<AllContactOfUserEntity?> = TreeSet(contactComparator)
			uniqueContacts2.addAll(connectedContact!!)
			connectedContact = ArrayList(uniqueContacts2)

			Log.d("log-AllContactOfUserDeviceView", "after sorting and remove duplicates disConnectedContact.size() = " + disConnectedContact?.size)
			Log.d("log-AllContactOfUserDeviceView", "setContentDetailsInView: enter")
			contactArrayList = ArrayList()
			contactSyncMainRecyclerViewAdapter = ContactSyncMainRecyclerViewAdapter(this@AllContactOfUserInDeviceView, contactArrayList)
			recyclerView1!!.setHasFixedSize(true)
			recyclerView1!!.layoutManager = LinearLayoutManager(this)
			recyclerView1!!.adapter = contactSyncMainRecyclerViewAdapter

			// first we have to remove all object in contactArrayList
			contactArrayList.clear()

			//after this we set lable
			val tmp_number = 0
			val newLabelEntity = AllContactOfUserEntity(tmp_number.toLong(), "mhk_label_boundary", "-100")
			contactArrayList.add(newLabelEntity)
			contactSyncMainRecyclerViewAdapter.notifyDataSetChanged()

			//set contact have massenger
			contactArrayList.addAll(connectedContact as ArrayList<AllContactOfUserEntity?>)
			contactSyncMainRecyclerViewAdapter.notifyDataSetChanged()
			Log.d("log-AllContactOfUserDeviceView", "setContentDetailsInView: after add connectedContact")

			//after this we set lable
			val newLabelEntity1 = AllContactOfUserEntity(tmp_number.toLong(), "mhk_label_boundary", "-101")
			contactArrayList.add(newLabelEntity1)
			contactSyncMainRecyclerViewAdapter.notifyDataSetChanged()

			//add all disConnected array
			contactArrayList.addAll(disConnectedContact as ArrayList<AllContactOfUserEntity?>)
			contactSyncMainRecyclerViewAdapter.notifyDataSetChanged()
			filteredContactArrayList = contactArrayList.clone() as ArrayList<AllContactOfUserEntity?>
			Log.d("log-AllContactOfUserDeviceView", "setContentDetailsInView: after clone of filteredContactArrayList")
			recyclerView1!!.scrollToPosition(0)
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	fun contactArrayListFilter(newText: String, flag: Int) {


		if (flag == 0) {
			Log.d("log-MainActivity", "contactArrayListFilter start with flag 0")
			contactArrayList.clear()
			contactArrayList.addAll(filteredContactArrayList)
			contactSyncMainRecyclerViewAdapter.notifyDataSetChanged()
			return
		}
		Log.d("log-MainActivity", "contactArrayListFilter start")

		val itemsToRemove = mutableListOf<AllContactOfUserEntity?>() // Create a list to store elements to be removed

		contactArrayList.clear()
		contactArrayList.addAll(filteredContactArrayList)

		for (e in contactArrayList.toList()) { // Use toList() to create a copy of the list for iteration

			val displayNameMatches = e?.DisplayName?.lowercase(Locale.getDefault())?.contains(newText.lowercase(Locale.getDefault())) ?: false
			val mobileNumberMatches = e?.MobileNumber.toString()?.contains(newText) ?: false

			if (!displayNameMatches && !mobileNumberMatches) {
				if (e?.DisplayName != "mhk_label_boundary") {
					itemsToRemove.add(e) // Add elements to be removed to the separate list
				}
			}
		}
		contactArrayList.removeAll(itemsToRemove.toSet())
		contactSyncMainRecyclerViewAdapter.notifyDataSetChanged()
	}

	fun ACOUDVAddNewUserIntoContact(view: View?) {
		val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
		contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
		val ContactMobileNumber = ""
		contactIntent.putExtra(ContactsContract.Intents.Insert.NAME, "").putExtra(ContactsContract.Intents.Insert.PHONE, ContactMobileNumber)
		startActivityForResult(contactIntent, ADD_NEW_CONTACT_REQUEST_CODE)
	}


	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == ADD_NEW_CONTACT_REQUEST_CODE) {
			start()
			Log.d("log-onActivityResult", "onActivityResult: contact add activity finished")
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show()
			}
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Contact not added", Toast.LENGTH_SHORT).show()
			}
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == CONTACTS_PERMISSION_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				start()
			} else {
				Toast.makeText(this, "contact read-write permission required", Toast.LENGTH_LONG).show()
			}
		} else if (requestCode == STORAGE_PERMISSION_CODE) {
			if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this@AllContactOfUserInDeviceView, "Permission Required", Toast.LENGTH_SHORT).show()
			}
		}
	}

	companion object {

	}
}