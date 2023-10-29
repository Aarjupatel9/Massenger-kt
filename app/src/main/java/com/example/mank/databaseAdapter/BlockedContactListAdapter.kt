package com.example.mank.databaseAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.mank.localDatabaseFiles.daoClasses.ContactsDao
import com.example.mank.localDatabaseFiles.daoClasses.MassegeDao
import com.example.mank.localDatabaseFiles.MainDatabaseClass
import com.example.mank.localDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.MainActivity
import com.example.mank.MainActivity.Companion.socket
import com.example.mank.profile.BlockAccountPage
import com.example.mank.MainActivity.Companion.user_login_id
import com.google.android.material.internal.ContextUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class BlockedContactListAdapter(db: MainDatabaseClass) {
	var massegeDao: MassegeDao
	var contactDao: ContactsDao
	var context: Context? = null
	var data: List<ContactWithMassengerEntity?>?

	init {
		massegeDao = db.massegeDao()
		contactDao = db.contactDao()
		data = contactDao.getBlockedContactDetailsFromDatabase(user_login_id)
		BlockAccountPage.contactList = ArrayList()
		data?.let { BlockAccountPage.contactList?.addAll(it) }
		setUpProfileImages()
		Log.d("log-BlockedContactListAdapter", "data size ${BlockAccountPage.contactList?.size}")
	}


	private fun setUpProfileImages() {
		val tu = Thread {
			for (i in BlockAccountPage.contactList!!) {
				val CID = i?.CID
				val imagePath = "/storage/emulated/0/Android/media/com.massenger.mank.main/Pictures/profiles/" + CID + MainActivity.user_login_id + ".png"
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
					Log.d("log-ContactListAdapter", "setUserImage : after fetch image form file system : " + byteArray.size)
					i!!.userImage = byteArray
					recyclerViewAdapterNotifyLocal()
				}
			}
		}
		tu.start()
	}

	fun setMyContext(context: Context?) {
		this.context = context
	}

	@SuppressLint("RestrictedApi", "NotifyDataSetChanged")
	private fun recyclerViewAdapterNotifyLocal() {
		try {
			Log.d("log-BlockedContactListAdapter", "recyclerViewAdapterNotifyLocal try enter");

			ContextUtils.getActivity(context)?.runOnUiThread(Runnable {
				BlockAccountPage.blockedContactRecyclerViewAdapter.notifyDataSetChanged()
			})
		} catch (e: Exception) {
			Log.d("log-ContactListAdapter", "AddContact Exception : $e")
		}
	}


	fun removeContact(CID: String) {

		socket?.emit("contactBlockStatusChanged", user_login_id, CID, 0 )
		val contact = BlockAccountPage.contactList?.find { it?.CID == CID }
		BlockAccountPage.contactList?.remove(contact);
		Log.d("log-BlockedContactListAdapter", "removeContact contact is ${contact?.DisplayName} $CID");
		recyclerViewAdapterNotifyLocal();
	}
}