package com.example.reclaim

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.reclaim.databinding.ActivityMainBinding
import com.example.reclaim.databinding.ActivityRtcactivityBinding
import com.example.reclaim.videocall.Constants
import com.example.reclaim.videocall.RTCActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout.TabGravity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewModel = viewModel
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navBottomView: BottomNavigationView = binding.bottomNavigation
        navBottomView.setupWithNavController(navController)

        viewModel.updateOnline(true)

        fun showToolBar(showLogo: Boolean, titleText: String) {
            binding.toolbar.visibility = View.VISIBLE
            binding.bottomNavigation.visibility = View.VISIBLE
            if (showLogo) {
                binding.appName.visibility = View.VISIBLE
                binding.toolbarLogo.visibility = View.VISIBLE
                binding.fragmentTitle.visibility = View.GONE
            } else {
                binding.appName.visibility = View.GONE
                binding.toolbarLogo.visibility = View.GONE
                binding.fragmentTitle.visibility = View.VISIBLE
                binding.fragmentTitle.text = titleText

            }

        }

        viewModel.totalUnreadMessage.observe(this){
            Log.i("mainactiviy", it.toString())
            navBottomView.getOrCreateBadge(R.id.chatListFragment).number = it
        }


        fun hideToolbarAndBottom(){
            binding.toolbar.visibility = View.GONE
            binding.toolbarLogo.visibility = View.GONE
            binding.fragmentTitle.visibility = View.GONE
            binding.bottomNavigation.visibility = View.GONE
        }


        fun hideToolbar(){
            binding.toolbar.visibility = View.GONE
            binding.toolbarLogo.visibility = View.GONE
            binding.fragmentTitle.visibility = View.GONE
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment ->
                    showToolBar(true, "首頁")

                R.id.alreadySignUpProfileFragment -> hideToolbar()
                R.id.chatListFragment -> showToolBar(false, "我的好友")
                R.id.chatRoomFragment -> hideToolbar()

                R.id.profileFragment -> showToolBar(false, "編輯個人檔案")
                R.id.matchFragment -> {
                    hideToolbarAndBottom()

                }

                R.id.agreementFragment -> {
                    hideToolbarAndBottom()
                }

                R.id.loadingFragment -> {
                    hideToolbarAndBottom()
                }


                R.id.loginFragment -> {
                    hideToolbarAndBottom()
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        viewModel.updateOnline(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.OnDestroyed()
        viewModel.updateOnline(false)
    }
}