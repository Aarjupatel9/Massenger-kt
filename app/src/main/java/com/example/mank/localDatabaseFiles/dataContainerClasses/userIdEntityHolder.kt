package com.example.mank.localDatabaseFiles.dataContainerClasses

import com.example.mank.localDatabaseFiles.MainDatabaseClass

class userIdEntityHolder(db: MainDatabaseClass) {
    //    public userIdEntityForApp getData(){
    //        return UserLoginId;
    //    }
    //    userIdEntityForApp UserLoginId;
    var userLoginId: String?
    var userMobileNumber: Long

    init {
        val userDao = db.userDao()
        userLoginId = userDao.userIdFromDatabase
        userMobileNumber = userDao.userMobileNumber
    }
}