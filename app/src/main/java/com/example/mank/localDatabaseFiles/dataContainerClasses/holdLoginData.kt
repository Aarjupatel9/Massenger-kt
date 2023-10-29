package com.example.mank.localDatabaseFiles.dataContainerClasses

import androidx.room.Room
import com.example.mank.localDatabaseFiles.MainDatabaseClass
import com.example.mank.localDatabaseFiles.entities.loginDetailsEntity
import com.example.mank.MainActivity.Companion.MainActivityStaticContext

class holdLoginData {
    var data: loginDetailsEntity? = null
    var db: MainDatabaseClass?

    init {
        db = MainActivityStaticContext?.let {
            Room.databaseBuilder(
                it,
                MainDatabaseClass::class.java,
                "MassengerDatabase"
            ).fallbackToDestructiveMigration().allowMainThreadQueries().build()

        }
        if (db != null) {
            val userDao = db?.userDao()

            data = userDao?.loginDetailsFromDatabase
        }
    }
}