package com.example.mank.localDatabaseFiles

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.example.mank.localDatabaseFiles.daoClasses.ContactsDao
import com.example.mank.localDatabaseFiles.daoClasses.MassegeDao
import com.example.mank.localDatabaseFiles.daoClasses.UserDao
import com.example.mank.localDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.localDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.localDatabaseFiles.entities.MassegeEntity
import com.example.mank.localDatabaseFiles.entities.SetupFirstTimeEntity
import com.example.mank.localDatabaseFiles.entities.loginDetailsEntity

@Database(
    entities = [MassegeEntity::class, loginDetailsEntity::class, ContactWithMassengerEntity::class, SetupFirstTimeEntity::class, AllContactOfUserEntity::class],
    version = 4, exportSchema = false
)
abstract class MainDatabaseClass : RoomDatabase() {
    abstract fun massegeDao(): MassegeDao
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactsDao

    companion object {
        @Volatile
        private var INSTANCE: MainDatabaseClass? = null
        private const val NUMBER_OF_THREADS = 4
        fun getDatabase(context: Context): MainDatabaseClass? {
            if (INSTANCE == null) {
                synchronized(MainDatabaseClass::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = databaseBuilder(
                            context.applicationContext,
                            MainDatabaseClass::class.java, "MassengerDatabase"
                        )
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}