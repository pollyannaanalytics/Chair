package com.example.reclaim.chatroom

import androidx.lifecycle.ViewModel
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.source.ChairRemoteDataSource
import com.example.reclaim.data.source.ChairRepository
import org.junit.Before

class ChatRoomViewModelTest {

    lateinit var chatRoomViewModel: ViewModel
    lateinit var chairRepository: ChairRepository
    lateinit var navArgs: ChatRoomFragmentArgs
    lateinit var dataSource: ChairRemoteDataSource
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
}