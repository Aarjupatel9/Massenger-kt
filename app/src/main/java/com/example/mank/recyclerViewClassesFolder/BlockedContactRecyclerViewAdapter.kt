package com.example.mank.recyclerViewClassesFolder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mank.MainActivity
import com.example.mank.profile.BlockAccountPage
import com.example.mank.MainActivityClassForContext
import com.example.mank.R

class BlockedContactRecyclerViewAdapter(context: Context?) : RecyclerView.Adapter<BlockedContactRecyclerViewAdapter.ViewHolder>() {
	val context: Context? = context;


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.contactviewrow, parent, false)
		return ViewHolder(view)
	}

	val itemCountMyOwn: Int
		get() = if (BlockAccountPage.contactList?.size!! == 0) {
			-1
		} else 0

	@SuppressLint("UseCompatLoadingForDrawables")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val contact = BlockAccountPage.contactList?.get(position)
		if (contact?.CID == MainActivity.user_login_id) {
			val name = contact?.DisplayName + " (self)"
			holder.Display_Name.text = name
		} else {
			if (contact?.DisplayName == null) {
				holder.Display_Name.text = contact?.MobileNumber.toString()
			} else {
				holder.Display_Name.text = contact?.DisplayName
			}
		}
		if (contact?.isTouchEffectPass == true) {
			holder.constraintLayout.setBackgroundColor(Color.argb(75, 100, 159, 107))
		}
		if (contact?.userImage == null) {
			holder.dPImageButton.setImageDrawable(MainActivityClassForContext.appContext?.resources?.getDrawable(R.drawable.ic_baseline_person_24))
		} else {
			val bitmap = BitmapFactory.decodeByteArray(contact.userImage, 0, contact.userImage?.size!!)
			holder.dPImageButton.setImageBitmap(bitmap)
		}
		holder.new_massege_arrive_value.text = ""
		holder.new_massege_arrive_value.setPadding(0, 0, 0, 0)
		holder.new_massege_arrive_value.minWidth = 0

	}

	override fun getItemCount(): Int {
		Log.d("log-BlockedContactRecyclerViewAdapter", "getItemCount ${BlockAccountPage.contactList?.size}")
		return BlockAccountPage.contactList?.size!!
	}

	inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, OnLongClickListener {
		var Display_Name: TextView
		var dPImageButton: ImageView
		var new_massege_arrive_value: TextView
		var constraintLayout: ConstraintLayout

		init {
			itemView.setOnClickListener(this)
			itemView.setOnLongClickListener(this)
			Display_Name = itemView.findViewById(R.id.Display_Name)
			new_massege_arrive_value = itemView.findViewById(R.id.new_massege_arrive_value)
			dPImageButton = itemView.findViewById(R.id.DPImageButton)
			constraintLayout = itemView.findViewById(R.id.contactViewRowConstraintLayout)
		}

		override fun onClick(view: View) {
			Log.d("log-clicked", "you clicked Contact recyclerView_main")
			val popup = PopupMenu(context, view)
			popup.gravity = Gravity.CENTER_HORIZONTAL
			val inflater = popup.menuInflater
			inflater.inflate(R.menu.long_press_on_blocked_contact_popup_menu, popup.menu)
			popup.show()
			popup.setOnMenuItemClickListener { item ->
				val position = this@ViewHolder.adapterPosition
				val contact = BlockAccountPage.contactList?.get(position)
				val cid = contact?.CID
				if (item.itemId == R.id.ABCUnBlockMenuItem) {
					Log.d("log-BlockedContactRecyclerViewAdapter", "enter in unblock clicked");
					contact?.let { MainActivity.contactListAdapter?.addContact(it, 1) } //1 for through blocked page
					cid?.let { BlockAccountPage.blockedContactListAdapter.removeContact(it) } //1 for through blocked page
				}
				false
			}
		}

		@SuppressLint("RestrictedApi", "NotifyDataSetChanged")
		override fun onLongClick(view: View): Boolean {
			Log.d("log-enter", "getMainSideMenu: enter here")
			return false
		}
	}


}