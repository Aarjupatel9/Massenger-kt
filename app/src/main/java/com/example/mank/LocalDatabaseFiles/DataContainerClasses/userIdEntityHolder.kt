package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import com.example.mank.LocalDatabaseFiles.MainDatabaseClass

class userIdEntityHolder(db: MainDatabaseClass) {
    //    public userIdEntityForApp getData(){
    //        return UserLoginId;
    //    }
    //    userIdEntityForApp UserLoginId;
    var userLoginId: String?
    var userMobileNumber: Long

    init {
        val massegeDao = db.massegeDao()
        userLoginId = massegeDao.userIdFromDatabase
        userMobileNumber = massegeDao.userMobileNumber
    }
}