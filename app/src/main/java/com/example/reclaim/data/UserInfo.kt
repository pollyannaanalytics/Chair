package com.example.reclaim.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey(autoGenerate = true)
    var id : String = "",

    @ColumnInfo(name = "email")
    var email : String = "",

    @ColumnInfo(name = "user_name")
    var userName : String = "",

    @ColumnInfo(name = "password")
    var password : String = "",

    @ColumnInfo(name = "gender")
    var gender: String = ""


)

