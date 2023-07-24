package com.example.mank.LocalDatabaseFiles.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mank.MainActivity

@Entity(tableName = "contactDetails")
class ContactWithMassengerEntity {
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "index")
	var index: Long = 0

	@ColumnInfo(name = "CID")
	var CID: String? = null

	@ColumnInfo(name = "AppUserId")
	var appUserId: String? = null

	@ColumnInfo(name = "MobileNumber")
	var MobileNumber: Long? = null

	@ColumnInfo(name = "DisplayName")
	var DisplayName: String? = null

	@ColumnInfo(name = "savedName")
	var savedName: String? = null

	@ColumnInfo(name = "About")
	var about = "jai shree krushn"

	@ColumnInfo(name = "UserImage")
	var userImage: ByteArray? = null

	@ColumnInfo(name = "ProfileImageVersion")
	var profileImageVersion: Long = 0

	@ColumnInfo(name = "PriorityRank")
	var priorityRank: Long = 0

	@ColumnInfo(name = "LocallySaved")
	var locallySaved = 1

	@ColumnInfo(name = "NewMassegeArriveValue")
	var newMassegeArriveValue = 0

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
	var isTouchEffectPass = false
	var lastMassege = ""

	constructor() {}
	constructor(MobileNumber: Long?, DisplayName: String?, CID: String?) {
		this.MobileNumber = MobileNumber
		this.DisplayName = DisplayName
		savedName = DisplayName
		this.CID = CID
		appUserId = MainActivity.user_login_id
	}

	constructor(MobileNumber: Long?, DisplayName: String?, CID: String?, priorityRank: Long) {
		this.MobileNumber = MobileNumber
		this.DisplayName = DisplayName
		savedName = DisplayName
		this.CID = CID
		this.priorityRank = priorityRank
		appUserId = MainActivity.user_login_id
	}

	constructor(MobileNumber: Long?, DisplayName: String?, CID: String?, LocallySaved: Int) {
		this.MobileNumber = MobileNumber
		this.DisplayName = DisplayName
		this.CID = CID
		locallySaved = LocallySaved
		appUserId = MainActivity.user_login_id
	}

}