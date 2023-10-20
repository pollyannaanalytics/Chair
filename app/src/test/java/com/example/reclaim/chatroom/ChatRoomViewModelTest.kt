package com.example.reclaim.chatroom

import androidx.navigation.NavArgs
import com.example.reclaim.data.source.ChairRepository
import com.google.errorprone.annotations.DoNotMock
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.webrtc.PeerConnection.Observer

class ChatRoomViewModelTest {
    @Mock
    private lateinit var repository: ChairRepository

    @Mock
    private lateinit var navArgs: ChatRoomFragmentArgs

    @Mock
    private lateinit var viewModel: ChatRoomViewModel




    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = ChatRoomViewModel(repository, navArgs = navArgs)

    }

    @Test
    fun getFriendImage() {
    }

    @Test
    fun getFriendName() {
    }

    @Test
    fun getRecordWithFriend() {
    }

    @Test
    fun getOnDestroyed() {
    }

    @Test
    fun getJoinMeetingId() {
    }

    @Test
    fun getRecordRegistraion() {
    }

    @Test
    fun setRecordRegistraion() {
    }

    @Test
    fun sendMessage() {
    }

    @Test
    fun sendVideoCallMessage() {
    }

    @Test
    fun joinMeeting() {
    }

    @Test
    fun removeListener() {
    }

    @Test
    fun stopUserJoinMeeting() {
    }

    @Test
    fun onCleared() {
    }
}