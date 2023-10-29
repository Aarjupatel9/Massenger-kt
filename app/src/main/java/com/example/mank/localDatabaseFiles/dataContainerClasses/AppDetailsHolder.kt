package com.example.mank.localDatabaseFiles.dataContainerClasses

import com.example.mank.localDatabaseFiles.daoClasses.UserDao
import com.example.mank.localDatabaseFiles.MainDatabaseClass
import com.example.mank.localDatabaseFiles.entities.SetupFirstTimeEntity
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