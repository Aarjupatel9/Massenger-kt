package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.MassegeEntity
import com.example.mank.MainActivity

class ContactMassegeHolder(db: MainDatabaseClass, CID: String?) {
    var massegeList: List<MassegeEntity?>? = null

    init {
        val massegeDao = db.massegeDao()
        if (CID == MainActivity.user_login_id) {
            massegeList = massegeDao.getSelfChat(CID, MainActivity.user_login_id)
        } else {
            massegeList = massegeDao.getChat(CID, MainActivity.user_login_id)
        }
    }
}