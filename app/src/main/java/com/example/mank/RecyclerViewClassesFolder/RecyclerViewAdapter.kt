package com.example.mank.RecyclerViewClassesFolder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mank.ContactMassegeDetailsView
import com.example.mank.LocalDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.MainActivity
import com.example.mank.MainActivity.Companion.setNewMassegeArriveValueToEmpty
import com.example.mank.MainActivityClassForContext
import com.example.mank.R
import com.google.android.material.internal.ContextUtils
import java.util.Objects

class RecyclerViewAdapter(context: Context?) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    private val context: Context = TODO()

    init {
        if (context != null) {
            this.context = context
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.contactviewrow, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun DpImageClickedFunction(position: Int): View.OnClickListener {
        return View.OnClickListener {
            Log.d("log-clicked-image_of-contact", "onClick: position is : $position")
            val holder =
                MainActivity.ChatsRecyclerView!!.findViewHolderForAdapterPosition(position) as ViewHolder?
            Log.d("log-clicked-image_of-contact", "onClick: holder at this position is : $holder")
            val name = holder!!.Display_Name.text.toString()
            Log.d(
                "log-clicked-image_of-contact",
                "onClick: name in holder at this position is : $name"
            )
            if (holder != null) {
                Log.d("log-clicked-image_of-contact", "onClick: enter in holder not null condition")
                holder.Display_Name.text = "jiii"
                val name1 = holder.Display_Name.text.toString()
                Log.d(
                    "log-clicked-image_of-contact",
                    "onClick: name in holder at this position is after : $name1"
                )

//                    Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(position)).setBackgroundColor(Color.RED);
                MainActivity.recyclerViewAdapter!!.notifyDataSetChanged()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = MainActivity.contactList?.get(position)
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
        holder.LastMassegeOfContact.text = contact?.lastMassege.toString()
        holder.DPImageButton.setOnClickListener(DpImageClickedFunction(position))
        if (contact?.isTouchEffectPass == true) {
            holder.constraintLayout.setBackgroundColor(Color.argb(75, 100, 159, 107))
        }
        if (contact?.userImage == null) {
            holder.DPImageButton.setImageDrawable(
                MainActivityClassForContext.appContext?.getResources()?.getDrawable(R.drawable.ic_baseline_person_24)
            )
        } else {
            val bitmap = BitmapFactory.decodeByteArray(contact.userImage, 0, contact.userImage?.size!!)
            holder.DPImageButton.setImageBitmap(bitmap)
        }
        if (contact?.newMassegeArriveValue == 0) {
            holder.new_massege_arrive_value.text = ""
            holder.new_massege_arrive_value.setPadding(0, 0, 0, 0)
            holder.new_massege_arrive_value.minWidth = 0
        } else {
            holder.new_massege_arrive_value.text = contact?.newMassegeArriveValue.toString()
            holder.new_massege_arrive_value.minWidth = 65
            holder.new_massege_arrive_value.setPadding(3, 3, 3, 3)
        }
    }

    override fun getItemCount(): Int {
//        Log.d("log-getItemCount", "getItemCount: size is : " + contactList.size());
        return MainActivity.contactList?.size!!
    }

    val itemCountMyOwn: Int
        get() = if (MainActivity.contactList?.size!! == 0) {
            -1
        } else 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, OnLongClickListener {
        var Display_Name: TextView
        var new_massege_arrive_value: TextView
        var LastMassegeOfContact: TextView
        var DPImageButton: ImageView
        var constraintLayout: ConstraintLayout

        //        public CardView cardView;
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            Display_Name = itemView.findViewById(R.id.Display_Name)
            new_massege_arrive_value = itemView.findViewById(R.id.new_massege_arrive_value)
            LastMassegeOfContact = itemView.findViewById(R.id.LastMassegeOfContact)
            DPImageButton = itemView.findViewById(R.id.DPImageButton)
            constraintLayout = itemView.findViewById(R.id.contactViewRowConstraintLayout)
            //            cardView = itemView.findViewById(R.id.contactViewRowCardView);
//            DPImageButton.setOnClickListener(this);
        }

        override fun onClick(view: View) {
            Log.d("log-clicked", "you clicked Contact recyclerView_main")
            val position = this.adapterPosition
            val contact = MainActivity.contactList?.get(position)
            val name = contact?.DisplayName
            val phone = contact?.MobileNumber
            val CID = contact?.CID
            MainActivity.Contact_page_opened_id = CID
            val ContactName = contact?.DisplayName

//            Toast.makeText(context, "The position is " + String.valueOf(position) +
//                    " Name: " + name + ", Phone:" + phone + ", c_ID:"+CID, Toast.LENGTH_SHORT).show();
            val intent = Intent(context.applicationContext, ContactMassegeDetailsView::class.java)
            intent.putExtra("CID", CID)
            intent.putExtra("ContactMobileNumber", phone)
            intent.putExtra("ContactName", ContactName)
            intent.putExtra("RecyclerviewPosition", position)
            //we are saving opened_contactChatView as CID
            Log.d(
                "log-opened_contactChatView",
                "onClick: opened_contactChatView is : " + MainActivity.Contact_page_opened_id
            )
            context.startActivity(intent)
            setNewMassegeArriveValueToEmpty(position)
        }

        @SuppressLint("RestrictedApi", "NotifyDataSetChanged")
        override fun onLongClick(view: View): Boolean {
            Log.d("log-enter", "getMainSideMenu: enter here")
            val popup = PopupMenu(context, view)
            popup.gravity = Gravity.CENTER_HORIZONTAL
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.long_press_on_contact_popup_menu, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener { item ->
                val position = this@ViewHolder.adapterPosition
                val contact = MainActivity.contactList?.get(position)
                val phone = contact?.MobileNumber
                val CID = contact?.CID
                if (item.itemId == R.id.LPOCPMDelete) {
                    Toast.makeText(context, "LPOCPMDelete", Toast.LENGTH_SHORT).show()
                    MainActivity.contactArrayList!!.removeIf { e -> e?.CID === CID }
                    MainActivity.filteredContactArrayList!!.removeIf { e -> e.CID === CID }
                    val t = Thread {
                        val massegeDao = MainActivity.db!!.massegeDao()
                        val r1 =
                            massegeDao.removeChatsFromMassegeTable(CID, MainActivity.user_login_id)
                        val r2 = massegeDao.removeSelfContactFromContactTable(
                            CID,
                            MainActivity.user_login_id
                        )
                    }
                    t.start()
                    Objects.requireNonNull(ContextUtils.getActivity(context))?.runOnUiThread { MainActivity.recyclerViewAdapter!!.notifyDataSetChanged() }
                } else if (item.itemId == R.id.LPOCPMClearChat) {
                    Toast.makeText(context, "LPOCPMClearChat", Toast.LENGTH_SHORT).show()
                    val t = Thread {
                        val massegeDao = MainActivity.db!!.massegeDao()
                        val r1 =
                            massegeDao.removeChatsFromMassegeTable(CID, MainActivity.user_login_id)
                    }
                    t.start()
                }
                false
            }
            return false
        }
    }


}