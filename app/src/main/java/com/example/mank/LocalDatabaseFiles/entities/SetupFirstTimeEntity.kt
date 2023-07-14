package com.example.mank.LocalDatabaseFiles.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "app_open_details")
class SetupFirstTimeEntity {
    @PrimaryKey(autoGenerate = true)
    var appOpenNumber = 0

    @ColumnInfo(name = "LastOpenTime")
    var date: Long
        get() = field
        set

    @ColumnInfo(name = "es1")
    var es1 = ""

    @ColumnInfo(name = "es2")
    var es2 = ""

    @ColumnInfo(name = "elf2")
    var elf2: Long = 0

    @ColumnInfo(name = "elf1")
    var elf1: Long = 0

    @ColumnInfo(name = "elf3")
    var elf3: Long = 0

    @ColumnInfo(name = "elf4")
    var elf4: Long = 0

    constructor() {
        val date = Date()
        this.date = date.time
    }

    constructor(date: Long) {
        this.date = date
    }
}