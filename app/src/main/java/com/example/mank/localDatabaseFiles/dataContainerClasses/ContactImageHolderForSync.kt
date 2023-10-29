package com.example.mank.localDatabaseFiles.dataContainerClasses

import com.example.mank.localDatabaseFiles.MainDatabaseClass
import com.example.mank.localDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.MainActivity

class ContactImageHolderForSync(db: MainDatabaseClass) {
    val imageOfConnectedContact: List<AllContactOfUserEntity?>?

    init {
        val contactDao = db.contactDao()
        imageOfConnectedContact =
            contactDao?.getConnectedContactImageList(MainActivity.user_login_id)
    }
}