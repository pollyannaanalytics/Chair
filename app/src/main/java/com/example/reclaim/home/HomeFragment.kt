package com.example.reclaim.home

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.text.Layout
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import com.example.reclaim.data.ReclaimDatabase
import com.example.reclaim.data.UserProfile
import com.example.reclaim.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val TAG = "HOMEFRAGMENT"

class HomeFragment : Fragment() {


    val db = Firebase.firestore


    var manager: CardStackLayoutManager? = null
    var friendNumber: Int? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val application = requireNotNull(this.activity).application
        val databaseDao = ReclaimDatabase.getInstance(application).reclaimDao()
        val homeFactory = HomeFactory(databaseDao)
        val viewModel = ViewModelProvider(this, homeFactory).get(HomeViewModel::class.java)

        val binding = FragmentHomeBinding.inflate(inflater)


        binding.viewModel = viewModel

        fun init() {
            manager = CardStackLayoutManager(requireContext(), object : CardStackListener {
                override fun onCardDragging(direction: Direction?, ratio: Float) {

                }

                override fun onCardSwiped(direction: Direction?) {
                    if (friendNumber != null) {
                        if (manager!!.topPosition == friendNumber) {
                            val hint = "剛剛那是你最後一個相似對象了，下次請好好把握!"
                            shadowOnFragment(binding, hint)
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
            Log.i(TAG, "${it.map { it.toString() }}")

            friendNumber = it.size
            val adapter = this.context?.let { it1 -> HomeAdapter(it1, it) }
            it.shuffle()

            binding.cardStackview.layoutManager = manager!!
            binding.cardStackview.itemAnimator = DefaultItemAnimator()
            binding.cardStackview.adapter = adapter


        }

//        val mockdata = UserProfile(userId = "32323", gender = "女", userName = "我是假的", worriesDescription = "月經來", worryType = "HEALTH", imageUri = "content://media/picker/0/com.android.providers.media.photopicker/media/1000000034")
//        val mockList = MutableList(5){mockdata}
//        val adapter = this.context?.let { context -> HomeAdapter(context, mockList) }
//
//        binding.cardStackview.layoutManager = manager!!
//            binding.cardStackview.itemAnimator = DefaultItemAnimator()
//            binding.cardStackview.adapter = adapter



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


}