package com.example.finalapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.finalapplication.services.NotifyService
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.NetworkController

class LoginActivity : AppCompatActivity() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var token: String
    lateinit var intentForThisActivity: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d("login", "LoginActivity!!!!!!")
        sharedPreferences = getSharedPreferences(DATA, MODE_PRIVATE)!!//好像是特殊用法
        token = sharedPreferences.getString(TOKEN, "")!!
        intentForThisActivity = Intent(this, BottomNavigationActivity::class.java)

        //推播的channel創建
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "firstChannel"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_HIGH;
            val mChannel = NotificationChannel("firstChannel", name, importance)
            mChannel.description = descriptionText
            //調成靜音
            mChannel.setSound(null, null)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    //Activity生命週期一定會經過的方法
    @SuppressLint("HardwareIds")
    override fun onResume() {
        super.onResume()
        //若sharedPreferences中有token表示註冊過，直接進行登入身分驗證
        if (sharedPreferences.contains(TOKEN)) {
            Log.d("login", "login: " + "contains TOKEN")
            Log.d("login", "token: $token")
            NetworkController.login(token!!).onResponse {
                //印出收到的東西
                Log.d("login", "verify: $it")
                //要跳轉畫面、要改文字等等都要用runOnUiThread做
                this.runOnUiThread {
                    //開啟service
                    val serviceIntent = Intent(this, NotifyService::class.java);
                    val serviceBundle = Bundle();
                    serviceBundle.putString("Token", token);
                    serviceBundle.putString("chatroomId", null);
                    serviceIntent.putExtras(serviceBundle);
                    startService(serviceIntent);

                    if (it.getString("msg") == "Valid UserId.") {
                        startActivity(intentForThisActivity)
                        finish()
                    } else {
                        Log.d("login", "wrong token: $it")
                        //驗證失敗處理
                        Toast.makeText(
                            applicationContext,
                            "無此用戶",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.onFailure {}.onComplete {}.exec()
        } else { //若sharedPreferences中無token，進行註冊，伺服器回傳token
            Log.d("login", "register: " + "no TOKEN")

            //改用AndroidId
            val androidID: String =
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            Log.d("login", "androidID: $androidID")

            NetworkController.register(androidID).onResponse {
                //印出收到的東西
                Log.d("login", "token&mood&act: $it")
                Log.d("login", it.getString("msg"))

                //要跳轉畫面、要改文字等等都要用runOnUiThread做
                this.runOnUiThread {
                    //讓App記住token之後才能自動登入
                    //讓App記住moodAndEvent
                    val moodAndEvent = it.getJSONObject("moodAndEvent")
                    Log.d("login", moodAndEvent.toString())
                    sharedPreferences.edit()
                        .putString(TOKEN, it.getString("userId"))
                        .putString(MOOD_AND_EVENT, moodAndEvent.toString())
                        .apply()

                    //開啟service
                    val serviceIntent = Intent(this, NotifyService::class.java);
                    val serviceBundle = Bundle();
                    serviceBundle.putString("Token", token);
                    serviceBundle.putString("chatroomId", null);
                    serviceIntent.putExtras(serviceBundle);
                    startService(serviceIntent);

                    startActivity(intentForThisActivity)
                    finish()
                }
            }.onFailure {}.onComplete {}.exec()
        }
    }

    //按下返回鍵跳出AlertDialog提示再按一次結束程式
    override fun onBackPressed() {
        val ad: AlertDialog.Builder = AlertDialog.Builder(this)
        ad.setTitle("離開")
        ad.setMessage("確定要離開此程式嗎?")
        ad.setPositiveButton("是", DialogInterface.OnClickListener { dialog, i ->

            //退出按鈕
            // TODO Auto-generated method stub
            this.finish() //關閉activity
        })
        ad.setNegativeButton("否", DialogInterface.OnClickListener { dialog, i ->
            onResume()
            //不退出不用執行任何操作
        })
        ad.show() //顯示對話框
    }
}