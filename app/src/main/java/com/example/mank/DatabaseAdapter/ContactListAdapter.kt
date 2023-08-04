package com.example.mank.DatabaseAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.Log
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.MainActivity
import com.example.mank.MainActivity.Companion.user_login_id
import com.google.android.material.internal.ContextUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ContactListAdapter(db: MainDatabaseClass) {
	var massegeDao: MassegeDao
	var context: Context? = null
	var data: List<ContactWithMassengerEntity?>?

	init {
		massegeDao = db.massegeDao()!!
		data = massegeDao.getContactDetailsFromDatabase(MainActivity.user_login_id)
		MainActivity.contactList = ArrayList()
		MainActivity.contactList?.addAll((data)!!)
		setUpLastMasseges()
		setUpProfileImages()
	}

	@SuppressLint("RestrictedApi")
	private fun setUpLastMasseges() {
		val ts = Thread {
			for (i in MainActivity.contactList!!) {
				if (i!!.CID != user_login_id) {
					val massege = massegeDao.getLastInsertedMassege(i.CID, user_login_id)
					i.lastMassege = (massege) ?: ""
					Log.d("log-ContactListAdapter", "massege is : $massege for CID : " + i.CID + " and appUserId : " + MainActivity.user_login_id)
				} else {
					val massege = massegeDao.getSelfLastInsertedMassege(i.CID, user_login_id)
					if (massege != null) {
						i.lastMassege = massege
					}
					Log.d("log-ContactListAdapter-self", "massege is : $massege for CID : " + i.CID + " and appUserId : " + MainActivity.user_login_id)
				}
			}
			try {
				ContextUtils.getActivity(context)?.runOnUiThread(Runnable {
					if (MainActivity.recyclerViewAdapter != null) {
						MainActivity.recyclerViewAdapter!!.notifyDataSetChanged()
					}
				})
			} catch (e: Exception) {
			}
		}
		ts.start()
	}

	private fun setUpProfileImages() {
		val tu = Thread {
			for (i in MainActivity.contactList!!) {
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

	@SuppressLint("NotifyDataSetChanged", "RestrictedApi")
	fun addContact(newEntity: ContactWithMassengerEntity) {
		Log.d("log-ContactListAdapter", "AddContact method start for " + newEntity.DisplayName + " , " + newEntity.MobileNumber)
		val tx = Thread { massegeDao.SaveContactDetailsInDatabase(newEntity) }
		tx.start()
		MainActivity.contactList?.add(0, newEntity)
		recyclerViewAdapterNotifyLocal()

		val jsonArray = JSONArray()
		try {
			val tmp = JSONObject()
			tmp.put("_id", newEntity.CID)
			tmp.put("Number", newEntity.MobileNumber)
			tmp.put("ProfileImageVersion", newEntity.profileImageVersion)
			jsonArray.put(tmp)
			Log.d("log-MainActivity", "profileImage update part : $jsonArray")
			MainActivity.socket!!.emit("updateProfileImages", MainActivity.user_login_id, jsonArray, 1)
		} catch (ex: Exception) {
			Log.d("log-ContactListAdapter-Exception", ex.toString())
		}
		Log.d("log-ContactListAdapter", "AddContact method end")
	}

	fun updateSelfUserImage(userImage: ByteArray) {
		val ti = Thread {
			Log.d("log-ContactListAdapter", "updateSelfUserImage result : " + MainActivity.user_login_id)
			Log.d("log-ContactListAdapter", "updateSelfUserImage result : " + userImage.size)
			for (i in MainActivity.contactList!!) {
				if (i?.CID == MainActivity.user_login_id) {
					i?.userImage = userImage
					recyclerViewAdapterNotifyLocal()
					break
				}
			}
		}
		ti.start()
	}

	@SuppressLint("RestrictedApi", "NotifyDataSetChanged")
	private fun recyclerViewAdapterNotifyLocal() {
		try {
			ContextUtils.getActivity(context)?.runOnUiThread(Runnable {
				if (MainActivity.recyclerViewAdapter != null) {
					MainActivity.recyclerViewAdapter!!.notifyDataSetChanged()
				}
			})
		} catch (e: Exception) {
			Log.d("log-ContactListAdapter", "AddContact Exception : $e")
		}
	}

	fun updateMassegeArrivalValue(index: Int, contactView: ContactWithMassengerEntity) {
		val prev_value = contactView.newMassegeArriveValue
		contactView.newMassegeArriveValue = prev_value + 1
		MainActivity.contactArrayList!![index] = contactView //        MainActivity.recyclerViewAdapter.notifyDataSetChanged();
		recyclerViewAdapterNotifyLocal()
		MainActivity.ChatsRecyclerView!!.scrollToPosition(MainActivity.recyclerViewAdapter!!.itemCountMyOwn)
	}

	fun practiceMethod(CID: String, image: ByteArray) {
		Log.d("log-ContactListAdapter", "practiceMethod start : " + image.size)
		for (i in MainActivity.contactList!!) {
			if ((i?.CID == CID)) {
				i.userImage = image
				recyclerViewAdapterNotifyLocal()
				break
			}
		}
	}

	@SuppressLint("NotifyDataSetChanged", "RestrictedApi")
	fun updatePositionOfContact(C_ID: String?, context: Context?) {
		if (MainActivity.contactArrayList != null) {
			for (i in MainActivity.contactArrayList!!.indices) {
				if ((MainActivity.contactArrayList!![i]?.CID == C_ID)) {
					val x = MainActivity.contactArrayList!!.removeAt(i)
					MainActivity.contactArrayList!!.add(0, x)
					ContextUtils.getActivity(context)?.runOnUiThread { MainActivity.recyclerViewAdapter!!.notifyDataSetChanged() }
				}
			}
		} else {
			Log.d("log-ContactListAdapter", "contactArrayList is null")
		}
	}

	fun setLastMassege(CID: String?) {
		val tu = Thread {
			for (i in MainActivity.contactArrayList!!.indices) {
				val contactView = MainActivity.contactArrayList!![i]
				if (contactView?.CID == CID) {
					contactView?.lastMassege = massegeDao.getLastInsertedMassege(CID, user_login_id).toString()
					MainActivity.contactArrayList!![i] = contactView
					recyclerViewAdapterNotifyLocal()
				}
			}
		}
		tu.start()
	}fun setLastMassege(CID: String?, massege: String) {
		val tu = Thread {
			for (i in MainActivity.contactArrayList!!.indices) {
				val contactView = MainActivity.contactArrayList!![i]
				if (contactView?.CID == CID) {
					contactView?.lastMassege = massege.toString()
					MainActivity.contactArrayList!![i] = contactView
					recyclerViewAdapterNotifyLocal()
				}
			}
		}
		tu.start()
	}
	val handler = Handler()
	val typingCounter = mutableMapOf<String, Int>()
	fun setTypingStatus(CID: String) {

		Log.d("log-ContactMassegeDetailsView", "updateOnlineStatusToTypingInUI start")
		// Increment the counter
		typingCounter[CID] = (typingCounter[CID] ?: 0) + 1
		// Calculate the delay based on the counter
		val delayMillis: Long = 1000 // Multiply by 2000 milliseconds (2 seconds)
		handler.postDelayed({
			Log.d("log-ContactMassegeDetailsView", "updateOnlineStatusToTypingInUI updateOnlineStatusInUI call after $delayMillis seconds")
			// Decrement the counter after the last run
			typingCounter[CID] = (typingCounter[CID] ?: 0) - 1
			// Check if it's the last run
			if (typingCounter[CID]  == 0) {
				val tu = Thread {
					for (i in MainActivity.contactArrayList!!.indices) {
						val contactView = MainActivity.contactArrayList!![i]
						if (contactView?.CID == CID) {
							val massege = massegeDao.getLastInsertedMassege(CID, MainActivity.user_login_id)
							contactView.lastMassege = massege.toString()
							MainActivity.contactArrayList!![i] = contactView
							recyclerViewAdapterNotifyLocal()
						}
					}
				}
				tu.start()
			}
		}, delayMillis)



		val tu = Thread {
			for (i in MainActivity.contactArrayList!!.indices) {
				val contactView = MainActivity.contactArrayList!![i]
				if (contactView?.CID == CID) {
					contactView.lastMassege = "typing..."
					MainActivity.contactArrayList!![i] = contactView
					recyclerViewAdapterNotifyLocal()
				}
			}
		}
		tu.start()

	}

	fun updateContactSavedName(number : Long, name : String){

		val rowsUpdated = massegeDao.updateSavedNameOfUserEntity(number, name, user_login_id)
		val rowsUpdated1 = massegeDao.updateSavedNameOfContactEntity(number, name, user_login_id)

		recyclerViewAdapterNotifyLocal()

	}

}