package com.example.testfirebase.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.testfirebase.R
import com.example.testfirebase.ui.data.DataStore
import com.example.testfirebase.ui.fragment.FirebaseFragment
import com.example.testfirebase.ui.fragment.MacFragment
import com.example.testfirebase.ui.fragment.ViewPagerAdapter
import com.example.testfirebase.ui.utils.ConnectionType
import com.example.testfirebase.ui.utils.NetworkMonitorUtil
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.FirebaseDatabase

class ListDeviceActivity : AppCompatActivity() {
    lateinit var viewPager : ViewPager
    lateinit var tabs: TabLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_device)
        viewPager = findViewById(R.id.viewpager)
        tabs = findViewById(R.id.tabs)
        setUpTabs()
    }
    fun setUpTabs(){
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(FirebaseFragment(),"ONLINE")
        adapter.addFragment(MacFragment(),"OFFLINE")
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
    }
    override fun onResume() {
        super.onResume()
        Log.d("TAG","network resume")
    }

    override fun onStop() {
        super.onStop()
//        networkMonitor.unregister()
    }
}