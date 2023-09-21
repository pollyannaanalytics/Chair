package com.example.reclaim.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface ReclaimDatabaseDao {

    @Insert
    fun insertUserInfo(userInfo: UserInfo)

    @Insert
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Insert
    fun insertFriends(friends: Friends)

    @Insert
    fun insertChatRecord(chatRecord: ChatRecord)

    @Update
    fun receiveFriends(friends: Friends)

    @Update
    fun changeUserInfo(userInfo: UserInfo)

    @Update
    fun changeUserProfile(userProfile: UserProfile)


    @Query("DELETE from friends WHERE friend_user_id = :id")
    fun rejectFriend(id: String)

    @Query("SELECT * from user_profile WHERE worry_type = :worries")
    fun findFriend(worries: String): UserProfile

    @Insert
    suspend fun saveImages(images: Images)
}