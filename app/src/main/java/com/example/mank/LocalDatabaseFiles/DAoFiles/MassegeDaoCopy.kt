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
interface MassegeDaoCopy {
	//massege related query
	@Query("SELECT * FROM massege WHERE ((ReceiverId IN (:ReceiverId) or SenderId IN(:ReceiverId)) and AppUserId=:appUserId )order by timeOfSend")
	fun getChat(ReceiverId: String?, appUserId: String?): List<MassegeEntity?>?

	@Query("SELECT * FROM massege WHERE ((ReceiverId IN (:ReceiverId) and SenderId IN(:ReceiverId)) and AppUserId=:appUserId) order by timeOfSend")
	fun getSelfChat(ReceiverId: String?, appUserId: String?): List<MassegeEntity?>?

	@Query("SELECT * FROM massege WHERE (massegeStatus=:status and AppUserId=:appUserId) ")
	fun getMassegesWithStatus(status: Int, appUserId: String?): List<MassegeEntity?>?

	@Query("SELECT massegeStatus FROM massege WHERE (SenderId=:senderId and ReceiverId=:receiverId and timeOfSend=:time and AppUserId=:appUserId)")
	fun getMassegeStatus(senderId: String?, receiverId: String?, time: Long, appUserId: String?
	): Int

	@Query("UPDATE massege SET massegeStatus=:massegeStatus WHERE (SenderId = :SenderId and ReceiverId=:ReceiverId and timeOfSend=:sentTime and AppUserId=:appUserId)")
	fun updateMassegeStatus(SenderId: String?, ReceiverId: String?, sentTime: Long, massegeStatus: Int, appUserId: String?
	): Int

	@Query("delete from massege where ((ReceiverId IN (:CID) or SenderId IN(:CID)) and AppUserId=:appUserId)")
	fun removeChatsFromMassegeTable(CID: String?, appUserId: String?): Int

	// inbuilt function of room
	@Insert
	fun insertMassegeIntoChat(massegeEntity: MassegeEntity?)

	//select queries
	@Query("SELECT * FROM massege WHERE (timeOfSend=:TimeOfSend and SenderId=:SenderId and AppUserId=:appUserId)")
	fun getMassegeByTimeOfSend(SenderId: String?, TimeOfSend: Long, appUserId: String?
	): MassegeEntity?

	@Query("SELECT * FROM massege where AppUserId=:appUserId")
	fun getMassegeByAppUserId(appUserId: String?): List<MassegeEntity?>?

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

	//contact related query

	@Query("SELECT * FROM contactDetails where AppUserId=:appUserId order by PriorityRank DESC")
	fun getContactDetailsFromDatabase(appUserId: String?): List<ContactWithMassengerEntity?>?

	@Query("SELECT * FROM contactDetails where CID=:CID and AppUserId=:appUserId limit 1")
	fun checkCIDInConnectedSavedContact(CID: String?, appUserId: String?): ContactWithMassengerEntity

	@Query("SELECT * FROM contactDetails where CID=:CID and AppUserId=:appUserId")
	fun getContactWith_CID(CID: String?, appUserId: String?): ContactWithMassengerEntity?

	@Query("SELECT PriorityRank FROM contactDetails where AppUserId=:appUserId order by PriorityRank desc limit 1 ")
	fun getHighestPriorityRank(appUserId: String?): Long

	@Query("update contactDetails set PriorityRank=:PriorityRank where CID=:CID and AppUserId=:appUserId")
	fun setPriorityRank(CID: String?, PriorityRank: Long, appUserId: String?)

	@Query("update contactDetails set UserImage=:userImage where (CID=:CID and AppUserId=:appUserId)")
	fun updateImageIntoContactDetails(CID: String?, userImage: ByteArray?, appUserId: String?): Int

	@Query("select UserImage from contactDetails  where CID=:CID and AppUserId=:appUserId")
	fun getSelfUserImage(CID: String?, appUserId: String?): ByteArray?

	@Insert
	fun SaveContactDetailsInDatabase(ContactWithMassengerEntity: ContactWithMassengerEntity?)

	@Query("Delete From contactDetails Where (MobileNumber=(:number) and AppUserId=:appUserId)")
	fun deleteContactDetailsInDatabase(number: Long?, appUserId: String?)

	@Query("Delete From contactDetails Where CID=(:CID) and AppUserId=:appUserId")
	fun removeSelfContactFromContactTable(CID: String?, appUserId: String?): Int

	@Query("SELECT ChatId From massege where AppUserId=:appUserId ORDER BY ChatId DESC LIMIT 1")
	fun getLastInsertedMassegeChatId(appUserId: String?): Int

	@Query("SELECT massege From massege where (ReceiverId=:CID or SenderId=:CID) and AppUserId=:appUserId ORDER BY ChatId DESC LIMIT 1")
	fun getLastInsertedMassege(CID: String?, appUserId: String?): String?

	@Query("SELECT massege From massege where (ReceiverId=:CID and SenderId=:CID and AppUserId=:appUserId) ORDER BY ChatId DESC LIMIT 1")
	fun getSelfLastInsertedMassege(CID: String?, appUserId: String?): String?

	@Query("update contactDetails set NewMassegeArriveValue=(:value) where CID=(:cId) and AppUserId=:appUserId")
	fun updateNewMassegeArriveValue(cId: String?, value: Int, appUserId: String?)

	@Query("update contactDetails set NewMassegeArriveValue=NewMassegeArriveValue+1 where CID=(:cId) and AppUserId=:appUserId")
	fun incrementNewMassegeArriveValue(cId: String?, appUserId: String?)

	//for first time setup
	@Insert
	fun addAppOpenDetails(setupFirstTimeEntity: SetupFirstTimeEntity?)

	@get:Query("SELECT * From app_open_details")
	val listOfAppOpenDetails: List<SetupFirstTimeEntity?>?

	@get:Query("SELECT * From app_open_details order by appOpenNumber DESC limit 1")
	val lastAppOpenEntity: SetupFirstTimeEntity?

	@Insert
	fun insertLastAppOpenEntity(setupFirstTimeEntity: SetupFirstTimeEntity?)

	@Query("update allContactDetails set CID=:CID where MobileNumber=:number and AppUserId=:appUserId")
	fun updateAllContactOfUserEntityCID(number: Long, CID: String?, appUserId: String?): Int

	@Query("SELECT * from allContactDetails where mobilenumber=:number and AppUserId=:appUserId")
	fun getSelectedAllContactOfUserEntity(number: Long, appUserId: String?
	): List<AllContactOfUserEntity?>?

//	@Query("UPDATE allContactDetails set DisplayName=:displayUserName  where MobileNumber =:number and AppUserId=:appUserId")
//	fun updateSavedNameOfUserEntity(number: Long, displayUserName: String?, appUserId: String?
//	): Int

	@Query("UPDATE allContactDetails SET DisplayName=:displayUserName WHERE MobileNumber=:number AND AppUserId=:appUserId")
	fun updateSavedNameOfUserEntity(number: Long, displayUserName: String?, appUserId: String?): Int
	@Query("UPDATE contactDetails SET DisplayName=:savedName , savedName=:savedName WHERE MobileNumber=:number AND AppUserId=:appUserId") //displayname updation may be reduce in future
	fun updateSavedNameOfContactEntity(number: Long, savedName: String?, appUserId: String?): Int

	@Update
	fun updateAllContactOfUserEntity(allContactOfUserEntity: AllContactOfUserEntity?): Int

	@Insert
	fun addAllContactOfUserEntity(allContactOfUserEntity: AllContactOfUserEntity?)

	@Query("select  * from allContactDetails where AppUserId=:appUserId")
	fun getAllContactDetailsFromDB(appUserId: String?): List<AllContactOfUserEntity?>?

	@Query("select * from allContactDetails where CID==-1 and AppUserId=:appUserId")
	fun getDisConnectedContactDetailsFromDB(appUserId: String?): List<AllContactOfUserEntity?>?

	@Query("select * from allContactDetails where CID>-1 and AppUserId=:appUserId")
	fun getConnectedContactDetailsFromDB(appUserId: String?): List<AllContactOfUserEntity?>?

	@Query("select * from allContactDetails where CID>-1 and AppUserId=:appUserId ")
	fun getConnectedContactImageList(appUserId: String?): List<AllContactOfUserEntity?>?

	@Query("update contactDetails set ProfileImageVersion=:ProfileImageVersion where CID=:CID and AppUserId=:appUserId")
	fun updateProfileImageVersion(CID: String?, ProfileImageVersion: Long, appUserId: String?): Int

	@Query("select  ProfileImageVersion from  contactDetails where CID=:CID and AppUserId=:appUserId")
	fun getContactProfileImageVersion(CID: String?, appUserId: String?): Long
}