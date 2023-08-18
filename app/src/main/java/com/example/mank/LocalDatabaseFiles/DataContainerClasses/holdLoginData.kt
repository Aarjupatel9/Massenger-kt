package com.example.mank.LocalDatabaseFiles.DataContainerClasses

import androidx.room.Room
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.LocalDatabaseFiles.entities.loginDetailsEntity
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