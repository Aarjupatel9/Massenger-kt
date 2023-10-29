package com.example.mank.localDatabaseFiles.dataContainerClasses

import com.example.mank.localDatabaseFiles.MainDatabaseClass
import com.example.mank.localDatabaseFiles.entities.MassegeEntity
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