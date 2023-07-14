package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.MainActivity

class ContactImageHolderForSync(db: MainDatabaseClass) {
    val imageOfConnectedContact: List<AllContactOfUserEntity?>?

    init {
        val massegeDao = db.massegeDao()
        imageOfConnectedContact =
            massegeDao?.getConnectedContactImageList(MainActivity.user_login_id)
    }
}