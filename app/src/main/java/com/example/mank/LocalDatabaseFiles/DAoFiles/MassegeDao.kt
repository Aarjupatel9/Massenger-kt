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
interface MassegeDao {
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


	@Query("SELECT ChatId From massege where AppUserId=:appUserId ORDER BY ChatId DESC LIMIT 1")
	fun getLastInsertedMassegeChatId(appUserId: String?): Int

	@Query("SELECT massege From massege where (ReceiverId=:CID or SenderId=:CID) and AppUserId=:appUserId ORDER BY ChatId DESC LIMIT 1")
	fun getLastInsertedMassege(CID: String?, appUserId: String?): String?

	@Query("SELECT massege From massege where (ReceiverId=:CID and SenderId=:CID and AppUserId=:appUserId) ORDER BY ChatId DESC LIMIT 1")
	fun getSelfLastInsertedMassege(CID: String?, appUserId: String?): String?


}