package com.example.reclaim.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "friends")
data class Friends(
@PrimaryKey(autoGenerate = false)
    val id: String = "",

    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "friend_user_id")
    val friendUserId: String = "",

    @ColumnInfo(name = "current_relation_ship")
    val currentRelationship: String = ""

)