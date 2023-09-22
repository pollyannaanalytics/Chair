package com.example.reclaim.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: String? = "",

    @ColumnInfo(name = "user_name")
    val userName: String? = "",

    @ColumnInfo(name = "gender")
    val gender: String? = "",

    @ColumnInfo(name = "worry_type")
    var worryType: String? = "",

    @ColumnInfo(name = "worries_description")
    var worriesDescription: String? = "",

    @ColumnInfo(name = "image_uri")
    var imageUri: String?


)

@Entity(tableName = "images")
data class Images(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String?,
    val imageUri: String?
)