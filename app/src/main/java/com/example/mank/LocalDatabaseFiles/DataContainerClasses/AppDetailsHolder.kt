package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.DAoFiles.UserDao
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.SetupFirstTimeEntity
import java.util.Date

class AppDetailsHolder(db: MainDatabaseClass) {
    var data: SetupFirstTimeEntity?
    var userDao: UserDao? = null

    init {
        userDao = db.userDao()
        data = userDao?.lastAppOpenEntity
    }

    fun addThisDetails() {
        val date = Date()
        val new_details = SetupFirstTimeEntity(date.time)
        userDao?.addAppOpenDetails(new_details)
    }
}