package com.example.mank.recyclerViewClassesFolder

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mank.R


class StatusRecyclerViewAdapter(var context: Context?, var ContactStatusList: ArrayList<String?>) :
    RecyclerView.Adapter<StatusRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_status_view_row, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val id = ContactStatusList[position]
        holder.SPContactName.text = "status is coming soon..."
        holder.SPStatusPostingTime.text = ""
    }

    override fun getItemCount(): Int {
        return ContactStatusList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var SPContactName: TextView
        var SPStatusPostingTime: TextView
        var SPStatusContactConstraintLayout: ConstraintLayout

        init {
            itemView.setOnClickListener(this)
            SPContactName = itemView.findViewById(R.id.SPContactName)
            SPStatusPostingTime = itemView.findViewById(R.id.SPStatusPostingTime)
            SPStatusContactConstraintLayout =
                itemView.findViewById(R.id.SPStatusContactConstraintLayout)
        }

        override fun onClick(view: View) {}
    }
}