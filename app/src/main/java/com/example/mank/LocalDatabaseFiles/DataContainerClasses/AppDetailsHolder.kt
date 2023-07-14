package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.SetupFirstTimeEntity
import java.util.Date

class AppDetailsHolder(db: MainDatabaseClass) {
    var data: SetupFirstTimeEntity?
    var massegeDao: MassegeDao

    init {
        massegeDao = db.massegeDao()!!
        data = massegeDao.lastAppOpenEntity
    }

    fun addThisDetails() {
        val date = Date()
        val new_details = SetupFirstTimeEntity(date.time)
        massegeDao.addAppOpenDetails(new_details)
    }
}