package com.example.reclaim.data


data class UserProfile(



    val userId: String? = "",


    val userName: String? = "",


    val gender: String? = "",

    val worryType: String? = "",

    val worriesDescription: String? = "",


    val imageUri: String?,

    val age: String?,

    val selfDescription : String?,

    val profileTime: Long


)


data class Images(

    val id: Long = 0L,
    val userId: String?,
    val imageUri: String?
)