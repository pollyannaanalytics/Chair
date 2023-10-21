package com.example.reclaim.chatroom

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.reclaim.data.ChatRecord
import com.example.reclaim.data.ChatRoom
import com.example.reclaim.data.MessageType
import com.example.reclaim.data.source.ChairRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify


class ChatRoomViewModelTest {

    companion object {
        private const val fakeUserName = "pollyanna"
        private const val fakeUserImage = "pollyannaImage"
        private const val fakeMeetingID = "meeting123"

        private val fakeChatRoom = ChatRoom(
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


    }

    private lateinit var chatRoomViewModel: ChatRoomViewModel

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var chairRepository: ChairRepository
    private lateinit var navArgs: ChatRoomFragmentArgs
    private lateinit var recordRegistration: ListenerRegistration
    private var fakeDocumentID: String = ""

    @Before
    fun setUp() {

        MockitoAnnotations.initMocks(this)

        recordRegistration = mock<ListenerRegistration>()
        navArgs = ChatRoomFragmentArgs.Builder(fakeChatRoom).build()


        chatRoomViewModel = ChatRoomViewModel(chairRepository, navArgs, fakeUserName, fakeUserImage)
    }


    @Test
    fun getAllRecordFromRoom() {
        val fakeChatRoomKey = "chatRoomKey1"

        val fakeQuery = mock<Query>()
        val fakeDocumentSnapshot = mock<DocumentSnapshot>()




        Mockito.`when`(chairRepository.getAllRecordFromRoom(eq(fakeChatRoomKey), anyOrNull()))
            .thenAnswer {

                val callback = it.arguments[1] as (Query, DocumentSnapshot) -> Unit
                fakeDocumentID = if (fakeDocumentSnapshot.id != null) {
                    fakeDocumentSnapshot.id
                } else {
                    ""
                }

                callback(fakeQuery, fakeDocumentSnapshot)
            }



        chatRoomViewModel.getAllRecordFromRoom(fakeChatRoomKey)

        val recordObserver = Observer<MutableList<ChatRecord>> { recordList ->
            assert(recordList is List<ChatRecord>)

        }

        chatRoomViewModel.recordWithFriend.observeForever(recordObserver)

    }


    @Test
    fun testClearUnreadCounts() {


        chatRoomViewModel.clearUnreadCounts(fakeDocumentID)

        verify(chairRepository).clearUnreadCounts(fakeDocumentID)

    }


    @Test
    fun testUpdateSeenStatus() {
        val fakeChatRoomID = fakeChatRoom.id


        chatRoomViewModel.updateSeenStatus(fakeChatRoomID, fakeDocumentID)

        verify(chairRepository).updateSeenStatus(fakeChatRoomID, fakeDocumentID)
    }

    @Test
    fun testSendMessage() {
        val fakeContent = "Hello World"
        val fakeType = MessageType.MESSAGE
        val meetingID = "asfasdf"


        chatRoomViewModel.sendMessage(fakeContent, fakeType, meetingID)
        verify(chairRepository).sendMessage(
            fakeContent,
            fakeType,
            meetingID,
            fakeChatRoom,
            fakeDocumentID,
            fakeUserName,
            fakeUserImage
        )

    }


    @Test
    fun testSendVideoCallMessage() {

        chatRoomViewModel.sendVideoCallMessage(fakeMeetingID)
        verify(chairRepository).sendVideoCallMessage(
            fakeMeetingID,
            chatRoom = fakeChatRoom,
            fakeDocumentID,
            fakeUserName,
            fakeUserImage
        )
    }

    @Test
    fun testJoinMeeting() {

        chatRoomViewModel.joinMeeting(fakeMeetingID)

        val meetingIDObserver = Observer<String> { meetingID ->
            assert(meetingID is String)
        }

        chatRoomViewModel.joinMeetingId.observeForever(meetingIDObserver)
    }


    @Test
    fun testStopUserJoinMeeting(){
        val meetingID = ""

        chatRoomViewModel.stopUserJoinMeeting()

        verify(chairRepository).stopUserJoinMeeting(fakeDocumentID, meetingID)

    }

}