package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.MassegeEntity
import com.example.mank.MainActivity

class MassegeHolderForSpecificPurpose(db: MainDatabaseClass, code: Int) {
    var massegeList: List<MassegeEntity?>? = null

    init {
        val massegeDao = db.massegeDao()
        if (code == -1) {
            massegeList = massegeDao.getMassegesWithStatus(-1, MainActivity.user_login_id)
        } else if (code == 0) {
            massegeList = massegeDao.getMassegesWithStatus(0, MainActivity.user_login_id)
        } else if (code == 2) {
            massegeList = massegeDao.getMassegeByAppUserId(MainActivity.user_login_id)
        }
    }
}