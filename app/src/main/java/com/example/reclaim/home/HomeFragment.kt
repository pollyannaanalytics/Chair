package com.example.reclaim.home

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
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
    var friendName: String? = null
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
            manager = CardStackLayoutManager(requireContext(), object : CardStackListener {
                override fun onCardDragging(direction: Direction?, ratio: Float) {

                }

                override fun onCardSwiped(direction: Direction?) {
                    if (OtherUserNumber != null) {

                            val currentFriend = OtherInfoList.get(manager!!.topPosition - 1 )
                            if (direction != null) {
                                Log.i(TAG, "current user is ${currentFriend.friendName}")
                                viewModel.findRelationship(
                                    currentFriend.friendId!!,
                                    currentFriend.friendName!!,
                                    direction
                                )
                            } else {
                                Log.i(TAG, "friends is null")
                            }

                    } else {
                        Log.i(TAG, "friend is null")
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

        viewModel.otherProfileList.observe(viewLifecycleOwner) {

            it.forEach { it ->
                val currentFriend = FriendInfo(it.userId, it.userName)
                OtherInfoList?.add(currentFriend)
                Log.i(TAG, "all friendInfoList is ${OtherInfoList.toString()}")
            }
            OtherUserNumber = OtherInfoList.size
            Log.i(TAG, "all friendnumber is ${OtherUserNumber.toString()}")
            val adapter = this.context?.let { it1 -> HomeAdapter(it1, it) }
            it.shuffle()

            binding.cardStackview.layoutManager = manager!!
            binding.cardStackview.itemAnimator = DefaultItemAnimator()
            binding.cardStackview.adapter = adapter


        }

//        binding.videoButton.setOnClickListener {
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMeetingFragment())
//        }




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