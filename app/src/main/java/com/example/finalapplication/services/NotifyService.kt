package com.example.finalapplication.services

import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.finalapplication.ChatRoomActivity
import com.example.finalapplication.R
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.CHAT_ROOM_ID
import com.example.finalapplication.utils.Global.MY_NAME
import com.example.finalapplication.utils.Global.OTHERS_NAME
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat


class NotifyService() : Service() {
    private var isServiceRunning = false
    private val URL_ROOT = "http://35.229.145.199:3000"
    var count = 0
    var userId: String? = null
    var chatroomId: Int? = null
    var notifyThread = NotifyThread()
    var isRun: Boolean = true


    //獨立做的Okhttp
    var okhttp = OkHttpClient();
    val JSON = "application/json; charset=utf-8".toMediaType() //Postman也會帶的header
    var connecting: Boolean = true;

    //執行緒1
    inner class NotifyThread : Thread() {
        override fun run() {
            while (isRun) {
                count++;
                sleep(2000);   //每兩秒跑一圈，降低負荷
//                Log.d("yamiew", "run is working, count = $count");

                //連線，用connecting:Boolean控制非同步的部分
                while (connecting) {
                    connecting = false;
//                    Log.d("connecting", "connecting is true");
                    val request = Request.Builder()
                        .url("${URL_ROOT}/notification/isNotified/?userId=$userId")
                        .get()
                        .build();
                    okhttp.newCall(request = request).enqueue(responseCallback = object : Callback {
                        override fun onResponse(call: Call, response: Response) {
//                            Log.d("yamiew", "onResponse is executed");
                            //接收json格式
                            var jsonObject = JSONObject(response.body?.string());

                            //若api回傳false就不必做事情，true則處理訊息更新
                            if (jsonObject.getString("msg") == "false") {
                                connecting = true;
                                return
                            };

                            //更新聊天室
                            Global.refreshChatList();

                            //存下所有的notificationId，並呼叫下一個api
                            var arr = jsonObject.getJSONArray("notificationId");
                            getContentApi(arr);
                        }

                        override fun onFailure(call: Call, e: IOException) {
                            connecting = true
                        }
                    })
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //判斷service是否在運作
        if (isServiceRunning) return super.onStartCommand(intent, flags, startId)
        else isServiceRunning = true

        isRun = true;
        //記下bundle傳來的id與chatroomId
        userId = intent?.getStringExtra("Token");
        chatroomId = if (intent?.getStringExtra("chatroomId") == null) {
            null
        } else {
            Integer.parseInt(intent?.getStringExtra("chatroomId"))
        };
//        Log.d("yamiew", "onStart receive token = $userId");
//        Log.d("yamiew", "onStart receive chatroomId = $chatroomId");
        notifyThread.start();
        return super.onStartCommand(intent, flags, startId)
    }

    //必須實現但用不上的方法
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy();
        isRun = false;
    }

    //可以拿推播詳細資料的api
    fun getContentApi(jsonArray: JSONArray) {
//        Log.d("getContentApi", "getContentApi is executed");
        var jsonObject = JSONObject();
        jsonObject.put("notificationId", jsonArray);
        val requestBody = jsonObject.toString().toRequestBody(JSON);
//        Log.d("requestbody", jsonObject.toString());

        val request = Request.Builder()
            .url("${URL_ROOT}/notification/content")
            .post(requestBody)
            .build();
        //連線
        okhttp.newCall(request = request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonArray = JSONObject(response.body?.string())
                    .getJSONArray("notify");
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i);
                    //同房的則刷新頁面
                    if (Global.whereChatroomId == jsonObject.getInt("chatroomId")) {
                        Global.roomLoad?.let { it() }
                        continue
                    }
                    //不同房的訊息則推播
                    notification(
                        chatroomId = jsonObject.getInt("chatroomId"),
                        myName = jsonObject.getString("myName"),
                        othersName = jsonObject.getString("othersName"),
                        text = jsonObject.getString("text"),
                        send_time = jsonObject.getString("send_time")
                    );
                }

                //迴圈跑完再開啟connecting
                connecting = true;
            }

        })
    }

    //推播主體
    fun notification(
        chatroomId: Int,
        myName: String,
        othersName: String,
        text: String,
        send_time: String
    ) {
//        Log.d("notification", name);
//        Log.d("notification", text);
//        Log.d("notification", send_time);
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm");
        val formatSendTime = format.parse(send_time).time;


        //推播測試
        // Create an explicit intent for an Activity in your app
        val resultIntent = Intent(this, ChatRoomActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //定義bundle要帶過去的資料
        val bundle = Bundle()
        bundle.putInt(CHAT_ROOM_ID, chatroomId)
        bundle.putString(MY_NAME, myName)
        bundle.putString(OTHERS_NAME, othersName)
        resultIntent.putExtras(bundle)

        //告訴service進入哪間房
        Global.whereChatroomId = chatroomId

        //設定notification點擊後頁面跳轉
        val stackBuilder =TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent =stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0)



        val builder = NotificationCompat.Builder(this, "firstChannel")
            .setSmallIcon(R.drawable.journal)
            .setContentTitle(othersName)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(resultPendingIntent)
            .setWhen(formatSendTime)
            .setAutoCancel(true)
        //show出來，以房號作為推播id
        with(NotificationManagerCompat.from(this)) {
            notify(chatroomId, builder.build())
        }
    }


}