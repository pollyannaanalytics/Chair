package com.example.reclaim.chatgpt



data class MessageToGPT(
    val message: String,
    val sentBy: String,
    val timestamp: String
) {
    companion object{
        const val SENT_BY_USER = "sent_user"
        const val SENT_BY_BOT = "sent_bot"
    }
}