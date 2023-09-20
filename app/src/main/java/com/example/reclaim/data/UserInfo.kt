package com.example.reclaim.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0L,
    var account : String = "",
    var userName : String = "",
    var password : String = "",
    var personalTaskId : String = "",
    var projectId : String = ""
)

