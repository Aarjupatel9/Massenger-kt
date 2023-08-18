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
interface ContactsDao {

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


	@Query("update contactDetails set NewMassegeArriveValue=(:value) where CID=(:cId) and AppUserId=:appUserId")
	fun updateNewMassegeArriveValue(cId: String?, value: Int, appUserId: String?)

	@Query("update contactDetails set NewMassegeArriveValue=NewMassegeArriveValue+1 where CID=(:cId) and AppUserId=:appUserId")
	fun incrementNewMassegeArriveValue(cId: String?, appUserId: String?)


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