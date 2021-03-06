package com.example.finalapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.MyApp.Companion.DATA
import com.example.finalapplication.MyApp.Companion.EVENT_ICON_NUM
import com.example.finalapplication.MyApp.Companion.EVENT_TYPE_NAME
import com.example.finalapplication.MyApp.Companion.MOOD_AND_EVENT
import com.example.finalapplication.MyApp.Companion.MOOD_ICON_NUM
import com.example.finalapplication.MyApp.Companion.TOKEN
import com.example.finalapplication.MyApp.Companion.editActActivity
import com.example.finalapplication.MyApp.Companion.iconPairing
import com.example.finalapplication.MyApp.Companion.isChangeMoodOrAct
import com.example.finalapplication.MyApp.Companion.isFromAddDiary
import com.example.finalapplication.items.NewMoodRVItem
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.adapters.CommonAdapter
import com.example.finalapplication.utils.NetworkController
import org.json.JSONObject

class NewActActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_act)
        //關掉自動彈出虛擬鍵盤
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        Log.d("newAct", "NewActActivity!!!!!!")

        //要傳出去的東西、要使用的東西
        var iconPath = "1"

        //把傳過來的TOKEN撈出來
        val eventTypeName = intent.extras?.getString(EVENT_TYPE_NAME)
        val sharedPreferences = getSharedPreferences(DATA, MODE_PRIVATE)//好像是特殊用法
        val token = sharedPreferences?.getString(TOKEN, "")
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
        for (i in 1 until MOOD_ICON_NUM) {
            moodIconPathArray.add("$i")
        }
        //填入心情圖片數量的Array，數量寫死
        for (i in 100 until EVENT_ICON_NUM) {
            actIconPathArray.add("$i")
        }

        //宣告元件
        val ivNewAct = findViewById<ImageView>(R.id.ivNewAct)
        val etNewActName = findViewById<EditText>(R.id.etNewActName)
        val rvNewAct = findViewById<RecyclerView>(R.id.rvNewAct)
        val btnNewActConfirm = findViewById<Button>(R.id.btnNewActConfirm)

        //確定新增活動
        btnNewActConfirm.setOnClickListener {
            var actNameRepeat = false
            var actName = etNewActName.text.toString()

            if (actName.length > 10) {
                Toast.makeText(this.applicationContext, "活動名稱字數過長", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            for (i in eventNames) {
                if (actName == i) {
                    actNameRepeat = true
                    break
                }
            }
            if (actNameRepeat) {
                //跳名稱不可重複toast
                Toast.makeText(this.applicationContext, "活動名稱不可重複", Toast.LENGTH_SHORT).show()
            } else {
                //新增活動API
                NetworkController
                    .modifyEventAdd(
                        token!!,
                        eventTypeName!!,
                        actName,
                        iconPath.toInt()
                    )
                    .onResponse {
                        //印出收到的東西
                        Log.d("newMood", "newMoodResponse: $it")
                        runOnUiThread {

                            //收到新的心情與活動陣列，存進sharedPreferences
                            sharedPreferences.edit()
                                .putString(MOOD_AND_EVENT, it.toString())
                                .apply()

                            //可以丟Toast訊息告知活動新增成功
                            Toast.makeText(this.applicationContext, "活動新增成功", Toast.LENGTH_SHORT)
                                .show()

                            val intent = Intent(this, EditActTypeActivity::class.java)
                            //若從新增日記進入
                            if (isFromAddDiary) {
                                //有更改活動發生
                                isChangeMoodOrAct = true
                            }
                            startActivity(intent)
                            //前一頁沒被finish的editActActivity
                            editActActivity!!.finish()
                            finish()
                        }
                    }.onFailure {}.onComplete {}.exec()
            }
        }

        //ivNewMood初始圖片
        iconPairing(ivNewAct, actIconPathArray[0])

        //設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = GridLayoutManager(this, 4)
        //設定layoutManager進去rv讓他們有關聯
        rvNewAct.layoutManager = layoutManager
        //recyclerView設定區
        val adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.new_mood_image_item, it, false)
                //要綁資料的話宣告在這裡
                val ivNewMoodImageItem = view.findViewById<ImageView>(R.id.ivNewMoodImageItem)

                return@Factory object : BaseViewHolder<NewMoodRVItem>(view) {
                    override fun bind(item: NewMoodRVItem) {
                        //設定圖片
                        iconPairing(ivNewMoodImageItem, item.imageResource!!)
                        //點擊事件
                        view.setOnClickListener {
                            iconPairing(ivNewAct, item.imageResource)
                            iconPath = item.imageResource
                        }
                    }
                }
            }, type = NewMoodRVItem.TYPE).build()
        rvNewAct.adapter = adapter
        adapter.clear()//開始讀之前先清空

        //依照我們提供的心情圖數量提供
        for (i in actIconPathArray) {
            val item = NewMoodRVItem(i)
            adapter.add(item)
        }
    }
}