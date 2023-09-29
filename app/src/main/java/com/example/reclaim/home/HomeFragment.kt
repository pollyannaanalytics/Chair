package com.example.reclaim.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.databinding.FragmentHomeBinding
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction


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


    lateinit var viewModel: HomeViewModel
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


        fun init() {
            var counter = 0
            manager = CardStackLayoutManager(requireContext(), object : CardStackListener {
                override fun onCardDragging(direction: Direction?, ratio: Float) {

                }

                override fun onCardSwiped(direction: Direction?) {
                    if (counter < OtherUserNumber!!) {
                        val currentFriend = OtherInfoList.get(counter)
                        if (direction != null) {
                            Log.i(TAG, "current user is ${currentFriend.friendName}")
                            viewModel.findRelationship(
                                currentFriend.friendId!!,
                                currentFriend.friendName!!,
                                currentFriend.friendImg!!,
                                direction
                            )
                        } else {
                            Log.i(TAG, "friends is null")
                        }
                        counter++

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

        viewModel.firebaseDisconnect.observe(viewLifecycleOwner) {
            if (it == true) {
                val hint = "加載好友失敗，請檢查你的網路連線狀況"
                shadowOnFragment(binding, hint)
            }
        }

        viewModel.noFriends.observe(viewLifecycleOwner) {
            if (it == true) {
                val hint = "目前沒有跟你類似的朋友了!"
                shadowOnFragment(binding, hint)
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
                                binding.cardStackview.scrollToPosition(binding.cardStackview.adapter!!.itemCount - 1)
                                binding.cardStackview.alpha = 1.0f
                                binding.cardStackview.rotation = 0f
                                binding.cardStackview.translationX = 0f

                            }
                            break
                        }
                    }
                }
            ).start()

        }

        viewModel.otherProfileList.observe(viewLifecycleOwner) { userProfileList ->
            userProfileList.forEach { userProfile ->
                val currentFriend =
                    FriendInfo(userProfile.userId, userProfile.userName, userProfile.imageUri)
                OtherInfoList?.add(currentFriend)
                Log.i(TAG, "all friendInfoList is ${OtherInfoList.toString()}")
            }
            OtherUserNumber = OtherInfoList.size
            Log.i(TAG, "all friendnumber is ${OtherUserNumber.toString()}")
            val clickListener = HomeAdapter.OnClickListener(
                dislistener = { position ->
                    Log.i(TAG, "dislike btn is clicked")
                    val currentProfile = userProfileList[position]
                    viewModel.findRelationship(
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
                    viewModel.findRelationship(
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
                    userProfileList,
                    clickListener
                )
            }
            userProfileList.shuffle()

            binding.cardStackview.layoutManager = manager!!
            binding.cardStackview.itemAnimator = DefaultItemAnimator()
            binding.cardStackview.adapter = adapter


        }

        viewModel.matchToChatRoom.observe(viewLifecycleOwner) {
            Log.i(TAG, it.toString())
            if(it != null){
                Log.i(TAG, "like each other, go to match")
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMatchFragment(it)
                )
                viewModel.navigateToMatch()
            }

        }




        return binding.root
    }


    private fun shadowOnFragment(binding: FragmentHomeBinding, hint: String) {
        val shadowDrawable = ShapeDrawable()
        val rectDrawable = RectShape()

        shadowDrawable.shape = rectDrawable
        shadowDrawable.paint.color = Color.parseColor("#3A3A3A")
        shadowDrawable.paint.alpha = 100

        binding.homeHint.text = hint
        binding.homeLayout.background = shadowDrawable


    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeProfileListener()
    }
}