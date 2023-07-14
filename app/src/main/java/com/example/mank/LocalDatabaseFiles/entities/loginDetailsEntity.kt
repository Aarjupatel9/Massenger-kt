package com.example.mank.LocalDatabaseFiles.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "login")
class loginDetailsEntity {
    @PrimaryKey
    lateinit var UID: String

    @ColumnInfo(name = "MobileNumber")
    var MobileNumber: Long? = null

    @ColumnInfo(name = "password")
    var Password: String? = null

    @ColumnInfo(name = "DisplayUserName")
    var displayUserName: String? = null

    @ColumnInfo(name = "About")
    var about: String? = null

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
    constructor(
        UID: String,
        Password: String,
        MobileNumber: Long?,
        DisplayUserName: String?,
        about: String?
    ) {
        this.Password = Password
        this.MobileNumber = MobileNumber
        this.UID = UID
        displayUserName = DisplayUserName
        this.about = about
    }


}