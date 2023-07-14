package com.example.mank.LocalDatabaseFiles

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.LocalDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.LocalDatabaseFiles.entities.MassegeEntity
import com.example.mank.LocalDatabaseFiles.entities.SetupFirstTimeEntity
import com.example.mank.LocalDatabaseFiles.entities.loginDetailsEntity
import java.util.concurrent.Executors

@Database(
    entities = [MassegeEntity::class, loginDetailsEntity::class, ContactWithMassengerEntity::class, SetupFirstTimeEntity::class, AllContactOfUserEntity::class],
    version = 2
)
abstract class MainDatabaseClass : RoomDatabase() {
    abstract fun massegeDao(): MassegeDao

    companion object {
        @Volatile
        private var INSTANCE: MainDatabaseClass? = null
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
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