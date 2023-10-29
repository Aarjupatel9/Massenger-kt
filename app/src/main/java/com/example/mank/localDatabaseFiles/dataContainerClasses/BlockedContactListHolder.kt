package com.example.mank.localDatabaseFiles.dataContainerClasses

import com.example.mank.localDatabaseFiles.MainDatabaseClass
import com.example.mank.localDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.MainActivity

class BlockedContactListHolder(db: MainDatabaseClass?) {
    var data: List<ContactWithMassengerEntity?>?
    val mainContactList: ArrayList<ContactWithMassengerEntity?>
    private var pass = false
    val contactDao = db?.contactDao()

    init {
        data = contactDao?.getBlockedContactDetailsFromDatabase(MainActivity.user_login_id)
        mainContactList = ArrayList()
        mainContactList.addAll(data!!)
        pass = true
    }
}