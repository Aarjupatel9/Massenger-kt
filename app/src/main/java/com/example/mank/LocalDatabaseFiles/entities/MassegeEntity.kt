package com.example.mank.LocalDatabaseFiles.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mank.MainActivity

@Entity(
    tableName = "massege",
    indices = [Index(value = ["SenderId", "ReceiverId", "timeOfSend"], unique = true)]
)
class MassegeEntity {
    @PrimaryKey(autoGenerate = true)
    var chatId: Long = 0

    @ColumnInfo(name = "AppUserId")
    var appUserId: String? = null

    @ColumnInfo(name = "SenderId")
    var senderId: String? = null

    @ColumnInfo(name = "ReceiverId")
    var receiverId: String? = null

    @ColumnInfo(name = "massege")
    var massege: String? = null

    @ColumnInfo(name = "timeOfSend")
    var timeOfSend: Long = 0

    //0 for sent to server , 1 for reached at server, 2 for reach at other side, 3 for viewed, 4 for delete, 5 for not sent to server
    @ColumnInfo(name = "MassegeStatus")
    var massegeStatus = 0

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
        UserId: String?,
        CID: String?,
        massege: String?,
        timeOfSend: Long,
        massegeStatus: Int
    ) {
        senderId = UserId
        receiverId = CID
        this.massege = massege
        this.timeOfSend = timeOfSend
        this.massegeStatus = massegeStatus
        appUserId = MainActivity.user_login_id
    }
    constructor(
        UserId: String?,
        CID: String?,
        massege: String?,
        timeOfSend: Long,
        massegeStatus: Int,
        es1 :String,
        es2 :String,
        elf1:Long,
        elf2 :Long,
        elf3 :Long,
        elf4 :Long,
    ) {
        senderId = UserId
        receiverId = CID
        this.massege = massege
        this.timeOfSend = timeOfSend
        this.massegeStatus = massegeStatus
        appUserId = MainActivity.user_login_id
        
        this.es1 = es1;
        this.es2 = es2;
        this.elf1 = elf1;
        this.elf2 = elf2;
        this.elf3 = elf3;
        this.elf4 = elf4;
    }
}