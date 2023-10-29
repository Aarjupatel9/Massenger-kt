package com.example.mank.databaseAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.mank.ContactMassegeDetailsView
import com.example.mank.ContactMassegeDetailsView.Companion.massegeArrayList
import com.example.mank.localDatabaseFiles.daoClasses.MassegeDao
import com.example.mank.localDatabaseFiles.MainDatabaseClass
import com.example.mank.localDatabaseFiles.entities.MassegeEntity
import com.example.mank.MainActivity
import com.example.mank.MainActivity.Companion.user_login_id
import com.google.android.material.internal.ContextUtils
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects

class MassegeListAdapter(db: MainDatabaseClass?) {
	var massegeDao: MassegeDao
	var context: Context? = null
	private var massegeEntityList: List<MassegeEntity?>? = null

	init {
		massegeDao = db!!.massegeDao()!!
	}

	private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
	fun formatDate(date: Date): String {
		val currentYear = Calendar.getInstance().get(Calendar.YEAR)
		val inputYear = date.year + 1900 // Year is stored as year - 1900 in Date object

		val dateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())

		return if (currentYear == inputYear) {
			dateFormat.format(date)
		} else {
			val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
			"${dateFormat.format(date)} ${yearFormat.format(date)}"
		}
	}
	fun fillMassegeListOfUser(CID: String?) {
		synchronized(this) {
			massegeEntityList = if (Objects.equals(CID, MainActivity.user_login_id)) {
				massegeDao.getSelfChat(CID, MainActivity.user_login_id)
			} else {
				massegeDao.getChat(CID, MainActivity.user_login_id)
			}
			massegeEntityList?.filterNotNull()?.let { massegeArrayList!!.addAll(it) }

			val messagesToAdd = mutableListOf<Pair<Int, MassegeEntity>>() // Store index and new message pairs
			for (i in 0 until (massegeArrayList?.size?.minus(1) ?: 0)) {

				val currentMessage = massegeArrayList?.get(i)
				val nextMessage = massegeArrayList?.get(i + 1)

				val currentDay = currentMessage?.timeOfSend?.div((1000 * 60 * 60 * 24))
				val nextDay = nextMessage?.timeOfSend?.div((1000 * 60 * 60 * 24))


				if (nextDay != null && currentDay != null) {
					if (nextDay > currentDay + 1 || i==0) {
						Log.d("MassegeListAdapter", "fillingMassege ||  day change for : ${currentMessage.massege}");
						val nextDayDate = Date(nextDay * (1000 * 60 * 60 * 24)) //						val newMessageText = "New Message for $nextDay"
						val newMessageText = formatDate(nextDayDate)
						val newMessageTime = nextDayDate.time
						val dateLabel: MassegeEntity = MassegeEntity(user_login_id, "-100", newMessageText, newMessageTime, 3);
						if(i==0){
							messagesToAdd.add(0 to dateLabel)
						}else{
						messagesToAdd.add(i + 1 to dateLabel)
						}
					}
				}
			}

			for ((index, message) in messagesToAdd.reversed()) {
				massegeArrayList?.add(index, message)
			}

			for (m : MassegeEntity in massegeArrayList!!){
			Log.d("MassegeListAdapter", "fillingMassege || massegeArrayList ${m.massege} ${m.receiverId} ${m.senderId} ");
			}


//			ContactMassegeDetailsView.ContactMassegeRecyclerView?.scrollToPosition(ContactMassegeDetailsView.massegeRecyclerViewAdapter!!.itemCountMyOwn)

			//updateMassegeToServerWithViewStatus(massegeArrayList);
			//for updating the massegeStatus to 3 for senders massege
			for (e: MassegeEntity in ContactMassegeDetailsView.massegeArrayList!!) {
				if (!Objects.equals(e.senderId, MainActivity.user_login_id) && e.massegeStatus != 3) {

					e.massegeStatus = 3
					recyclerViewAdapterNotifyLocal()
					val tb: Thread = Thread(object : Runnable {
						override fun run() {
							massegeDao.updateMassegeStatus(e.senderId, e.receiverId, e.timeOfSend, 3, MainActivity.user_login_id)
						}
					})
					tb.start()
					if (MainActivity.socket != null) {
						var returnArray: JSONArray
						try {
							val massegeOBJ: JSONObject = JSONObject()
							massegeOBJ.put("from", e.senderId)
							massegeOBJ.put("to", e.receiverId)
							massegeOBJ.put("massege", e.massege)
							massegeOBJ.put("chatId", e.chatId)
							massegeOBJ.put("time", e.timeOfSend)
							massegeOBJ.put("massegeStatus", 3)
							massegeOBJ.put("massegeStatusL", 1)
							massegeOBJ.put("ef1", 0)
							massegeOBJ.put("ef2", 0)
							returnArray = JSONArray()
							returnArray.put(massegeOBJ)
							MainActivity.socket?.emit("massege_reach_read_receipt", 4, MainActivity.user_login_id, returnArray)
							Log.d("log-MassegeListAdapter-fillMassegeListOfUser", "updating the massegeStatus to 3 for massege : " + e.massege)
						} catch (exception: Exception) {
							Log.d("log-updateMassegeToServerWithViewStatus-exception", exception.toString())
						}
					}
				}
			}
		}
	}

	fun setMyContext(context: Context?) {
		this.context = context
	}

	@SuppressLint("NotifyDataSetChanged", "RestrictedApi")
	fun addMassege(newEntity: MassegeEntity) {
		Log.d("log-MassegeListAdapter", "addMassege method start for " + newEntity.massege + " , " + newEntity.senderId)
		val tx = Thread {
			try {
				massegeDao.insertMassegeIntoChat(newEntity)
			} catch (e: Exception) {
				Log.d("log-sql-exception", e.toString())
			}
		}
		tx.start()
		ContactMassegeDetailsView.massegeArrayList!!.add(newEntity)
		try {
			ContextUtils.getActivity(context)?.runOnUiThread(Runnable {
				Log.d("log-MassegeListAdapter", "recyclerViewAdapterNotifyLocal run start")
				ContactMassegeDetailsView.massegeRecyclerViewAdapter!!.notifyDataSetChanged()
				ContactMassegeDetailsView.ContactMassegeRecyclerView!!.scrollToPosition(ContactMassegeDetailsView.massegeRecyclerViewAdapter!!.itemCountMyOwn)
			})
		} catch (e: Exception) {
			Log.d("log-ContactListAdapter", "AddContact Exception : $e")
		}

		//        recyclerViewAdapterNotifyLocal();
		//        ContactMassegeRecyclerView.scrollToPosition(massegeRecyclerViewAdapter.getItemCountMyOwn());
		Log.d("log-MassegeListAdapter", "addMassege method end")
	}

	@SuppressLint("NotifyDataSetChanged", "RestrictedApi")
	fun addMassege(newEntity: MassegeEntity, flag: Int) {
		Log.d("log-MassegeListAdapter", "addMassege method start for " + newEntity.massege + " , " + newEntity.senderId)
		if (flag == 1) {
			val tx = Thread(object : Runnable {
				override fun run() {
					if (massegeDao.getMassegeByTimeOfSend(newEntity.senderId, newEntity.timeOfSend, MainActivity.user_login_id) == null) {
						Log.d("log-onMassegeArriveFromServer3", "Contact page is opened inside runnable inside if")
						ContactMassegeDetailsView.massegeArrayList!!.add(newEntity)
						try {
							ContextUtils.getActivity(context)?.runOnUiThread(object : Runnable {
								@SuppressLint("NotifyDataSetChanged")
								override fun run() {
									Log.d("log-MassegeListAdapter", "recyclerViewAdapterNotifyLocal run start")
									ContactMassegeDetailsView.massegeRecyclerViewAdapter!!.notifyDataSetChanged()
									ContactMassegeDetailsView.ContactMassegeRecyclerView!!.scrollToPosition(ContactMassegeDetailsView.massegeRecyclerViewAdapter!!.itemCountMyOwn)
								}
							})
						} catch (e: Exception) {
							Log.d("log-ContactListAdapter", "AddContact Exception : $e")
						}
						try {
							massegeDao.insertMassegeIntoChat(newEntity)
						} catch (e: Exception) {
							Log.d("log-sql-exception", e.toString())
						}
					}
				}
			})
			tx.start()
		}
		Log.d("log-MassegeListAdapter", "addMassege method end")
	}

	fun updateMassegeStatus(receiverId: String, time: Long, viewStatus: Int) {
		val x = ContactMassegeDetailsView.massegeArrayList
		for (j in x!!.indices.reversed()) {
			if (x[j].timeOfSend == time && (receiverId == x[j].receiverId)) {
				x[j].massegeStatus = viewStatus
			}
		}
		recyclerViewAdapterNotifyLocal()
	}

	//    //run at start of the CMDV
	//    public void updateMassegeToServerWithViewStatus(ArrayList<MassegeEntity> massegeArrayList) {
	//        for (MassegeEntity me : massegeArrayList) {
	//            if (!me.getSenderId().equals(user_login_id)) {
	//                if (me.getMassegeStatus() != 2) {
	//                    Thread ums = new Thread(new Runnable() {
	//                        @Override
	//                        public void run() {
	//                            massegeDao.updateMassegeStatus(me.getSenderId(), me.getReceiverId(), me.getTimeOfSend(), 3);
	//                        }
	//                    });
	//                    ums.start();
	//                    if (socket != null) {
	//                        JSONArray returnArray;
	//                        try {
	//                            JSONObject massegeOBJ = new JSONObject();
	//                            massegeOBJ.put("from", me.getSenderId());
	//                            massegeOBJ.put("to", me.getReceiverId());
	//                            massegeOBJ.put("massege", me.getMassege());
	//                            massegeOBJ.put("chatId", me.getChatId());
	//                            massegeOBJ.put("time", me.getTimeOfSend());
	//                            massegeOBJ.put("massegeStatus", 3);
	//                            massegeOBJ.put("massegeStatusL", 1);
	//                            massegeOBJ.put("ef1", 0);
	//                            massegeOBJ.put("ef2", 0);
	//                            returnArray = new JSONArray();
	//                            returnArray.put(massegeOBJ);
	//                            socket.emit("massege_reach_read_receipt", 4, user_login_id, me);
	//                        } catch (Exception e) {
	//                            Log.d("log-updateMassegeToServerWithViewStatus-exception", e.toString());
	//                        }
	//                    }
	//                }
	//            }
	//        }
	//    }
	@SuppressLint("RestrictedApi", "NotifyDataSetChanged")
	private fun recyclerViewAdapterNotifyLocal() {
		try {
			ContextUtils.getActivity(context)?.runOnUiThread {
				if (ContactMassegeDetailsView.massegeRecyclerViewAdapter != null) {
					Log.d("log-MassegeListAdapter", "recyclerViewAdapterNotifyLocal run start")
					ContactMassegeDetailsView.massegeRecyclerViewAdapter!!.notifyDataSetChanged()
				}
			}
		} catch (e: Exception) {
			Log.d("log-ContactListAdapter", "AddContact Exception : $e")
		}
	}
}