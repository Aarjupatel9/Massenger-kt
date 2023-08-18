package com.example.mank.LocalDatabaseFiles.DAoFiles

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mank.LocalDatabaseFiles.entities.AllContactOfUserEntity
import com.example.mank.LocalDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.LocalDatabaseFiles.entities.MassegeEntity
import com.example.mank.LocalDatabaseFiles.entities.SetupFirstTimeEntity
import com.example.mank.LocalDatabaseFiles.entities.loginDetailsEntity

@Dao
interface UserDao {


	//fro Login activity
	@get:Query("SELECT * FROM login limit 1")
	val loginDetailsFromDatabase: loginDetailsEntity?

	@Insert
	fun SaveLoginDetailsInDatabase(loginDetailsEntity: loginDetailsEntity?)

	@Query("UPDATE login SET DisplayUserName = :displayUserName WHERE UID = :u_id")
	fun updateDisplayUserName(displayUserName: String?, u_id: String?): Int

	@Query("UPDATE login SET es1 = :email ,elf1 = :elf1 WHERE UID=:appUserId")
	fun updateUserLoginRecoveryEmail(email: String?, elf1: Long, appUserId: String): Int

	@Query("UPDATE login SET About=:about WHERE UID = :u_id")
	fun updateAboutUserName(about: String?, u_id: String?): Int

	//for logout
	@Query("delete from login where UID =:appUserId")
	fun LogOutFromAppForThisUser(appUserId: String?)

	//getting user_id for app
	@get:Query("SELECT UID FROM login limit 1")
	val userIdFromDatabase: String?

	@get:Query("SELECT MobileNumber FROM login limit 1")
	val userMobileNumber: Long

	@Insert
	fun addAppOpenDetails(setupFirstTimeEntity: SetupFirstTimeEntity?)

	@get:Query("SELECT * From app_open_details")
	val listOfAppOpenDetails: List<SetupFirstTimeEntity?>?

	@get:Query("SELECT * From app_open_details order by appOpenNumber DESC limit 1")
	val lastAppOpenEntity: SetupFirstTimeEntity?

	@Insert
	fun insertLastAppOpenEntity(setupFirstTimeEntity: SetupFirstTimeEntity?)

	@Query("update login set elf1=:elf1 where UID=:appUserId")
	fun updateProfileImageVersion(appUserId: String?, elf1: Long): Int

	@Query("select elf1 from login where UID=:appUserId")
	fun getProfileImageVersion(appUserId: String?): Long

}