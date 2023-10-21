package com.example.reclaim.chatroom

import androidx.lifecycle.ViewModel
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.source.ChairRemoteDataSource
import com.example.reclaim.data.source.ChairRepository
import com.google.firebase.database.Query
import org.junit.Before
import org.junit.Test

class ChatRoomViewModelTest {

    private lateinit var chatRoomViewModel: ViewModel
    private lateinit var chairRepository: ChairRepository
    private lateinit var navArgs: ChatRoomFragmentArgs
    private lateinit var dataSource: ChairRemoteDataSource
    @Before
    fun setUp(){
        val fakeChatRoom = ChatRoom(
            id = "1",
            key = "chatRoomKey123",
            selfId = "selfUserId",
            otherId = "otherUserId",
            selfName = "YourName",
            otherName = "FriendName",
            lastSentence = "Hello, how are you?",
            sendById = "selfUserId",
            selfImage = "selfImageUrl",
            otherImage = "friendImageUrl",
            sentTime = "2023-10-21 10:30:00",
            unreadTimes = "3",
            otherOnline = true
        )

        dataSource = ChairRemoteDataSource()
        navArgs = ChatRoomFragmentArgs.Builder(fakeChatRoom).build()
        chairRepository = ChairRepository(dataSource)


        chatRoomViewModel = ChatRoomViewModel(chairRepository, navArgs)
    }

    @Test
    fun testGetAllRecord(){
        val fakeQuery = Query::class.java


    }

    @Test
    fun testClearUnreadCounts(){

    }
}