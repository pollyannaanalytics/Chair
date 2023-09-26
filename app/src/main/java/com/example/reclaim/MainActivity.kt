package com.example.reclaim

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.myNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navBottomView: BottomNavigationView = binding.bottomNavigation
        navBottomView.setupWithNavController(navController)


        navController.addOnDestinationChangedListener{ _, destination, _ ->
            when(destination.id){
                R.id.homeFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbarTitle.setText("Reclaim")
                }
                R.id.profileFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbarTitle.setText("我的個人檔案")
                }
                R.id.chatListFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbarTitle.setText("我的好友")
                }
                R.id.chatRoomFragment -> {
                    binding.toolbar.visibility = View.GONE
                }

            }
        }

    }
}