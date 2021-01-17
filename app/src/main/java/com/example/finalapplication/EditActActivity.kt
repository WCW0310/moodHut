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
import com.example.finalapplication.utils.adapters.CommonAdapter
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.EVENT_TYPE_NAME
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.OLD_EVENT_NAME
import com.example.finalapplication.utils.Global.OLD_ICON_PATH
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.Global.editActActivity
import com.example.finalapplication.utils.NetworkController
import org.json.JSONObject

class EditActActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_act)

        Log.d("editActType", "EditActTypeActivity!!!!!!")

        //要傳出去的東西、要使用的東西

        //把傳過來的TOKEN撈出來
        //取出要改的組別名(oldEventTypeName):OLD_MOOD_NAME
        val oldEventTypeName =
            intent.extras?.getString(OLD_EVENT_NAME)//不是用sharedPreferences存所以再載入會出錯
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
        val tvEditActActTypeName = findViewById<TextView>(R.id.tvEditActActTypeName)
        val rvEditAct = findViewById<RecyclerView>(R.id.rvEditAct)
        val btnEditActAddAct = findViewById<Button>(R.id.btnEditActAddAct)
        val fabEditActEditActTypeName =
            findViewById<ImageView>(R.id.fabEditActEditActTypeName)
        //改組別名的彈跳視窗
        val alertDialogContentView =
            LayoutInflater.from(this)
                .inflate(R.layout.alertdialog_add_act_type, null)
        val btnAlertdialogUpdateActTypeConfirm =
            alertDialogContentView.findViewById<Button>(R.id.btnAlertdialogAddActTypeConfirm)
        val btnAlertdialogUpdateActTypeCancel =
            alertDialogContentView.findViewById<Button>(R.id.btnAlertdialogAddActTypeCancel)
        val etUpdateActTypeName =
            alertDialogContentView.findViewById<EditText>(R.id.etAddActTypeName)
        val alertDialogUpdateActType =
            AlertDialog.Builder(this)
                .setView(alertDialogContentView)
                .setCancelable(false)//避免別人填東西過程中被突然取消
                .create()

        //取消改名
        btnAlertdialogUpdateActTypeCancel.setOnClickListener {
            alertDialogUpdateActType.dismiss()
        }

        //確認改名
        btnAlertdialogUpdateActTypeConfirm.setOnClickListener {
            var eventTypeNameRepeat = false
            var eventTypeName = etUpdateActTypeName.text.toString()

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

            if (eventTypeNameRepeat && eventTypeName == oldEventTypeName) {
                //跳名稱不可重複toast
                Toast.makeText(this.applicationContext, "組別名稱不可重複", Toast.LENGTH_SHORT).show()
            } else {
                //更新類別API
                NetworkController
                    .modifyEventTypeUpdate(
                        token!!,
                        oldEventTypeName!!,
                        eventTypeName
                    )
                    .onResponse {
                        //印出收到的東西
                        Log.d("updateEventType", "updateEventTypeResponse: $it")
                        runOnUiThread {

                            //收到新的心情與活動陣列，存進sharedPreferences
                            sharedPreferences.edit()
                                .putString(MOOD_AND_EVENT, it.toString())
                                .apply()

                            //可以丟Toast訊息告知心情新增成功
                            Toast.makeText(this.applicationContext, "組別名稱更新成功", Toast.LENGTH_SHORT)
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


        //初始化組別名稱
        tvEditActActTypeName.text = oldEventTypeName
        //更改組別名稱按鈕
        fabEditActEditActTypeName.setOnClickListener {
            //設成當前類別名稱
            etUpdateActTypeName.setText(oldEventTypeName)
            alertDialogUpdateActType.show()
        }

        //添加活動按鈕
        btnEditActAddAct.setOnClickListener {
            val intent = Intent(this, NewActActivity::class.java)
            //包入組別名名(oldMoodName)
            val bundle = Bundle()
            bundle.putString(EVENT_TYPE_NAME, oldEventTypeName)
            intent.putExtras(bundle)
            //記住這個活動類別的EditActActivity，讓他在新增日記完成時一起被finish()
            editActActivity = this
            startActivity(intent)
        }

        //設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = GridLayoutManager(this, 4)
        //設定layoutManager進去rv讓他們有關聯
        rvEditAct.layoutManager = layoutManager
        //recyclerView設定區
        val adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.edit_mood_item, it, false)
                //要綁資料的話宣告在這裡
                val ivEditMoodItem = view.findViewById<ImageView>(R.id.ivEditMoodItem)
                val tvEditMoodItem = view.findViewById<TextView>(R.id.tvEditMoodItem)

                //用dialog時，context要用對，不然程式會死
                val alertDialogContentViewInside =
                    LayoutInflater.from(it?.context)
                        .inflate(R.layout.alertdialog_delete_mood, null)
                val btnAlertdialogDeleteMoodConfirm =
                    alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertdialogDeleteMoodConfirm)
                val btnAlertdialogDeleteMoodCancel =
                    alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertdialogDeleteMoodCancel)
                val alertDialogDeleteMood =
                    AlertDialog.Builder(it?.context)
                        .setView(alertDialogContentViewInside)
                        .setCancelable(false)//避免別人填東西過程中被突然取消
                        .create()

                return@Factory object : BaseViewHolder<AddDiaryMoodItem>(view) {
                    override fun bind(item: AddDiaryMoodItem) {
                        //設定圖片
                        Global.iconPairing(ivEditMoodItem, item.imageResource!!)
                        tvEditMoodItem.text = item.moodName
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
                                        //選擇更改進入EditMoodEditingActivity
                                        val intent = Intent(
                                            applicationContext,
                                            EditActEditingActivity::class.java
                                        )
                                        //包入要改的活動名(oldActName)
                                        val bundle = Bundle()
                                        bundle.putString(
                                            OLD_EVENT_NAME,
                                            item.moodName
                                        )//(oldActName)
                                        bundle.putString(OLD_ICON_PATH, item.imageResource)
                                        intent.putExtras(bundle)
                                        editActActivity = this@EditActActivity
                                        startActivity(intent)

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
                                            if (moodNames.size <= 1) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "至少要有一個活動",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                alertDialogDeleteMood.dismiss()
                                            } else {
                                                //呼叫刪除API
                                                NetworkController.modifyEventDelete(
                                                    token!!,
                                                    item.moodName!!
                                                ).onResponse {
                                                    //印出收到的東西
                                                    Log.d("editAct", "deleteActResponse: $it")

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
                                                            "活動刪除成功",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        val intent = Intent(
                                                            this@EditActActivity,
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
        rvEditAct.adapter = adapter
        adapter.clear()//開始讀之前先清空

        for (i in eventJSONObject.getJSONObject(oldEventTypeName!!).keys()) {
            val item = AddDiaryMoodItem(actPathMap[i], i)
            adapter.add(item)
        }
    }

    //按下返回鍵跳出AlertDialog提示再按一次結束程式
    override fun onBackPressed() {
        val intent = Intent(this, EditActTypeActivity::class.java)
        startActivity(intent)
        finish()
    }
}