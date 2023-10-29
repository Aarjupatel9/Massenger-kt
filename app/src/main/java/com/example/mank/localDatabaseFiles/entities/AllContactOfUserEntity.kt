package com.example.mank.localDatabaseFiles.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mank.MainActivity
import java.util.Date

@Entity(tableName = "allContactDetails")
class AllContactOfUserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "index")
    var index = 0

    @ColumnInfo(name = "AppUserId")
    var appUserId: String? = null

    @ColumnInfo(name = "MobileNumber")
    var MobileNumber: Long? = null

    @ColumnInfo(name = "CID")
    var CID: String? = null

    @ColumnInfo(name = "DisplayName")
    var DisplayName: String? = null

    @ColumnInfo(name = "About")
    var about = "jai shree krushn"

    @ColumnInfo(name = "time")
    var timestamp: Long = 0
        get() = field

    @ColumnInfo(name = "ImageVersion")
    var imageVersion: Long = 0

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

    constructor() {}
    constructor(MobileNumber: Long, Display_name: String?, CID: String) {
        this.MobileNumber = MobileNumber
        DisplayName = Display_name
        this.CID = CID
        timestamp = Date().time
        appUserId = MainActivity.user_login_id
    }

    constructor(MobileNumber: Long, Display_name: String?, CID: String, es1: String, es2: String) {
        this.MobileNumber = MobileNumber
        DisplayName = Display_name
        this.CID = CID
        timestamp = Date().time
        appUserId = MainActivity.user_login_id
        this.es1 = es1
        this.es2 = es2
    }

    constructor(
        MobileNumber: Long,
        Display_name: String?,
        CID: String,
        elf1: Long,
        elf2: Long,
        elf3: Long,
        elf4: Long,
        es1: String,
        es2: String
    ) {
        this.MobileNumber = MobileNumber
        DisplayName = Display_name
        this.CID = CID
        timestamp = Date().time
        appUserId = MainActivity.user_login_id
        this.elf1 = elf1
        this.elf2 = elf2
        this.elf3 = elf3
        this.elf4 = elf4
        this.es1 = es1
        this.es2 = es2
    }

    constructor(
        MobileNumber: Long,
        Display_name: String?,
        CID: String,
        elf1: Long,
        elf2: Long,
        elf3: Long,
        elf4: Long
    ) {
        this.MobileNumber = MobileNumber
        DisplayName = Display_name
        this.CID = CID
        timestamp = Date().time
        appUserId = MainActivity.user_login_id
        this.elf1 = elf1
        this.elf2 = elf2
        this.elf3 = elf3
        this.elf4 = elf4
    }
}