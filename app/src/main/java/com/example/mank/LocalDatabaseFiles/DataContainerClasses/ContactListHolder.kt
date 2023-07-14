package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.MainActivity

class ContactListHolder(db: MainDatabaseClass?) {
    var data: List<ContactWithMassengerEntity?>?
    val mainContactList: ArrayList<ContactWithMassengerEntity?>
    private var pass = false

    init {
        val massegeDao = db?.massegeDao()
        data = massegeDao?.getContactDetailsFromDatabase(MainActivity.user_login_id)
        mainContactList = ArrayList()
        mainContactList.addAll(data!!)
        pass = true
    }
}