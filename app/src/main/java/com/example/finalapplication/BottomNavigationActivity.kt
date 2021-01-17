package com.example.finalapplication

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.adapters.MainPageAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.system.exitProcess


class BottomNavigationActivity : AppCompatActivity() {
    lateinit var viewPagerMainPage: ViewPager2;
    lateinit var mainPageAdapter: MainPageAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation)
        Log.d("yamiew", "BottomNavigationactivity onCreate 執行了")
        Log.d("bottomNavigation", "BottomNavigationActivity!!!!!!")

        //navigationView與viewPager2元件
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        viewPagerMainPage = findViewById(R.id.ViewPagerMainPage)

        //viewPager2設置
        mainPageAdapter = MainPageAdapter(supportFragmentManager, lifecycle)
        viewPagerMainPage.adapter = mainPageAdapter;
        supportFragmentManager.fragments.clear();
        //viewPager2滑動時要讓navigation按鈕一起移動
        viewPagerMainPage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigationView.menu.getItem(position).isChecked = true;
            }
        })

        //判斷是否從聊天室跳轉過來
        if (Global.toChatList) {
            Global.toChatList = false
            viewPagerMainPage.setCurrentItem(3, false)
        }

        //navigation點擊時要讓viewPager2一起移動
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.page_diary_book -> {
                    viewPagerMainPage.setCurrentItem(0, false)
                    true
                }
                R.id.page_statistic -> {
                    viewPagerMainPage.setCurrentItem(1, false)
                    true
                }
                R.id.page_square -> {
                    viewPagerMainPage.setCurrentItem(2, false)
                    true
                }
                R.id.page_chat_list -> {
                    //smoothScroll要設成false，不然首次切換到聊天室會出錯
                    viewPagerMainPage.setCurrentItem(3, false)
//                    mainPageAdapter.refreshChatList();
                    true
                }
                R.id.page_more -> {
                    viewPagerMainPage.setCurrentItem(4, false)
                    true
                }
                else -> false
            }
        }
    }

    //按下返回鍵跳出AlertDialog提示再按一次結束程式
    override fun onBackPressed() {
        val ad: AlertDialog.Builder = AlertDialog.Builder(this)
        ad.setTitle("離開")
        ad.setMessage("確定要離開此應用程式嗎?")
        ad.setPositiveButton("是") { dialog, i ->

            //退出按鈕
            // TODO Auto-generated method stub
            this.finish() //關閉activity
            exitProcess(0)//結束程式
        }
        ad.setNegativeButton("否") { dialog, i ->
            //不退出不用執行任何操作
        }
        ad.show() //顯示對話框
    }
}