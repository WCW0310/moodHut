package com.example.finalapplication

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.items.AddDiaryMoodItem
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.OLD_EVENT_NAME
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.Global.addDiaryMood
import com.example.finalapplication.utils.Global.addDiaryTime
import com.example.finalapplication.utils.adapters.CommonAdapter
import com.example.finalapplication.utils.NetworkController
import org.json.JSONObject

//顯示活動組別
class EditActTypeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_act_type)

        Log.d("editActType", "EditActTypeActivity!!!!!!")

        //要傳出去的東西、要使用的東西
        //把傳過來的TOKEN撈出來
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
        val eventTypeNames: MutableList<String> = mutableListOf()

        //順便填入心情名List
        for (key in moodJSONObject.keys()) {
            moodPathMap[key] = moodJSONObject.getInt(key).toString()
            moodNames.add(key)
        }
        //順便填入活動名List
        for (key in eventJSONObject.keys()) {
            eventTypeNames.add(key)
            for (key2 in eventJSONObject.getJSONObject(key).keys()) {
                actPathMap[key2] = eventJSONObject.getJSONObject(key).getInt(key2).toString()
                eventNames.add(key2)
            }
        }
        actPathMap["null"] = "0"
        moodPathMap["null"] = "0"


        //宣告元件
        val rvEditActType = findViewById<RecyclerView>(R.id.rvEditActType)
        val btnEditActAddActType = findViewById<Button>(R.id.btnEditActTypeAddActType)

        //用dialog時，context要用對，不然程式會死
        val alertDialogContentView =
            LayoutInflater.from(this)
                .inflate(R.layout.alertdialog_add_act_type, null)
        val btnAlertdialogAddActTypeConfirm =
            alertDialogContentView.findViewById<Button>(R.id.btnAlertdialogAddActTypeConfirm)
        val btnAlertdialogAddActTypeCancel =
            alertDialogContentView.findViewById<Button>(R.id.btnAlertdialogAddActTypeCancel)
        val etAddActTypeName =
            alertDialogContentView.findViewById<EditText>(R.id.etAddActTypeName)
        val alertDialogAddActType =
            AlertDialog.Builder(this)
                .setView(alertDialogContentView)
                .setCancelable(false)//避免別人填東西過程中被突然取消
                .create()

        btnAlertdialogAddActTypeCancel.setOnClickListener {
            alertDialogAddActType.dismiss()
        }

        btnAlertdialogAddActTypeConfirm.setOnClickListener {
            var eventTypeNameRepeat = false
            var eventTypeName = etAddActTypeName.text.toString()

            if (eventTypeName.length > 10) {
                Toast.makeText(this.applicationContext, "組別名稱字數過長", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            for (i in eventTypeNames) {
                if (eventTypeName == i) {
                    eventTypeNameRepeat = true
                    break
                }
            }
            if (eventTypeNameRepeat) {
                //跳名稱不可重複toast
                Toast.makeText(this.applicationContext, "組別名稱不可重複", Toast.LENGTH_SHORT).show()
            } else {
                //新增類別API
                NetworkController
                    .modifyEventTypeAdd(
                        token!!,
                        eventTypeName
                    )
                    .onResponse {
                        //印出收到的東西
                        Log.d("newEventType", "newEventTypeResponse: $it")
                        runOnUiThread {

                            //收到新的心情與活動陣列，存進sharedPreferences
                            sharedPreferences.edit()
                                .putString(MOOD_AND_EVENT, it.toString())
                                .apply()

                            //可以丟Toast訊息告知心情新增成功
                            Toast.makeText(this.applicationContext, "類別新增成功", Toast.LENGTH_SHORT)
                                .show()

                            val intent = Intent(this, EditActTypeActivity::class.java)
                            //若從新增日記進入
                            if (Global.isFromAddDiary) {
                                //有更改活動發生
                                Global.isChangeMoodOrAct = true
                            }
                            startActivity(intent)
                            finish()
                        }
                    }.onFailure {}.onComplete {}.exec()
            }
        }

        btnEditActAddActType.setOnClickListener {
//選擇新增跳出刪除Dialog(alertdialog_delete_mood)
            alertDialogAddActType.show()
        }

        //設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = GridLayoutManager(this, 3)
        //設定layoutManager進去rv讓他們有關聯
        rvEditActType.layoutManager = layoutManager
        //recyclerView設定區
        val adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.new_event_type_item, it, false)
                //要綁資料的話宣告在這裡
                val tvEventTypeName = view.findViewById<TextView>(R.id.tvEventTypeName)
                val tvEventCount = view.findViewById<TextView>(R.id.tvEventCount)

                //用dialog時，context要用對，不然程式會死
                val alertDialogContentViewInside =
                    LayoutInflater.from(it?.context)
                        .inflate(R.layout.alertdialog_delete_mood, null)
                val tvAlertdialogDeleteMoodHint =
                    alertDialogContentViewInside.findViewById<TextView>(R.id.tvAlertdialogDeleteMoodHint)
                val btnAlertdialogDeleteMoodConfirm =
                    alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertdialogDeleteMoodConfirm)
                val btnAlertdialogDeleteMoodCancel =
                    alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertdialogDeleteMoodCancel)

                tvAlertdialogDeleteMoodHint.text = "確定要刪除這個組別嗎?"

                val alertDialogDeleteMood =
                    AlertDialog.Builder(it?.context)
                        .setView(alertDialogContentViewInside)
                        .setCancelable(false)//避免別人填東西過程中被突然取消
                        .create()

                return@Factory object : BaseViewHolder<AddDiaryMoodItem>(view) {
                    override fun bind(item: AddDiaryMoodItem) {

                        tvEventTypeName.text = item.moodName
                        tvEventCount.text = "(${item.imageResource}x)"
                        //點擊事件
                        view.setOnClickListener {
                            //跳出更改刪除選項的popupWindow
                            //選擇更改進入EditMoodEditingActivity
                            //選擇刪除跳出刪除Dialog(alertdialog_delete_mood)

                            Log.d("editMood", "pop of moodEdit")

                            //PopupMenu物件的佈局檔
                            val popupMenu = PopupMenu(applicationContext, view)
                            popupMenu.inflate(R.menu.modify_delete_menu)

                            //點擊事件
                            popupMenu.setOnMenuItemClickListener { popupMenuItem ->
                                when (popupMenuItem.itemId) {
                                    //更改與刪除事件
                                    R.id.menuModify -> {
                                        //選擇更改進入EditActActivity
                                        val intent = Intent(
                                            applicationContext,
                                            EditActActivity::class.java
                                        )
                                        //包入要改的組別名(oldEventTypeName)
                                        val bundle = Bundle()
                                        bundle.putString(
                                            OLD_EVENT_NAME,
                                            item.moodName
                                        )

                                        intent.putExtras(bundle)

                                        startActivity(intent)
                                        finish()

                                        true
                                    }
                                    R.id.menuDelete -> {

                                        //選擇刪除跳出刪除Dialog(alertdialog_delete_mood)
                                        alertDialogDeleteMood.show()

                                        //刪除心情的AlertDialog取消鍵被點擊
                                        btnAlertdialogDeleteMoodCancel.setOnClickListener {
                                            alertDialogDeleteMood.dismiss()
                                        }
                                        //刪除心情的AlertDialog確定鍵被點擊
                                        btnAlertdialogDeleteMoodConfirm.setOnClickListener {
                                            if (eventTypeNames.size <= 1) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "至少要有一個組別",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                alertDialogDeleteMood.dismiss()
                                            } else {
                                                //呼叫刪除API
                                                NetworkController.modifyEventTypeDelete(
                                                    token!!,
                                                    item.moodName!!
                                                ).onResponse {
                                                    //印出收到的東西
                                                    Log.d(
                                                        "editActType",
                                                        "deleteActTypeResponse: $it"
                                                    )

                                                    runOnUiThread {
//收到新的心情與活動陣列，存進sharedPreferences
                                                        sharedPreferences.edit()
                                                            .putString(
                                                                MOOD_AND_EVENT,
                                                                it.toString()
                                                            )
                                                            .apply()

                                                        //可以丟Toast訊息告知心情新增成功
                                                        Toast.makeText(
                                                            applicationContext,
                                                            "組別刪除成功",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        val intent = Intent(
                                                            this@EditActTypeActivity,
                                                            EditActTypeActivity::class.java
                                                        )
                                                        //若從新增日記進入
                                                        if (Global.isFromAddDiary) {
                                                            //有更改活動發生
                                                            Global.isChangeMoodOrAct = true
                                                        }
                                                        startActivity(intent)
                                                        finish()
                                                    }
                                                }.onFailure {}.onComplete {}.exec()

                                                alertDialogDeleteMood.dismiss()
                                            }
                                        }
                                        true
                                    }
                                    else -> false
                                }
                            }

                            //icon顯示會有問題，需做特別處理
                            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup");
                            fieldMPopup.isAccessible = true;
                            val mPopup = fieldMPopup.get(popupMenu)
                            mPopup.javaClass
                                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                                .invoke(mPopup, true)
                            //顯示
                            popupMenu.show()
                        }
                    }
                }
            }, type = AddDiaryMoodItem.TYPE).build()
        rvEditActType.adapter = adapter
        adapter.clear()//開始讀之前先清空

        for (i in eventTypeNames) {
            var count = 0
            for (key in eventJSONObject.getJSONObject(i).keys()) {
                count++
            }
            val item = AddDiaryMoodItem(count.toString(), i)
            adapter.add(item)
        }
    }

    //按下返回鍵
    override fun onBackPressed() {
        if (Global.isFromAddDiary && Global.isChangeMoodOrAct) {
            Global.addDiaryActActivity!!.finish()
            Global.addDiaryActActivity = null
            Global.isFromAddDiary = false
            Global.isChangeMoodOrAct = false
            val intent = Intent(this, AddDiaryActActivity::class.java)
            //放入原先的時間跟心情陣列
            val bundle = Bundle()
            bundle.putString(Global.TIME, addDiaryTime)
            bundle.putIntArray(Global.MOOD, addDiaryMood)
            intent.putExtras(bundle)
            //參考資料型態段開連結
            addDiaryTime = null
            addDiaryMood = null
            startActivity(intent)
            finish()
        } else {
            Global.isFromAddDiary = false
            super.onBackPressed()
        }
    }
}