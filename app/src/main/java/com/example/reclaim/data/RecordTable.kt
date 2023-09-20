package com.example.reclaim.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.JsonAdapter
import com.squareup.moshi.Json
import kotlinx.serialization.json.JSON

@Entity(tableName = "record_table")
data class RecordTable(

    @PrimaryKey(autoGenerate = false)
    @Json(name = "record_id")
    val id: String ="",

    val content : String = "",

    @Json(name = "project_id")
    val projectId: String = "",

    @Json(name = "send_time")
    val sendTime: String = "",

    @Json(name = "user_id")
    val userId : String = ""
)