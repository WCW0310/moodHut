package com.example.finalapplication.utils.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.finalapplication.fragments.*

class MainPageAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {//把fragment當recycleView使用，所可以滑動切換


    private val fragments = ArrayList<Fragment>();
    private val fM = fragmentManager;

    //init
    init {
        fragments.add(DiaryBookFragment())
        fragments.add(StatisticFragment())
        fragments.add(SquareFragment())
        fragments.add(ChatListFragment())
        fragments.add(MoreFragment())
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        Log.d("yamiew1", position.toString())
        return fragments[position]
    }

}