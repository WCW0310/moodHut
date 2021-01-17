package com.example.finalapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.items.AddDiaryActItem
import com.example.finalapplication.items.AddDiaryMoodItem
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.adapters.CommonAdapter
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.NetworkController
import org.json.JSONObject
import java.util.*
import kotlin.system.exitProcess

class UpdateDiaryActivity : AppCompatActivity() {
    companion object Instance {
        private const val DIARY_ID = "DIARY_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_diary)
        //關掉自動彈出虛擬鍵盤
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        Log.d("newMood", "NewMoodActivity!!!!!!")

        //要傳出去的東西、要使用的東西

        //把傳過來的TOKEN撈出來
        val diaryId = intent.extras?.getString(DIARY_ID)

        val sharedPreferences = getSharedPreferences(DATA, MODE_PRIVATE)//好像是特殊用法
        val moodAndEvent = sharedPreferences?.getString(MOOD_AND_EVENT, "")
        val moodAndEventJson = JSONObject(moodAndEvent!!)
        //心情圖示配對
        val moodPathMap: MutableMap<String, String> = mutableMapOf()

        //活動圖示配對
        val actPathMap: MutableMap<String, String> = mutableMapOf()

        //填入活動心情Icon鍵值對
        val moodJSONObject = moodAndEventJson.getJSONObject("mood")
        val eventJSONObject = moodAndEventJson.getJSONObject("event")
        val moodNames: MutableList<String> = mutableListOf()
        val eventNames: MutableList<String> = mutableListOf()
        //心情圖片數量的Array
        val moodIconPathArray: MutableList<String> = mutableListOf()
        //活動圖片數量的Array
        val actIconPathArray: MutableList<String> = mutableListOf()

        //順便填入心情名List
        for (key in moodJSONObject.keys()) {
            moodPathMap[key] = moodJSONObject.getInt(key).toString()
            moodNames.add(key)
        }
        //順便填入活動名List
        for (key in eventJSONObject.keys()) {
            for (key2 in eventJSONObject.getJSONObject(key).keys()) {
                actPathMap[key2] = eventJSONObject.getJSONObject(key).getInt(key2).toString()
                eventNames.add(key2)
            }
        }
        actPathMap["null"] = "0"
        moodPathMap["null"] = "0"

        //填入心情圖片數量的Array，數量寫死
        for (i in 1 until 5) {
            moodIconPathArray.add("$i")
        }
        //填入心情圖片數量的Array，數量寫死
        for (i in 100 until 112) {
            actIconPathArray.add("$i")
        }

        //宣告元件
        //設定日記日期與時間的按鈕
        val btnUpdateDiaryDate = findViewById<Button>(R.id.btnUpdateDiaryDate)
        val btnUpdateDiaryTime = findViewById<Button>(R.id.btnUpdateDiaryTime)

        val rvUpdateDiaryMood = findViewById<RecyclerView>(R.id.rvUpdateDiaryMood)
        val rvUpdateDiaryAct = findViewById<RecyclerView>(R.id.rvUpdateDiaryAct)
        val fabUpdateDiaryComplete = findViewById<ImageView>(R.id.fabUpdateDiaryComplete)
        val etUpdateDiaryNote = findViewById<EditText>(R.id.etUpdateDiaryNote)

        //先丟出API取得該篇日記內容陣列
        //屬性列這裡

        var recordDate: String = ""//"2020-12-02 00:22"
        var mood = IntArray(moodNames.size)//[5,1,1,1]
        var event: Array<IntArray?>? =
            Array(eventJSONObject.length()) { i -> IntArray(i) }//[[1,1,1],[1,1,1],[1,1,1],[1,1,1]]
        var content: String = ""//"第一篇"

        //設成該篇日記的時間
        //一開始先取得當前日期與時間，之後可透過dataPickerDialog與timePickerDialog更新日記的設定時間，傳送時應該同時傳當前時間的Long與設定時間
        val calendar =
            Calendar.getInstance()//1970年1月1日開始計算到目前為止的格林威治標準時間的milliseconds，所以不管在哪一個時區所儲存的時間都是一樣的
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        var dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        var hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year1, month1, dayOfMonth1 ->
                year = year1
                month = month1 + 1
                dayOfMonth = dayOfMonth1
                btnUpdateDiaryDate.text = "$year" + "年$month" + "月$dayOfMonth" + "日"
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
                btnUpdateDiaryTime.text = timeInside.format(calendar.time)
            },
            hourOfDay,
            minute,
            true
        )
        btnUpdateDiaryDate.setOnClickListener { datePickerDialog.show() }
        btnUpdateDiaryTime.setOnClickListener { timePickerDialog.show() }

//完成更改日記的按鍵
        //將時間字串與心情分數陣列傳到選擇活動頁面
        fabUpdateDiaryComplete.setOnClickListener {

            //心情分數加總==0就隱藏繼續按鈕
            var moodScoreCount = 0
            for (i in 0 until (mood?.size!!)) {
                moodScoreCount += mood!![i]
            }
            if (moodScoreCount == 0) {
                Toast.makeText(this.applicationContext, "至少要選一個心情!", Toast.LENGTH_SHORT).show()
            } else {
                recordDate = ""
                recordDate += "$year-$month-$dayOfMonth $hourOfDay:$minute"

                NetworkController
                    .modifyDiaryUpdate(
                        diaryId!!.toInt(),
                        recordDate,
                        mood,
                        event,
                        etUpdateDiaryNote.text.toString()
                    )
                    .onResponse {
                        //印出收到的東西
                        Log.d("updateDiary", "modifyDiaryGetDiaryResponse: $it")
                        runOnUiThread {
                            Toast.makeText(this.applicationContext, "日記更改成功", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this, BottomNavigationActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }.onFailure {}.onComplete {}.exec()
            }
        }

        //設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = GridLayoutManager(this, 2)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        //設定layoutManager進去rv讓他們有關聯
        rvUpdateDiaryMood.layoutManager = layoutManager
        //recyclerView設定區
        val adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.update_diary_mood_item, it, false)
                //要綁資料的話宣告在這裡
                val ivMood = view.findViewById<ImageView>(R.id.ivUpdateDiaryMood)
                val tvMoodName = view.findViewById<TextView>(R.id.tvUpdateDiaryMoodName)
                val tvMoodScore = view.findViewById<TextView>(R.id.tvUpdateDiaryMoodScore)

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
                    override fun bind(item: AddDiaryMoodItem) {
                        //Icon配對
                        Global.iconPairing(ivMood, item.imageResource!!)

                        tvMoodName.text = item.moodName
                        view.background = resources.getDrawable(R.drawable.radius_all)
                        if (mood[adapterPosition] > 0) {
                            tvMoodScore.text = "" + mood[adapterPosition] + "分"
                            view.background =
                                resources.getDrawable(R.drawable.radius_choosed)
                        }


                        //整個item被點擊
                        view.setOnClickListener {
                            alertDialogChooseMoodScore.show()
                        }

                        //選心情分數的AlertDialog取消鍵被點擊
                        btnChooseMoodCancel.setOnClickListener {
                            //取消時心情分數設成0
                            mood?.set(adapterPosition, 0)
                            tvMoodScore.text = ""

                            view.background = resources.getDrawable(R.drawable.radius_all)
                            alertDialogChooseMoodScore.dismiss()
                        }
                        //選心情分數的AlertDialog確定鍵被點擊
                        btnChooseMoodConfirm.setOnClickListener {
                            //改變心情分數陣列中特定位置的值
                            mood?.set(adapterPosition, npChooseMood.value)
                            tvMoodScore.text = "" + npChooseMood.value + "分"

                            view.background =
                                resources.getDrawable(R.drawable.radius_choosed)
                            alertDialogChooseMoodScore.dismiss()
                        }
                    }
                }

            }, type = AddDiaryMoodItem.TYPE).build()
        rvUpdateDiaryMood.adapter = adapter

        adapter.clear()//開始讀之前先清空


//設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager2 = LinearLayoutManager(this)
        //設定layoutManager進去rv讓他們有關聯
        rvUpdateDiaryAct.layoutManager = layoutManager2
        //recyclerView設定區
        val adapter2 = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.add_diary_act_item, it, false)
                //要綁資料的話宣告在這裡
                val tvActFolder = view.findViewById<TextView>(R.id.tvActFolder)
                //llAct中放動態生成的活動物件
                val llAct = view.findViewById<LinearLayout>(R.id.llAct)

                return@Factory object : BaseViewHolder<AddDiaryActItem>(view) {
                    override fun bind(item: AddDiaryActItem) {
                        tvActFolder.text = item.actFolderName
                        //想辦法動態生成view!!!!!!!!!!!!

                        for (i in item.actArray.indices) {
                            val view: View = LayoutInflater.from(it?.context)
                                .inflate(R.layout.add_diary_act_item_motivate, it, false)
                            val tvActName = view.findViewById<TextView>(R.id.tvActName)
                            val ivAct = view.findViewById<ImageView>(R.id.ivAct)
                            tvActName.text = item.actArray[i]

                            //Icon配對
                            Global.iconPairing(ivAct, item.imageResourceArray[i]!!)

                            view.background = resources.getDrawable(R.drawable.radius_all)
                            //設定本來日記被選的活動為黃色
                            if (event?.get(adapterPosition)?.get(i)!! > 0) {
                                view.background =
                                    resources.getDrawable(R.drawable.radius_choosed)
                            }

                            //點擊事件
                            view.setOnClickListener {
                                if (event?.get(adapterPosition)?.get(i) == 0) {
                                    view.background =
                                        resources.getDrawable(R.drawable.radius_choosed)
                                    event?.get(adapterPosition)?.set(i, 1)
                                    Log.d(
                                        "addDiaryAct",
                                        event?.get(adapterPosition)?.get(i).toString()
                                    )
                                } else {
                                    view.background = resources.getDrawable(R.drawable.radius_all)
                                    event?.get(adapterPosition)?.set(i, 0)
                                    Log.d(
                                        "addDiaryAct",
                                        event?.get(adapterPosition)?.get(i).toString()
                                    )
                                }
                            }
                            llAct.addView(view)
                        }
                    }
                }
            }, type = AddDiaryActItem.TYPE).build()
        rvUpdateDiaryAct.adapter = adapter2
        adapter2.clear()//開始讀之前先清空


        //取得更新日記API，盡量寫在最後，改變值就好
        NetworkController
            .modifyDiaryGetDiary(diaryId!!.toInt())
            .onResponse {
                //印出收到的東西
                Log.d("updateDiary", "modifyDiaryGetDiaryResponse: $it")
                runOnUiThread {
                    recordDate = it.getString("record_date")
                    for (i in mood.indices) {
                        mood[i] = it.getJSONArray("mood").getInt(i)
                    }
                    for (i in event!!.indices) {
                        //設定活動陣列每格的大小
                        event[i] = IntArray(it.getJSONArray("event").getJSONArray(i).length())
                        for (int in event[i]!!.indices) {
                            event[i]?.set(int, it.getJSONArray("event").getJSONArray(i).getInt(int))
                        }
                    }
                    content = it.getString("content")

                    year = recordDate.split(" ")[0].split("-")[0].toInt()
                    month = recordDate.split(" ")[0].split("-")[1].toInt()
                    dayOfMonth = recordDate.split(" ")[0].split("-")[2].toInt()
                    hourOfDay = recordDate.split(" ")[1].split(":")[0].toInt()
                    minute = recordDate.split(" ")[1].split(":")[1].toInt()

                    //設定日期與時間按鈕的顯示文字
                    btnUpdateDiaryDate.text = "$year" + "年$month" + "月$dayOfMonth" + "日"
                    //時間格式調整
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    btnUpdateDiaryTime.text = timeFormat.format(calendar.time)
//        btnDiaryTime.text = "$hourOfDay:$minute"

                    etUpdateDiaryNote.setText(content)

                    //將資料填入adapter
                    for (key in moodJSONObject.keys()) {
                        val imageResource = moodJSONObject.getInt(key)
                        val item = AddDiaryMoodItem(imageResource.toString(), key)
                        adapter.add(item)
                    }


                    //將資料填入adapter

                    //取得所有資料夾(分類)名稱
                    val keys = eventJSONObject.keys()
                    for (key in keys) {
                        val actArr: Array<String?> =
                            arrayOfNulls(eventJSONObject.getJSONObject(key).length())
                        val imageResourceArray: Array<String?> =
                            arrayOfNulls(eventJSONObject.getJSONObject(key).length())

                        var count = 0
                        for (act in eventJSONObject.getJSONObject(key).keys()) {
                            //活動資料夾內的活動
                            val imageResource =
                                eventJSONObject.getJSONObject(key).getInt(act).toString()
                            Log.d("addDiaryAct", act)

                            actArr[count] = act
                            imageResourceArray[count] = imageResource
                            count++
                        }
                        val item = AddDiaryActItem(key, actArr, imageResourceArray)
                        adapter2.add(item)
                    }
                }
            }.onFailure {}.onComplete {}.exec()
    }

    //按下返回鍵回到主頁
    override fun onBackPressed() {
        val intent = Intent(this, BottomNavigationActivity::class.java)
        startActivity(intent)
        finish()
    }
}