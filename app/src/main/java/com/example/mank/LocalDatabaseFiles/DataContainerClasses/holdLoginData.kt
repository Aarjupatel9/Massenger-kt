package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import com.example.mank.LocalDatabaseFiles.entities.loginDetailsEntity
import com.example.mank.MainActivity

class holdLoginData {
    var data: loginDetailsEntity?

    init {
        val massegeDao = MainActivity.db!!.massegeDao()
        data = massegeDao.loginDetailsFromDatabase
    }
}