package com.example.mank.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mank.databaseAdapter.BlockedContactListAdapter
import com.example.mank.localDatabaseFiles.daoClasses.ContactsDao
import com.example.mank.localDatabaseFiles.daoClasses.MassegeDao
import com.example.mank.localDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.MainActivity
import com.example.mank.MainActivity.Companion.db
import com.example.mank.R
import com.example.mank.recyclerViewClassesFolder.BlockedContactRecyclerViewAdapter

class BlockAccountPage : Activity() {

	private lateinit var massegeDao: MassegeDao
	private lateinit var contactDao: ContactsDao

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_block_contact)

		blockedContactRecyclerViewAdapter = BlockedContactRecyclerViewAdapter(this)
		blockedContactRecyclerView = findViewById(R.id.ABCRecyclerView)

		findViewById<ImageButton>(R.id.BAPFinishButton).setOnClickListener{finish()}

		massegeDao = MainActivity.db?.massegeDao()!!
		contactDao = MainActivity.db?.contactDao()!!

		blockedContactListAdapter.setMyContext(this)


		blockedContactRecyclerView?.setHasFixedSize(true)
		blockedContactRecyclerView?.layoutManager = LinearLayoutManager(this)
		blockedContactRecyclerView?.adapter = blockedContactRecyclerViewAdapter //        Log.d("log-list-size", "setAllMassege: list size is :" + massegeRecyclerViewAdapter.getItemCountMyOwn());
		blockedContactRecyclerView?.scrollToPosition(blockedContactRecyclerViewAdapter!!.itemCountMyOwn)


	}

	companion object {

		@JvmStatic
		var contactList: ArrayList<ContactWithMassengerEntity?>? = null

		@SuppressLint("StaticFieldLeak")
		@JvmStatic
		var blockedContactListAdapter: BlockedContactListAdapter = BlockedContactListAdapter(db!!)

		@SuppressLint("StaticFieldLeak")
		@JvmStatic
		lateinit var blockedContactRecyclerViewAdapter: BlockedContactRecyclerViewAdapter

		@JvmStatic
		var blockedContactRecyclerView: RecyclerView? = null


	}


}