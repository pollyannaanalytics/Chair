package com.example.reclaim.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",

    @ColumnInfo(name = "school_name")
    val schoolName: String = "",

    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "user_name")
    val userName: String = "",

    @ColumnInfo(name = "image_url")
    val imageUrl: String = "",

    @ColumnInfo(name = "gender")
    val gender: String = "",

    @ColumnInfo(name = "worry_type")
    var worryType: String = "",

    @ColumnInfo(name = "worries_description")
    var worriesDescription: String = ""



)