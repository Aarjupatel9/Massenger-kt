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
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.contactDetailsHolderForSync
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.RecyclerViewClassesFolder.ContactSyncMainRecyclerViewAdapter
import com.example.mank.ThreadPackages.GetUserContactDetailsFromPhone
import com.example.mank.ThreadPackages.IContactSync
import com.example.mank.ThreadPackages.SyncContactDetailsThread
import com.example.mank.cipher.MyCipher
import com.example.mank.configuration.permission_code.CONTACTS_PERMISSION_CODE
import com.example.mank.configuration.permission_code.STORAGE_PERMISSION_CODE
import com.example.mank.R
import java.util.Locale
import java.util.TreeSet

class AllContactOfUserInDeviceView : Activity() {
    private var loadingPB: ProgressBar? = null
    private var contactArrayList: ArrayList<AllContactOfUserEntity?>? = null
    private var filteredContactArrayList: ArrayList<AllContactOfUserEntity?>? = null
    var recyclerView1: RecyclerView? = null
    private var ContactSyncMainRecyclerViewAdapter: ContactSyncMainRecyclerViewAdapter? = null
    var getUserContactDetailsFromPhone: GetUserContactDetailsFromPhone? = null
    var mc = MyCipher()
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
        massegeDao = MainActivity.db!!.massegeDao()
        ACSPSearchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("log-MainActivity", "onQueryTextChange newText:$query")
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d("log-MainActivity", "onQueryTextChange newText:$newText")
                //                if (!prevCatchText.equals(newText)) {
//                    prevCatchText = newText;
                if (newText == null || newText.trim { it <= ' ' } == "") {
                    contactArrayListFilter(newText, 0)
                } else {
                    contactArrayListFilter(newText, 1)
                }
                //                }
                return false
            }
        })
    }

    private fun start() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(
                    Manifest.permission.READ_CONTACTS
                ), CONTACTS_PERMISSION_CODE
            )
        }
    }

    private var disConnectedContact: List<AllContactOfUserEntity?>? = ArrayList()
    private var connectedContact: List<AllContactOfUserEntity?>? = ArrayList()
    private var allContactOfUser: List<AllContactOfUserEntity?>? = ArrayList()
    fun syncContactDetails(db: MainDatabaseClass?) {
        getUserContactDetailsFromPhone = GetUserContactDetailsFromPhone(
            this@AllContactOfUserInDeviceView,
            db!!
        )
        getUserContactDetailsFromPhone!!.start()

        //fetch existing data from database and display it
        val contactDetailsHolder = contactDetailsHolderForSync(db)
        connectedContact = contactDetailsHolder.connectedContact
        disConnectedContact = contactDetailsHolder.disConnectedContact
        allContactOfUser = contactDetailsHolder.allContact
        setContentDetailsInView()
        syncContactListToServer(connectedContact, disConnectedContact)

    }

    private fun syncContactListToServer(
        connectedContact: List<AllContactOfUserEntity?>?,
        disConnectedContact: List<AllContactOfUserEntity?>?
    ) {
        loadingPB!!.visibility = View.VISIBLE
        val scdt = SyncContactDetailsThread(
            this,
            connectedContact,
            disConnectedContact,
            object : IContactSync {
                override fun execute(status: Int, massege: String?) {
                    loadingPB!!.visibility = View.GONE
                    Toast.makeText(
                        this@AllContactOfUserInDeviceView,
                        massege.toString(),
                        Toast.LENGTH_LONG
                    ).show()
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
            val newContactDetailsHolder =
                contactDetailsHolderForSync(MainActivity.db!!)
            connectedContact = newContactDetailsHolder.connectedContact
            disConnectedContact = newContactDetailsHolder.disConnectedContact
            for (e in connectedContact!!) {
                Log.d(
                    "log-AllContactOfUserDeviceView",
                    "onResponse: connected contact is :" + e!!.DisplayName + ", " + e.CID + " , " + e.MobileNumber
                )
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

    val contactComparator = object : Comparator<AllContactOfUserEntity?> {
        override fun compare(contact1: AllContactOfUserEntity?, contact2: AllContactOfUserEntity?): Int {
            try {
                return contact1?.DisplayName?.compareTo(contact2?.DisplayName ?: "", ignoreCase = true) ?: 0
            } catch (e: Exception) {
                Log.d("log-AllContactOfUserDeviceView", "Exception in comparator: $e")
                return 1
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setContentDetailsInView() {
        synchronized(this) {
            Log.d("log-AllContactOfUserDeviceView", "setContentDetailsInView: enter")
            Log.d(
                "log-AllContactOfUserDeviceView",
                "before sorting and remove duplicates disConnectedContact.size() = " + disConnectedContact!!.size
            )

            // Create a TreeSet with the custom comparator to store the sorted, unique contacts
//            val uniqueContacts: MutableSet<AllContactOfUserEntity?> =
//                TreeSet(contactComparator)

            val uniqueContacts: MutableSet<AllContactOfUserEntity?> =
                TreeSet(contactComparator)
            uniqueContacts.addAll(disConnectedContact!!)
            disConnectedContact = ArrayList(uniqueContacts)



            val uniqueContacts2: MutableSet<AllContactOfUserEntity?> =
                TreeSet(contactComparator)
            uniqueContacts2.addAll(connectedContact!!)
            connectedContact = ArrayList(uniqueContacts2)


            Log.d(
                "log-AllContactOfUserDeviceView",
                "after sorting and remove duplicates disConnectedContact.size() = " + disConnectedContact?.size
            )
            Log.d("log-AllContactOfUserDeviceView", "setContentDetailsInView: enter")
            contactArrayList = ArrayList()
            ContactSyncMainRecyclerViewAdapter = ContactSyncMainRecyclerViewAdapter(
                this@AllContactOfUserInDeviceView,
                contactArrayList!!
            )
            recyclerView1!!.setHasFixedSize(true)
            recyclerView1!!.layoutManager = LinearLayoutManager(this)
            recyclerView1!!.adapter = ContactSyncMainRecyclerViewAdapter

            // first we have to remove all object in contactArrayList
            contactArrayList!!.clear()

            //after this we set lable
            val tmp_number = 0
            val new_label_entity =
                AllContactOfUserEntity(tmp_number.toLong(), "boundary_100", "-100")
            contactArrayList!!.add(new_label_entity)
            ContactSyncMainRecyclerViewAdapter!!.notifyDataSetChanged()

            //set contact have massenger
            contactArrayList!!.addAll(connectedContact as ArrayList<AllContactOfUserEntity?>)
            ContactSyncMainRecyclerViewAdapter!!.notifyDataSetChanged()
            Log.d(
                "log-AllContactOfUserDeviceView",
                "setContentDetailsInView: after add connectedContact"
            )

            //after this we set lable
            val new_label_entity1 =
                AllContactOfUserEntity(tmp_number.toLong(), "boundary_101", "-101")
            contactArrayList!!.add(new_label_entity1)
            ContactSyncMainRecyclerViewAdapter!!.notifyDataSetChanged()

            //add all disConnected array
            contactArrayList!!.addAll(disConnectedContact as ArrayList<AllContactOfUserEntity?>)
            ContactSyncMainRecyclerViewAdapter!!.notifyDataSetChanged()
            filteredContactArrayList =
                contactArrayList!!.clone() as ArrayList<AllContactOfUserEntity?>
            Log.d(
                "log-AllContactOfUserDeviceView",
                "setContentDetailsInView: after clone of filteredContactArrayList"
            )
            recyclerView1!!.scrollToPosition(0)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun contactArrayListFilter(newText: String, flag: Int) {
        if (flag == 0) {
            Log.d("log-MainActivity", "contactArrayListFilter start with flag 0")
            contactArrayList!!.clear()
            contactArrayList!!.addAll(filteredContactArrayList!!)
            ContactSyncMainRecyclerViewAdapter!!.notifyDataSetChanged()
            return
        }
        Log.d("log-MainActivity", "contactArrayListFilter start")
        contactArrayList!!.clear()
        contactArrayList!!.addAll(filteredContactArrayList!!)
        for (e in filteredContactArrayList!!) {
            if (!e!!.DisplayName!!.lowercase(Locale.getDefault())
                    .contains(newText.lowercase(Locale.getDefault()))
            ) {
                if (e.CID != "-100" || e.CID != "-101") {
                    contactArrayList!!.remove(e)
                }
            }
        }
        ContactSyncMainRecyclerViewAdapter!!.notifyDataSetChanged()
    }

    fun ACOUDVAddNewUserIntoContact(view: View?) {
        val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
        contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
        val ContactMobileNumber = ""
        contactIntent
            .putExtra(ContactsContract.Intents.Insert.NAME, "")
            .putExtra(ContactsContract.Intents.Insert.PHONE, ContactMobileNumber)
        startActivityForResult(contactIntent, ADD_NEW_CONTACT_REQUEST_CODE)
    }

    private val ADD_NEW_CONTACT_REQUEST_CODE = 107
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_NEW_CONTACT_REQUEST_CODE) {
            start()
            Log.d("log-onActivityResult", "onActivityResult: contact add activity finished")
            if (data != null) {
                Log.d(
                    "log-onActivityResult",
                    "onActivityResult: " + resultCode + " | " + data.data.toString()
                )
            }
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show()
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Contact not added", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
        if (requestCode == CONTACTS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start()
            } else {
                Toast.makeText(this, "contact read-write permission required", Toast.LENGTH_LONG)
                    .show()
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@AllContactOfUserInDeviceView,
                    "Permission Required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        var massegeDao: MassegeDao? = null
    }
}