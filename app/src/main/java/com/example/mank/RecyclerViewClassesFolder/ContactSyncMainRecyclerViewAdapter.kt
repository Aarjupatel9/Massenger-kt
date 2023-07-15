package com.example.mank.RecyclerViewClassesFolder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mank.AllContactOfUserInDeviceView
import com.example.mank.ContactMassegeDetailsView
import com.example.mank.LocalDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.LocalDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.MainActivity
import com.example.mank.R

class ContactSyncMainRecyclerViewAdapter(
    context: Context,
    contactArrayList: ArrayList<AllContactOfUserEntity?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context: Context

    private var contactArrayList: ArrayList<AllContactOfUserEntity?>? = null

    init {
        this.context = context
        this.contactArrayList = contactArrayList;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0 || viewType == 3) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.contact_row_for_all_contact_sync_page, parent, false)
            return ViewHolder(view)
        } else if (viewType == 1) {
            val view1 = LayoutInflater.from(parent.context)
                .inflate(R.layout.allcontact_sync_devider_label, parent, false)
            return ViewHolder1(view1)
        } else if (viewType == 2) {
            val view1 = LayoutInflater.from(parent.context)
                .inflate(R.layout.allcontact_sync_devider_label, parent, false)
            return ViewHolder1(view1)
        }
        val view1 = LayoutInflater.from(parent.context)
            .inflate(R.layout.allcontact_sync_devider_label, parent, false)
        return ViewHolder1(view1)
    }

    override fun getItemViewType(position: Int): Int {
        val contact = contactArrayList?.get(position)
        //        Log.d("log-ContactSyncMainRecyclerViewAdapter", "contact.getCID()  : " + contact.getCID() + " DisplayName: "+contact.getDisplay_name());
        if (contact?.CID == "-5") {
            return 3
        } else if (contact?.CID == "-101") {
            return 2
        } else if (contact?.CID == "-100") { //for contact on massenger label
            return 1
        }
        return 0
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val viewHolder = holder as ViewHolder
                val contact = contactArrayList?.get(position)
                viewHolder.Display_Name.text = contact?.DisplayName
                viewHolder.LastMassegeOfContact.text = contact?.MobileNumber.toString()
                viewHolder.DPImageButton.setImageDrawable(context.resources.getDrawable(R.drawable.b_user_image))
            }

            3 -> {
                val viewHolder3 = holder as ViewHolder
                val contact1 = contactArrayList?.get(position)
                viewHolder3.Display_Name.text = contact1?.DisplayName
                viewHolder3.LastMassegeOfContact.text = contact1?.MobileNumber.toString()
                viewHolder3.DPImageButton.setImageDrawable(context.resources.getDrawable(R.drawable.null_user_image))
                viewHolder3.InviteText.visibility = View.VISIBLE
            }

            1 -> {
                val viewHolder1 = holder as ViewHolder1
                viewHolder1.main_label.text = "Contact on Massenger"
            }

            2 -> {
                val viewHolder2 = holder as ViewHolder1
                viewHolder2.main_label.text = "Invite To Massenger"
            }

            else -> {
                val viewHolder0 = holder as ViewHolder1
                viewHolder0.main_label.text = "not matched with any type of label"
            }
        }
    }

    override fun getItemCount(): Int {
//        Log.d("log-getItemCount", "getItemCount: size is : " + contactList.size());
        return contactArrayList?.size!!
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var Display_Name: TextView
        var LastMassegeOfContact: TextView
        var DPImageButton: ImageView
        var InviteText: TextView

        init {
            itemView.setOnClickListener(this)
            Display_Name = itemView.findViewById(R.id.Display_Name)
            LastMassegeOfContact = itemView.findViewById(R.id.LastMassegeOfContact)
            DPImageButton = itemView.findViewById(R.id.DPImageButton)
            InviteText = itemView.findViewById(R.id.InviteTextForOtherPeople)
        }

        override fun onClick(view: View) {
            Log.d("log-clicked", "you clicked Contact recyclerView_main")
            val position = this.adapterPosition
            val contact = contactArrayList?.get(position)
            val CID = contact?.CID
            if (CID == "-1") {
                Toast.makeText(context, "Invite your friend on Massenger", Toast.LENGTH_LONG).show()
            } else {
                val phone = contact?.MobileNumber
                if (CID != null) {
                    MainActivity.Contact_page_opened_id = CID
                }
                val ContactName = contact?.DisplayName

                // save contact to contactDetails table
                val tx = Thread {
                    val y = AllContactOfUserInDeviceView.massegeDao!!.getContactWith_CID(
                        CID,
                        MainActivity.user_login_id
                    )
                    if (y == null) {
                        //if first time then store it to contactDetails table
                        val x = AllContactOfUserInDeviceView.massegeDao?.getHighestPriorityRank(
                            MainActivity.user_login_id
                        )
                        val newEntity = ContactWithMassengerEntity(
                            phone, ContactName, CID,
                            x?.plus(1) ?: 0
                        )
                        MainActivity.contactListAdapter?.addContact(newEntity) // adapter add into database as well as reflect into UI

                    }
                }
                tx.start()

                // staring intent
                val intent =
                    Intent(context.applicationContext, ContactMassegeDetailsView::class.java)
                intent.putExtra("CID", CID)
                intent.putExtra("ContactMobileNumber", phone)
                intent.putExtra("ContactName", ContactName)
                intent.putExtra("RecyclerviewPosition", position)
                context.startActivity(intent)
                //we are saving opened_contactChatView as CID
//                Log.d("log-opened_contactChatView", "onClick: opened_contactChatView is : " + Contact_page_opened_id);
//            setOpened_contactChatViewToEmpty(position);
            }
        }
    }

    inner class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var main_label: TextView
        var small_massege: TextView? = null

        init {
            main_label = itemView.findViewById(R.id.label_boundary_1_main)
            //            small_massege = itemView.findViewById(R.id.label_boundary_1_small);
        }
    }


}