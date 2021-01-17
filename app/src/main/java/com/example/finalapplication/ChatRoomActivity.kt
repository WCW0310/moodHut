package com.example.finalapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.items.MyMessageItem
import com.example.finalapplication.items.OthersMessageItem
import com.example.finalapplication.items.SystemMessageItem
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.CHAT_ROOM_ID
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.MY_NAME
import com.example.finalapplication.utils.Global.OTHERS_NAME
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.Global.toChatList
import com.example.finalapplication.utils.IType
import com.example.finalapplication.utils.NetworkController
import com.example.finalapplication.utils.adapters.CommonAdapter
import org.json.JSONObject

class ChatRoomActivity : AppCompatActivity() {
    var chatRoomId = 0

    //訊息列第一條訊息的messageId
    var firstMessageId = ""

    var adapter: CommonAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        Log.d("chatRoom", "chatRoomActivity!!!!!!")

        //要傳出去的東西、要使用的東西
        //把傳過來的TOKEN撈出來
        //取得訊息用

        //判斷是誰的訊息用
        val myName = intent.extras?.getString(MY_NAME)
        val othersName = intent.extras?.getString(OTHERS_NAME)
        chatRoomId = intent.extras?.getInt(CHAT_ROOM_ID)!!
        Log.d("chatRoomId", "" + chatRoomId!!)
        val sharedPreferences = this.getSharedPreferences(
            DATA,
            Context.MODE_PRIVATE
        )//好像是特殊用法
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

        for (key in moodJSONObject.keys()) {
            moodPathMap[key] = moodJSONObject.getInt(key).toString()
        }

        for (key in eventJSONObject.keys()) {
            for (key2 in eventJSONObject.getJSONObject(key).keys()) {
                actPathMap[key2] = eventJSONObject.getJSONObject(key).getInt(key2).toString()
            }
        }

        //宣告物件
        val tvChatRoomTitle = findViewById<TextView>(R.id.tvChatRoomTitle)
        val ivChatRoomLeaveRoom = findViewById<ImageView>(R.id.ivChatRoomLeaveRoom)
        val etChatRoomMessage = findViewById<EditText>(R.id.etChatRoomMessage)
        val ivChatRoomSend = findViewById<ImageView>(R.id.ivChatRoomSend)
        val rvChatRoomMessages = findViewById<RecyclerView>(R.id.rvChatRoomMessages)

        //離開聊天室的Dialog
        //用dialog時，context要用對，不然程式會死
        val alertDialogContentViewInside =
            LayoutInflater.from(this)
                .inflate(R.layout.alertdialog_delete_mood, null)
        val tvAlertdialogDeleteMoodHint =
            alertDialogContentViewInside.findViewById<TextView>(R.id.tvAlertdialogDeleteMoodHint)
        val btnAlertdialogDeleteMoodConfirm =
            alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertdialogDeleteMoodConfirm)
        val btnAlertdialogDeleteMoodCancel =
            alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertdialogDeleteMoodCancel)

        tvAlertdialogDeleteMoodHint.text = "離開聊天室將無法再收到對方訊息，確定要離開聊天室嗎?"
        btnAlertdialogDeleteMoodConfirm.text = "離開"

        //離開聊天室的警示視窗
        val alertDialogDeleteMood =
            AlertDialog.Builder(this)
                .setView(alertDialogContentViewInside)
                .setCancelable(false)//避免別人填東西過程中被突然取消
                .create()

        btnAlertdialogDeleteMoodCancel.setOnClickListener {
            alertDialogDeleteMood.dismiss()
        }

        btnAlertdialogDeleteMoodConfirm.setOnClickListener {
            //呼叫刪除聊天室API
            NetworkController.chatChatRoomLeave(
                token!!,
                chatRoomId
            ).onResponse {
                //印出收到的東西
                Log.d(
                    "leaveChatRoom",
                    "leaveChatRoomResponse: $it"
                )

                runOnUiThread {
                    //可以丟Toast訊息告知心情新增成功
                    Toast.makeText(
                        this,
                        "已離開聊天室",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(
                        this,
                        BottomNavigationActivity::class.java
                    )
                    //告訴BottomNavigationActivity要移動到ChatListFragment
                    toChatList = true
                    startActivity(intent)
                    finish()
                }
            }.onFailure {}.onComplete {}.exec()

            alertDialogDeleteMood.dismiss()
        }

        ivChatRoomLeaveRoom.setOnClickListener {
            alertDialogDeleteMood.show()
        }

        //將對方暱稱設為聊天室標題
        tvChatRoomTitle.text = othersName

        //對方離開聊天室時不能再傳送訊息的Boolean
        var sendAble = true

        //顯示訊息rv
        //設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = LinearLayoutManager(this)
        //設定layoutManager進去rv讓他們有關聯
        rvChatRoomMessages.layoutManager = layoutManager
        //recyclerView設定區
        adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chat_room_message_mine_item, it, false)
                //若本來background布局有設shape就不能在程式碼中設顏色，會蓋掉本來的形狀

                //要綁資料的話宣告在這裡
                val tvMyMessage = view.findViewById<TextView>(R.id.tvMyMessage)
                val tvMyMessageTime = view.findViewById<TextView>(R.id.tvMyMessageTime)

                return@Factory object : BaseViewHolder<MyMessageItem>(view) {
                    override fun bind(item: MyMessageItem) {
                        tvMyMessage.text = item.text
                        //自己的框是綠色

                        tvMyMessageTime.text = item.sendTime
                    }
                }
            }, type = MyMessageItem.TYPE)
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chat_room_message_others_item, it, false)

                //要綁資料的話宣告在這裡
                val tvOthersMessage = view.findViewById<TextView>(R.id.tvOthersMessage)
                val tvOthersMessageTime = view.findViewById<TextView>(R.id.tvOthersMessageTime)

                return@Factory object : BaseViewHolder<OthersMessageItem>(view) {
                    override fun bind(item: OthersMessageItem) {
                        tvOthersMessage.text = item.text
                        //對方的框是白色

                        tvOthersMessageTime.text = item.sendTime
                    }
                }
            }, type = OthersMessageItem.TYPE)
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chat_room_message_system_item, it, false)

                //要綁資料的話宣告在這裡
                val tvSystemMessage = view.findViewById<TextView>(R.id.tvSystemMessage)
                val tvSystemMessageTime = view.findViewById<TextView>(R.id.tvSystemMessageTime)

                return@Factory object : BaseViewHolder<SystemMessageItem>(view) {
                    override fun bind(item: SystemMessageItem) {
                        tvSystemMessage.text = item.text
                        //系統的眶是灰色

                        tvSystemMessageTime.text = item.sendTime
                    }
                }
            }, type = SystemMessageItem.TYPE)
            .header(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chat_room_show_more_message, it, false)

                //要綁資料的話宣告在這裡
                val tvChatRoomShowMoreMessage =
                    view.findViewById<TextView>(R.id.tvChatRoomShowMoreMessage)

                tvChatRoomShowMoreMessage.setOnClickListener {
                    //更多訊息API
                    adapter!!.clear()//開始讀之前先清空
                    NetworkController.chatChatRoomHistory(
                        chatRoomId.toString(),
                        firstMessageId
                    )
                        .onResponse {
                            //印出收到的東西
                            Log.d("chatChatRoomHistory", "messages: $it")
                            runOnUiThread {
                                val messages = it.getJSONArray("chat")
                                //存第一則訊息的messageId
                                firstMessageId = "" + messages.getJSONObject(0).getInt("messageId")
                                Log.d("chatChatRoomHistory", "firstMessageId: $firstMessageId")
                                for (i in 0 until messages.length()) {
                                    val messageObject = messages.getJSONObject(i)
                                    var message: IType? = null
                                    when (messageObject.getString("name")) {
                                        myName -> {
                                            message = MyMessageItem(
                                                messageObject.getInt("messageId"),
                                                messageObject.getString("name"),
                                                messageObject.getString("text"),
                                                messageObject.getString("sendTime")
                                            )
                                        }
                                        othersName -> {
                                            message = OthersMessageItem(
                                                messageObject.getInt("messageId"),
                                                messageObject.getString("name"),
                                                messageObject.getString("text"),
                                                messageObject.getString("sendTime")
                                            )
                                        }
                                        "<系統>" -> {
                                            message = SystemMessageItem(
                                                messageObject.getInt("messageId"),
                                                messageObject.getString("name"),
                                                messageObject.getString("text"),
                                                messageObject.getString("sendTime")
                                            )
                                            //有離開聊天室的訊息就不能再傳訊息
                                            sendAble = false
                                        }
                                    }
                                    adapter!!.add(message!!)
                                    Log.d("rvLength", "" + "${adapter!!.itemCount - 1}")
                                }
                                //訊息置底
//                                (rvChatRoomMessages.layoutManager as LinearLayoutManager).scrollToPosition(
//                                    adapter!!.itemCount - 1
//                                )

                            }
                        }.onFailure {}.onComplete {}.exec()
                }

                return@Factory object : BaseViewHolder<IType>(view) {
                    override fun bind(item: IType) {
                        //只有一顆顯示更多紐，無資料，不綁資料
                    }
                }
            })
            .build()


        rvChatRoomMessages.adapter = adapter



        Log.d("yamiew", "$myName, $othersName, $chatRoomId");
        //網路取得訊息陣列
        val roomLoad = fun() {
            NetworkController.chatChatRoomLoad(
                chatRoomId.toString()
            )
                .onResponse {
                    //印出收到的東西
                    Log.d("chatChatRoomLoad", "messages: $it")
                    runOnUiThread {
                        val messages = it.getJSONArray("chat")
                        //存第一則訊息的messageId
                        firstMessageId = "" + messages.getJSONObject(0).getInt("messageId")
                        Log.d("chatChatRoomHistory", "firstMessageId: $firstMessageId")

                        adapter!!.clear()//開始讀之前先清空

                        for (i in 0 until messages.length()) {
                            val messageObject = messages.getJSONObject(i)
                            var message: IType? = null
                            when (messageObject.getString("name")) {
                                myName -> {
                                    message = MyMessageItem(
                                        messageObject.getInt("messageId"),
                                        messageObject.getString("name"),
                                        messageObject.getString("text"),
                                        messageObject.getString("sendTime")
                                    )
                                }
                                othersName -> {
                                    message = OthersMessageItem(
                                        messageObject.getInt("messageId"),
                                        messageObject.getString("name"),
                                        messageObject.getString("text"),
                                        messageObject.getString("sendTime")
                                    )
                                }
                                "<系統>" -> {
                                    message = SystemMessageItem(
                                        messageObject.getInt("messageId"),
                                        messageObject.getString("name"),
                                        messageObject.getString("text"),
                                        messageObject.getString("sendTime")
                                    )
                                    //有離開聊天室的訊息就不能再傳訊息
                                    sendAble = false
                                }
                            }
                            adapter!!.add(message!!)
                        }
                        //訊息置底
                        (rvChatRoomMessages.layoutManager as LinearLayoutManager).scrollToPosition(
                            adapter!!.itemCount - 1
                        )
                    }
                }.onFailure {}.onComplete {}.exec()
        }
        roomLoad()
        //告訴service該刷新誰
        Global.roomLoad = roomLoad;

        //傳送訊息按鈕
        ivChatRoomSend.setOnClickListener {
            if (!sendAble) {
                Toast.makeText(
                    this,
                    "對方已離開聊天室，無法再發送訊息",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val message = etChatRoomMessage.text.toString()
                if (message == "") {
                    Toast.makeText(
                        this,
                        "請輸入訊息",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    NetworkController.chatChatRoomSend(
                        chatRoomId,
                        token!!,
                        message

                    ).onResponse {
                        //印出收到的東西
                        Log.d(
                            "chatRoomSend",
                            "chatRoomSendResponse: $it"
                        )

                        runOnUiThread {
                            //清空輸入框
                            etChatRoomMessage.setText("")
                        }
                    }.onFailure {}.onComplete {}.exec()
                }
            }

            adapter!!.clear()//開始讀之前先清空
            //網路取得訊息陣列
            NetworkController.chatChatRoomLoad(
                chatRoomId.toString()
            )
                .onResponse {
                    //印出收到的東西
                    Log.d("chatChatRoomLoad", "messages: $it")
                    runOnUiThread {
                        val messages = it.getJSONArray("chat")
                        //存第一則訊息的messageId
                        firstMessageId = "" + messages.getJSONObject(0).getInt("messageId")
                        Log.d("chatChatRoomHistory", "firstMessageId: $firstMessageId")
                        adapter!!.clear()//開始讀之前先清空

                        for (i in 0 until messages.length()) {
                            val messageObject = messages.getJSONObject(i)
                            var message: IType? = null
                            when (messageObject.getString("name")) {
                                myName -> {
                                    message = MyMessageItem(
                                        messageObject.getInt("messageId"),
                                        messageObject.getString("name"),
                                        messageObject.getString("text"),
                                        messageObject.getString("sendTime")
                                    )
                                }
                                othersName -> {
                                    message = OthersMessageItem(
                                        messageObject.getInt("messageId"),
                                        messageObject.getString("name"),
                                        messageObject.getString("text"),
                                        messageObject.getString("sendTime")
                                    )
                                }
                                "<系統>" -> {
                                    message = SystemMessageItem(
                                        messageObject.getInt("messageId"),
                                        messageObject.getString("name"),
                                        messageObject.getString("text"),
                                        messageObject.getString("sendTime")
                                    )
                                    //有離開聊天室的訊息就不能再傳訊息
                                    sendAble = false
                                }
                            }
                            adapter!!.add(message!!)
                            Log.d("rvLength", "" + "${adapter!!.itemCount - 1}")
                        }
                        //訊息置底
                        (rvChatRoomMessages.layoutManager as LinearLayoutManager).scrollToPosition(
                            adapter!!.itemCount - 1
                        )

                    }
                }.onFailure {}.onComplete {}.exec()
        }
    }

    //按下返回鍵
    override fun onBackPressed() {
        Global.whereChatroomId = null
        Global.roomLoad = null
        //告訴BottomNavigationActivity要移動到ChatListFragment
        toChatList = true
        val intentBottomNavigationActivity = Intent(this, BottomNavigationActivity::class.java)
        startActivity(intentBottomNavigationActivity)
        finish()
    }
}