package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.MainActivity

class contactDetailsHolderForSync(db: MainDatabaseClass) {
    var allContact: List<AllContactOfUserEntity?>? = null
    var connectedContact: List<AllContactOfUserEntity?>? = null
    var disConnectedContact: List<AllContactOfUserEntity?>? = null

    init {
        val massegeDao = db.massegeDao()
        allContact = massegeDao.getAllContactDetailsFromDB(MainActivity.user_login_id)
        connectedContact = massegeDao.getConnectedContactDetailsFromDB(MainActivity.user_login_id)
        disConnectedContact =massegeDao.getDisConnectedContactDetailsFromDB(MainActivity.user_login_id)
    }
}