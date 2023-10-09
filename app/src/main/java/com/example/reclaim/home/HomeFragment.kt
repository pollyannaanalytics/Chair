package com.example.reclaim.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.ui.text.font.Typeface
import androidx.core.animation.addListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.reclaim.R
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.data.UserManager
import com.example.reclaim.data.UserProfile
import com.example.reclaim.databinding.FragmentHomeBinding
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "HOMEFRAGMENT"


data class FriendInfo(
    var friendId: String? = null,
    var friendName: String? = null,
    var friendImg: String? = null
)

class HomeFragment : Fragment() {


    var manager: CardStackLayoutManager? = null
    var OtherUserNumber: Int? = null
    var OtherInfoList = emptyList<FriendInfo>().toMutableList()


    private var viewModel: HomeViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

// Inflate the layout for this fragment
        val application = requireNotNull(this.activity).application
        val databaseDao = ReclaimDatabase.getInstance(application).reclaimDao()
        val homeFactory = HomeFactory(databaseDao)
        viewModel = ViewModelProvider(this, homeFactory).get(HomeViewModel::class.java)

        val binding = FragmentHomeBinding.inflate(inflater)


        binding.viewModel = viewModel

        viewModel?.loadWhoLikeMe(UserManager.userId)


        fun loadingAvatar() {
            binding.selfAvatarLoadingContainer.visibility = View.VISIBLE
            binding.selfAvatarLoadingImg.visibility = View.VISIBLE
            binding.loadingText.visibility = View.VISIBLE
            binding.selfWrapperImg.visibility = View.VISIBLE
            val selfAvatar = binding.selfWrapperImg

            val scaleUpX =
                ObjectAnimator.ofFloat(selfAvatar, "scaleX", 1.0f, 1.2f, 1.0f, 1.2f, 1.0f)
            val scaleUpY =
                ObjectAnimator.ofFloat(selfAvatar, "scaleY", 1.0f, 1.2f, 1.0f, 1.2f, 1.0f)

            val alphaChange = ObjectAnimator.ofFloat(selfAvatar, "alpha", 0f, 1f, 0f, 1f, 0f)


            val scaleAnim = AnimatorSet()
            scaleAnim.playTogether(scaleUpX, scaleUpY, alphaChange)
            scaleAnim.duration = 5000
            scaleAnim.start()
            scaleAnim.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                    super.onAnimationEnd(animation, isReverse)
                    animation.start()
                }
            })


        }

        fun init() {
            manager = CardStackLayoutManager(requireContext(), object : CardStackListener {
                override fun onCardDragging(direction: Direction?, ratio: Float) {

                }

                override fun onCardSwiped(direction: Direction?) {
                    val currentFriend = OtherInfoList[manager.let { it!!.topPosition } - 1]
                    if (direction != null) {
                        Log.i(TAG, "current user is ${currentFriend.friendName}")
                        viewModel?.findRelationship(
                            currentFriend.friendId!!,
                            currentFriend.friendName!!,
                            currentFriend.friendImg!!,
                            direction
                        )
                        UserManager.touchNumber++

                    } else {
                        Log.i(TAG, "friends is null")
                    }

                    if (manager.let { it!!.topPosition == OtherInfoList.size }){
                        loadingAvatar()
                    }


                }


                override fun onCardRewound() {

                }

                override fun onCardCanceled() {

                }

                override fun onCardAppeared(view: View?, position: Int) {

                }

                override fun onCardDisappeared(view: View?, position: Int) {

                }

            })

            manager!!.setVisibleCount(3)
            manager!!.setTranslationInterval(0.6f)
            manager!!.setScaleInterval(0.8f)
            manager!!.setMaxDegree(20.0f)
            manager!!.setDirections(Direction.HORIZONTAL)
        }

        init()



        viewModel?.firebaseDisconnect?.observe(viewLifecycleOwner) {
            if (it == true) {
                val hint = "加載好友失敗，請檢查你的網路連線狀況"
                lifecycleScope.launch {
                    shadowOnFragment(binding, hint)
                }

            }
        }


        viewModel?.noFriends?.observe(viewLifecycleOwner) {
            if (it == true) {
                val hint = "目前沒有跟你類似的朋友了!"
                lifecycleScope.launch {
                    loadingAvatar()
                    shadowOnFragment(binding, hint)
                }

            }

        }

        fun swipeAnimation(direction: Direction) {
            var scrollX: Float
            var angle: Float
            when (direction) {
                Direction.Right -> {
                    scrollX = binding.cardStackview.width.toFloat()
                    angle = 45f
                }

                Direction.Left -> {
                    scrollX = -binding.cardStackview.width.toFloat()
                    angle = -45f
                }

                else -> {
                    scrollX = binding.cardStackview.width.toFloat()
                    angle = 45f
                }
            }
            val animator = ObjectAnimator.ofFloat(
                binding.cardStackview,
                View.TRANSLATION_X,
                scrollX
            )
            animator.duration = 1000

            val rotationAnimator = ObjectAnimator.ofFloat(
                binding.cardStackview,
                View.ROTATION,
                angle
            )
            val alphaAnimator = ObjectAnimator.ofFloat(
                binding.cardStackview,
                View.ALPHA,
                0f
            )

            val animationSet = AnimatorSet()
            animationSet.playTogether(animator, rotationAnimator, alphaAnimator)
            animationSet.duration = 1000


            animationSet.start()

            Thread(
                Runnable {
                    while (true) {
                        if (!animationSet.isRunning) {

                            binding.cardStackview.post {

                                binding.cardStackview.alpha = 1.0f
                                binding.cardStackview.rotation = 0f
                                binding.cardStackview.translationX = 0f

                                binding.cardStackview.scrollToPosition(binding.cardStackview.adapter!!.itemCount - 1)


                            }
                            break
                        }
                    }
                }
            ).start()


        }
        var currentProfileList = emptyList<UserProfile>()

        viewModel?.otherProfileList?.observe(viewLifecycleOwner) { userProfileList ->
            currentProfileList = userProfileList

            if (userProfileList.isNotEmpty()) {
                binding.selfWrapperImg.visibility = View.GONE
                binding.selfAvatarLoadingContainer.visibility = View.GONE
                binding.selfAvatarLoadingImg.visibility = View.GONE
                binding.loadingText.visibility = View.GONE
            }else{
                binding.selfWrapperImg.visibility = View.VISIBLE
                binding.selfAvatarLoadingContainer.visibility = View.VISIBLE
                binding.selfAvatarLoadingImg.visibility = View.VISIBLE
                binding.loadingText.visibility = View.VISIBLE
                loadingAvatar()
            }

            Log.i(TAG, "profile part is triggered")
            Log.i(TAG, "list into adapter is : $currentProfileList")
            OtherUserNumber = 0
            OtherInfoList.clear()
            var currentFriendList  = emptyList<FriendInfo>().toMutableList()
            userProfileList.forEach { userProfile ->

                val currentFriend =
                    FriendInfo(userProfile.userId, userProfile.userName, userProfile.imageUri)
                currentFriendList.add(currentFriend)

            }
            OtherInfoList = currentFriendList
            OtherUserNumber = OtherInfoList.size

            Log.i(TAG, "all friendnumber is ${OtherUserNumber.toString()}")
            Log.i(TAG, "all friendInfoList is ${OtherInfoList.toString()}")
            val clickListener = HomeAdapter.OnClickListener(
                dislistener = { position ->
                    Log.i(TAG, "dislike btn is clicked")
                    val currentProfile = userProfileList[position]
                    viewModel?.findRelationship(
                        currentProfile.userId!!,
                        currentProfile.userName!!,
                        currentProfile.imageUri!!,
                        Direction.Left
                    )

                    swipeAnimation(Direction.Left)

                },
                likeListener = { position ->
                    Log.i(TAG, "like btn is clicked")
                    val currentProfile = userProfileList[position]
                    viewModel?.findRelationship(
                        currentProfile.userId!!,
                        currentProfile.userName!!,
                        currentProfile.imageUri!!,
                        Direction.Right
                    )
                    swipeAnimation(Direction.Right)
                }
            )
            val adapter = this.context?.let { context ->
                HomeAdapter(
                    context,
                    currentProfileList,
                    clickListener
                )
            }
//            userProfileList.shuffle()

            binding.cardStackview.layoutManager = manager!!
            binding.cardStackview.itemAnimator = DefaultItemAnimator()
            binding.cardStackview.adapter = adapter


        }

        viewModel?.matchToChatRoom?.observe(viewLifecycleOwner) {
            Log.i(TAG, it.toString())
            if (it != null) {
                Log.i(TAG, "like each other, go to match")
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMatchFragment(it)
                )
                viewModel?.navigateToMatch()
            }

        }





        return binding.root
    }


    private suspend fun shadowOnFragment(binding: FragmentHomeBinding, hint: String) {


        delay(5500).let {
            binding.loadingText.text = hint
            binding.loadingText.setTextColor(resources.getColor(R.color.myPrimary))
            binding.loadingText.setTypeface(null, android.graphics.Typeface.BOLD)
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "fragment is dead")

    }
}