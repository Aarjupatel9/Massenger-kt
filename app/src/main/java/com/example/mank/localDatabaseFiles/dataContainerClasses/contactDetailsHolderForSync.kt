package com.example.mank.localDatabaseFiles.dataContainerClasses

import com.example.mank.localDatabaseFiles.MainDatabaseClass
import com.example.mank.localDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.MainActivity

class contactDetailsHolderForSync(db: MainDatabaseClass) {
    var allContact: List<AllContactOfUserEntity?>? = null
    var connectedContact: List<AllContactOfUserEntity?>? = null
    var disConnectedContact: List<AllContactOfUserEntity?>? = null

    init {
        val contactDao = db.contactDao()
        allContact = contactDao.getAllContactDetailsFromDB(MainActivity.user_login_id)
        connectedContact = contactDao.getConnectedContactDetailsFromDB(MainActivity.user_login_id)
        disConnectedContact =contactDao.getDisConnectedContactDetailsFromDB(MainActivity.user_login_id)
    }
}