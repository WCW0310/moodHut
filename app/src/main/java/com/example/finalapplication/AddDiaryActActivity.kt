package com.example.finalapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.items.AddDiaryActItem
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.adapters.CommonAdapter
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.MOOD
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.TIME
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.Global.addDiaryActActivity
import com.example.finalapplication.utils.Global.addDiaryMood
import com.example.finalapplication.utils.Global.addDiaryMoodActivity
import com.example.finalapplication.utils.Global.addDiaryTime
import com.example.finalapplication.utils.Global.iconPairing
import com.example.finalapplication.utils.Global.isFromAddDiary
import com.example.finalapplication.utils.NetworkController
import org.json.JSONObject

class AddDiaryActActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_diary_act)
        //關掉自動彈出虛擬鍵盤
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        Log.d("addDiaryAct", "AddDiaryActActivity!!!!!!")

        //要傳出去的東西
        //把傳過來的TOKEN撈出來
        val sharedPreferences = getSharedPreferences(DATA, MODE_PRIVATE)//好像是特殊用法
        val token = sharedPreferences.getString(TOKEN, "")
        val moodAndEvent = sharedPreferences.getString(MOOD_AND_EVENT, "")
        val moodAndEventJson = JSONObject(moodAndEvent!!)
        //把傳過來的時間字串與心情陣列撈出來
        val time = intent.extras?.getString(TIME)
        val mood = intent.extras?.getIntArray(MOOD)
        //日記備註
        val etDiaryNote = findViewById<EditText>(R.id.etDiaryNote)
        //活動雙層陣列
        var eventArray: Array<IntArray?>? = null
        //確定日記、前往更改活動的按鍵
        val fabBuildDiary = findViewById<ImageView>(R.id.fabBuildDiary)
        val fabToEditAct = findViewById<ImageView>(R.id.fabToEditAct)

        //防止重複送日記的boolean
        var lockFabBuildDiary = false

        //按下按鈕，建立日記，返回日記本頁面
        fabBuildDiary.setOnClickListener {
            // 防止重複新增日記
            if (lockFabBuildDiary) {
                return@setOnClickListener
            }

            lockFabBuildDiary = true

            //建立日記API
            NetworkController
                .addDiary(
                    token!!,
                    time!!,
                    mood!!,
                    eventArray,
                    etDiaryNote.text.toString()
                )
                .onResponse {
                    //印出收到的東西
                    Log.d("addDiaryAct", "addDiaryResponse: $it")
                    runOnUiThread {
                        //可以丟Toast訊息告知日記建立完成
                        Toast.makeText(
                            applicationContext,
                            "日記新增成功",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, BottomNavigationActivity::class.java)
                        startActivity(intent)
                        //讓上一頁的addDiaryActivity結束
                        addDiaryMoodActivity!!.finish()
                        //再次手動回歸null
                        addDiaryMoodActivity = null
                        finish()
                    }
                }.onFailure {}.onComplete {}.exec()
        }


        //前往更改活動頁面，因為活動有可能被更改，所以後續要防呆，先在這裡標示
        fabToEditAct.setOnClickListener {
            val intent = Intent(this, EditActTypeActivity::class.java)
            addDiaryActActivity = this
            isFromAddDiary = true
            addDiaryTime = time
            addDiaryMood = mood
            startActivity(intent)
        }

        //活動的recyclerView
        val rvAct = findViewById<RecyclerView>(R.id.rvAct)
        //設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = LinearLayoutManager(this)
        //設定layoutManager進去rv讓他們有關聯
        rvAct.layoutManager = layoutManager
        //recyclerView設定區
        val adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.add_diary_act_item, it, false)
                //要綁資料的話宣告在這裡
                val tvActFolder = view.findViewById<TextView>(R.id.tvActFolder)
                //llAct中放動態生成的活動物件
                val llAct = view.findViewById<LinearLayout>(R.id.llAct)

                return@Factory object : BaseViewHolder<AddDiaryActItem>(view) {
                    @SuppressLint("UseCompatLoadingForDrawables")
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
                            iconPairing(ivAct, item.imageResourceArray[i]!!)

                            //點擊事件
                            view.background = resources.getDrawable(R.drawable.radius_all)
                            view.setOnClickListener {
                                if (eventArray?.get(adapterPosition)?.get(i) == 0) {
                                    view.background =
                                        resources.getDrawable(R.drawable.radius_choosed)
                                    eventArray?.get(adapterPosition)?.set(i, 1)
                                    Log.d(
                                        "addDiaryAct",
                                        eventArray?.get(adapterPosition)?.get(i).toString()
                                    )
                                } else {
                                    view.background = resources.getDrawable(R.drawable.radius_all)
                                    eventArray?.get(adapterPosition)?.set(i, 0)
                                    Log.d(
                                        "addDiaryAct",
                                        eventArray?.get(adapterPosition)?.get(i).toString()
                                    )
                                }
                            }
                            llAct.addView(view)
                        }
                    }
                }
            }, type = AddDiaryActItem.TYPE).build()
        rvAct.adapter = adapter
        adapter.clear()//開始讀之前先清空

        //將資料填入adapter
        val eventJSONObject = moodAndEventJson.getJSONObject("event")
        //取得所有資料夾(分類)名稱
        val keys = eventJSONObject.keys()
        eventArray = Array(eventJSONObject.length()) { i -> IntArray(i) }

        //活動類別陣列紀錄器
        var countForKeyPosition = 0
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
            adapter.add(item)

            //設定活動陣列每格的大小
            eventArray?.set(countForKeyPosition, IntArray(item.actArray.size))

            countForKeyPosition++
        }


    }
}


