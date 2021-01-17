package com.example.finalapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.items.AddDiaryMoodItem
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.adapters.CommonAdapter
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.MOOD
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.TIME
import com.example.finalapplication.utils.Global.addDiaryMoodActivity
import com.example.finalapplication.utils.Global.isFromAddDiary
import org.json.JSONObject
import java.util.*

class AddDiaryMoodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_diary_mood)

        Log.d("addDiaryMood", "AddDiaryMoodActivity!!!!!!")

        //要傳出去的東西
        var time: String
        var moodArr: IntArray? = null

        //把傳過來的TOKEN撈出來
        val sharedPreferences = getSharedPreferences(DATA, MODE_PRIVATE)//好像是特殊用法
        val moodAndEvent = sharedPreferences.getString(MOOD_AND_EVENT, "")
        val moodAndEventJson = JSONObject(moodAndEvent!!)

        //設定日記日期與時間的按鈕
        val btnDiaryDate = findViewById<Button>(R.id.btnDiaryDate)
        val btnDiaryTime = findViewById<Button>(R.id.btnDiaryTime)

        //一開始先取得當前日期與時間，之後可透過dataPickerDialog與timePickerDialog更新日記的設定時間，傳送時應該同時傳當前時間的Long與設定時間
        val calendar =
            Calendar.getInstance()//1970年1月1日開始計算到目前為止的格林威治標準時間的milliseconds，所以不管在哪一個時區所儲存的時間都是一樣的
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        var dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        var hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        //設定日期與時間按鈕的顯示文字
        btnDiaryDate.text = "$year" + "年$month" + "月$dayOfMonth" + "日"
        //時間格式調整
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        btnDiaryTime.text = timeFormat.format(calendar.time)
//        btnDiaryTime.text = "$hourOfDay:$minute"
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year1, month1, dayOfMonth1 ->
                year = year1
                month = month1 + 1
                dayOfMonth = dayOfMonth1
                btnDiaryDate.text = "$year" + "年$month" + "月$dayOfMonth" + "日"
            },
            year,
            month,
            dayOfMonth
        )

        datePickerDialog.datePicker.maxDate = Date().time//不能寫未來日記
        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker?, hourOfDay1: Int, minute1: Int ->
                hourOfDay = hourOfDay1
                minute = minute1
                //時間格式調整
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeInside = SimpleDateFormat("HH:mm", Locale.getDefault())
                btnDiaryTime.text = timeInside.format(calendar.time)
            },
            hourOfDay,
            minute,
            true
        )
        btnDiaryDate.setOnClickListener { datePickerDialog.show() }
        btnDiaryTime.setOnClickListener { timePickerDialog.show() }

        //確定心情、前往更改心情的按鍵
        val fabToAct = findViewById<ImageView>(R.id.fabToAct)
        val tvNextToAddActivity = findViewById<TextView>(R.id.tvNextToAddActivity)
        val fabToEditMood = findViewById<ImageView>(R.id.fabToEditMood)
        //如果都沒選心情就不顯示繼續按鈕
        fabToAct.visibility = INVISIBLE
        tvNextToAddActivity.visibility = INVISIBLE
        //將時間字串與心情分數陣列傳到選擇活動頁面
        fabToAct.setOnClickListener {
            val intent = Intent(this, AddDiaryActActivity::class.java)
            time = ""
            time += "$year-$month-$dayOfMonth $hourOfDay:$minute"
            Log.d("addDiaryMood", time)

            val bundle = Bundle()
            bundle.putString(TIME, time)
            bundle.putIntArray(MOOD, moodArr)
            intent.putExtras(bundle)

            //Global中的addDiaryActivity等於這個activity
            addDiaryMoodActivity = this
            startActivity(intent)
        }

        //前往更改心情頁面，因為心情有可能被更改，所以後續要防呆，先在這裡標示
        fabToEditMood.setOnClickListener {
            val intent = Intent(this, EditMoodActivity::class.java)
            addDiaryMoodActivity = this
            isFromAddDiary = true
            startActivity(intent)
        }

        //心情的recyclerView
        val rvMood = findViewById<RecyclerView>(R.id.rvMood)
        //設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = GridLayoutManager(this, 3)
        //設定layoutManager進去rv讓他們有關聯
        rvMood.layoutManager = layoutManager
        //recyclerView設定區
        val adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.add_diary_mood_item, it, false)
                //要綁資料的話宣告在這裡
                val ivMood = view.findViewById<ImageView>(R.id.ivMood)
                val tvMoodName = view.findViewById<TextView>(R.id.tvMoodName)
                val tvMoodScore = view.findViewById<TextView>(R.id.tvMoodScore)

                //選心情分數的AlertDialog
                val alertDialogContentView = LayoutInflater.from(this)
                    .inflate(R.layout.alertdialog_choose_mood_score, null)
                val npChooseMood =
                    alertDialogContentView.findViewById<NumberPicker>(R.id.npChooseMood)
                npChooseMood.maxValue = 5
                npChooseMood.minValue = 1
                npChooseMood.value = 5
                val btnChooseMoodCancel =
                    alertDialogContentView.findViewById<Button>(R.id.btnChooseMoodCancel)
                val btnChooseMoodConfirm =
                    alertDialogContentView.findViewById<Button>(R.id.btnChooseMoodConfirm)
                val alertDialogChooseMoodScore = AlertDialog.Builder(this)
                    .setView(alertDialogContentView)
                    .setCancelable(false)//避免別人填東西過程中被突然取消
                    .setTitle(getString(R.string.alertdialog_choose_mood_score_title))
                    .create()//show創建出來同時顯示()；create是創建出來不顯示，可以不用每次點擊都創建，效能消耗有差

                return@Factory object : BaseViewHolder<AddDiaryMoodItem>(view) {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    override fun bind(item: AddDiaryMoodItem) {
                        //Icon配對
                        Global.iconPairing(ivMood, item.imageResource!!)

                        tvMoodName.text = item.moodName
                        view.background = resources.getDrawable(R.drawable.radius_all)
                        //整個item被點擊
                        view.setOnClickListener {
                            alertDialogChooseMoodScore.show()
                        }

                        //選心情分數的AlertDialog取消鍵被點擊
                        btnChooseMoodCancel.setOnClickListener {
                            //取消時心情分數設成0
                            moodArr?.set(adapterPosition, 0)
                            tvMoodScore.text = ""
                            //心情分數加總==0就隱藏繼續按鈕
                            var moodScoreCount = 0
                            for (i in 0 until (moodArr?.size!!)) {
                                moodScoreCount += moodArr!![i]
                            }
                            if (moodScoreCount == 0) {
                                fabToAct.visibility = INVISIBLE
                                tvNextToAddActivity.visibility = INVISIBLE
                            }
                            view.background = resources.getDrawable(R.drawable.radius_all)
                            alertDialogChooseMoodScore.dismiss()
                        }
                        //選心情分數的AlertDialog確定鍵被點擊
                        btnChooseMoodConfirm.setOnClickListener {
                            //改變心情分數陣列中特定位置的值
                            moodArr?.set(adapterPosition, npChooseMood.value)
                            tvMoodScore.text = "" + npChooseMood.value + "分"
                            //顯示繼續按鈕
                            fabToAct.visibility = VISIBLE
                            tvNextToAddActivity.visibility = VISIBLE
                            view.background =
                                resources.getDrawable(R.drawable.radius_choosed)

                            alertDialogChooseMoodScore.dismiss()
                        }
                    }
                }

            }, type = AddDiaryMoodItem.TYPE).build()
        rvMood.adapter = adapter

        adapter.clear()//開始讀之前先清空

        //將資料填入adapter
        val moodJSONObject = moodAndEventJson.getJSONObject("mood")
        moodArr = IntArray(moodJSONObject.length())
        for (key in moodJSONObject.keys()) {
            val imageResource = moodJSONObject.getInt(key)
            val item = AddDiaryMoodItem(imageResource.toString(), key)
            adapter.add(item)
        }
        for (i in 0 until moodJSONObject.length()) {
            moodArr[i] = 0
        }
    }

    //按下返回鍵
    override fun onBackPressed() {
        val intent = Intent(this, BottomNavigationActivity::class.java)
        startActivity(intent)
        finish()
    }
}