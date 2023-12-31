package com.example.mank.recyclerViewClassesFolder

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mank.localDatabaseFiles.entities.MassegeEntity
import com.example.mank.MainActivity
import com.example.mank.MainActivity.Companion.user_login_id
import com.example.mank.R

import java.text.SimpleDateFormat
import java.util.Date

class ContactMassegeRecyclerViewAdapter(private val context: Context, private val massegeList: List<MassegeEntity>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	override fun getItemViewType(position: Int): Int {
		val massege = massegeList[position]
		return if (massege.senderId == user_login_id && massege.receiverId == "-100") {
			Log.d("ContactMassegeRecyclerViewAdapter", "getItemViewType || ${massege.massege} ${massege.receiverId} ${massege.senderId}");
			return 2 //for date label
		} else if (massege.senderId == MainActivity.user_login_id) {
			1 // fro sender is user
		} else {
			0 // for sender is contact
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return if (viewType == 1) {
			val view = LayoutInflater.from(parent.context).inflate(R.layout.massege_design_row_for_user, parent, false)
			ViewHolderUser(view)
		} else if (viewType == 0) {
			val view = LayoutInflater.from(parent.context).inflate(R.layout.massege_design_row, parent, false)
			ViewHolderContact(view)
		} else {
//			Log.d("ContactMassegeRecyclerViewAdapter", "onCreateViewHolder || vieType : $viewType");
			val view1 = LayoutInflater.from(parent.context).inflate(R.layout.massege_date_label, parent, false)
			return ViewHolderDateLabel(view1)
		}
	}

	@SuppressLint("UseCompatLoadingForDrawables")
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (holder.itemViewType == 2) {
//			Log.d("ContactMassegeRecyclerViewAdapter", "onBindViewHolder || position : $position");
			val viewHolder = holder as ViewHolderDateLabel
			val massege = massegeList[position]
			viewHolder.mainLabel.text = massege.massege
		} else if (holder.itemViewType == 0) {
			val viewHolder = holder as ViewHolderContact
			val massege = massegeList[position]
			viewHolder.massege_display.text = massege.massege
			viewHolder.massege_id.text = massege.chatId.toString()
			val date = Date(massege.timeOfSend)
			val formatted_date = SimpleDateFormat("HH:mm").format(date)
			viewHolder.contact_main_massege_time.text = formatted_date
		} else {
			val viewHolder1 = holder as ViewHolderUser
			val massege1 = massegeList[position]
			viewHolder1.user_main_massege.text = massege1.massege
			viewHolder1.user_main_massege_id.text = massege1.chatId.toString()
			val date = Date(massege1.timeOfSend)
			val formatted_date = SimpleDateFormat("HH:mm").format(date)
			viewHolder1.user_main_massege_time.text = formatted_date
			if (massege1.massegeStatus == 1 || massege1.massegeStatus == 0) {
				viewHolder1.user_massege_status_main.setImageDrawable(context.resources.getDrawable(R.drawable.ic_massege_sent_icon))
			} else if (massege1.massegeStatus == 2) {
				viewHolder1.user_massege_status_main.setImageDrawable(context.resources.getDrawable(R.drawable.ic_massege_reach_icon))
			} else if (massege1.massegeStatus == 3) {
				viewHolder1.user_massege_status_main.setImageDrawable(context.resources.getDrawable(R.drawable.ic_massege_read_icon))
			} else if (massege1.massegeStatus == -1) {
				viewHolder1.user_massege_status_main.setImageDrawable(context.resources.getDrawable(R.drawable.ic_offline_massege_state_icon))
			} else {
				viewHolder1.user_massege_status_main.setImageDrawable(context.resources.getDrawable(R.drawable.ic_check_all))
			}
		}
	}

	override fun getItemCount(): Int { //        Log.d("log-getItemCount", "getItemCount: size is : " + massegeList.size());
		return massegeList.size
	}

	val itemCountMyOwn: Int
		get() = massegeList.size - 1

	inner class ViewHolderContact(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
		var massege_display: TextView
		var massege_id: TextView
		var contact_main_massege_time: TextView

		init {
			itemView.setOnClickListener(this)
			massege_display = itemView.findViewById(R.id.contact_main_massege)
			massege_id = itemView.findViewById(R.id.contact_main_massege_id)
			contact_main_massege_time = itemView.findViewById(R.id.contact_main_massege_time)
		}

		override fun onClick(view: View) { //            int position = this.getAdapterPosition();
			//            MassegeEntity massege = massegeList.get(position);
			Log.d("log-clicked", "you clicked on massege ") //+ massege.getMassege());
		}
	}

	inner class ViewHolderUser(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
		var user_main_massege: TextView
		var user_main_massege_id: TextView
		var user_main_massege_time: TextView
		var user_massege_status_main: ImageView
		var contact_main_massege_time: ImageView? = null

		init {
			itemView.setOnClickListener(this)
			user_main_massege = itemView.findViewById(R.id.user_main_massege)
			user_main_massege_id = itemView.findViewById(R.id.user_main_massege_id)
			user_massege_status_main = itemView.findViewById(R.id.user_massege_status_main)
			user_main_massege_time = itemView.findViewById(R.id.user_main_massege_time)
		}

		override fun onClick(view: View) { //            int position = this.getAdapterPosition();
			//            MassegeEntity massege = massegeList.get(position);
			//            Log.d("log-clicked", "you clicked on massege " + massege.getMassege());
		}
	}

	inner class ViewHolderDateLabel(itemView: View) : RecyclerView.ViewHolder(itemView) {
		var mainLabel: TextView

		init {
			mainLabel = itemView.findViewById(R.id.label_boundary_1_main)
		}
	}

}