package com.example.finalapplication.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.finalapplication.ChatRoomActivity
import com.example.finalapplication.R
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.CHAT_ROOM_ID
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.MY_NAME
import com.example.finalapplication.utils.Global.OTHERS_NAME
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.NetworkController
import org.json.JSONArray
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SquareFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SquareFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Log.d("square", "squareFragment!!!!!!")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_square, container, false)
        //再用view變數找其他元件

        //要傳出去的東西、要使用的東西
        val sharedPreferences = activity?.getSharedPreferences(
            DATA,
            Context.MODE_PRIVATE
        )//好像是特殊用法
        val token = sharedPreferences?.getString(TOKEN, "")
        val nickName = sharedPreferences?.getString(NICK_NAME, "")
        val moodAndEvent = sharedPreferences?.getString(MOOD_AND_EVENT, "")
        val moodAndEventJson = JSONObject(moodAndEvent!!)
        //心情圖示配對
        val moodPathMap: MutableMap<String, String> = mutableMapOf()

        //活動圖示配對
        val actPathMap: MutableMap<String, String> = mutableMapOf()

        //填入心情與活動的圖示配對
        Global.fillPathMap(moodAndEventJson, moodPathMap, actPathMap)

        //宣告物件
        //更改自己貼文的按鈕，跳出一視窗，可更新、刪除、取消
        val tvSquareEditPost = fragmentView.findViewById<TextView>(R.id.tvSquareEditPost)
        //更新所有貼文的按鈕
        val tvSquareRefresh = fragmentView.findViewById<TextView>(R.id.tvSquareRefresh)
        //該篇貼文作者暱稱
        val tvSquareHostName = fragmentView.findViewById<TextView>(R.id.tvSquareHostName)
        //該篇貼文張貼日期
        val tvSquarePostDate = fragmentView.findViewById<TextView>(R.id.tvSquarePostDate)
        //塞入貼文日記的ll
        val llSquareDiary = fragmentView.findViewById<LinearLayout>(R.id.llSquareDiary)
        //該篇貼文主內容
        val tvSquarePost = fragmentView.findViewById<TextView>(R.id.tvSquarePost)
        //使用者設定自己暱稱，會記錄上次使用的暱稱(sharedPreferences)
        val etSquareNickName = fragmentView.findViewById<EditText>(R.id.etSquareNickName)
        etSquareNickName.setText(nickName)
        //傳送訊息的按鈕，創建聊天室，紀錄暱稱(sharedPreferences)
        val ivSquareSend = fragmentView.findViewById<ImageView>(R.id.ivSquareSend)
        //訊息內容
        val etSquareMessage = fragmentView.findViewById<EditText>(R.id.etSquareMessage)
        //下一篇內Po文
        val tvSquareNext = fragmentView.findViewById<TextView>(R.id.tvSquareNext)
        //上一篇內Po文
        val tvSquarePrior = fragmentView.findViewById<TextView>(R.id.tvSquarePrior)
        //顯示總篇數分之第幾篇
        val tvSquarePostNum = fragmentView.findViewById<TextView>(R.id.tvSquarePostNum)

        //網路取得的全部貼文，因為一次只秀一篇，所以要全部存下來
        var squarePosts = JSONArray()
        //顯示的po文是第幾篇
        var postNum = 0

        //diaryId是搭配貼文分享的日記，傳送訊息時要用
        var diaryId = 0
        //diary是日記詳細內容
        var diary: JSONObject

        //日記的整個view
        val diaryView =
            LayoutInflater.from(context).inflate(R.layout.diary_book_item, container, false)
        diaryView.elevation = 0.0F//讓分享廣場的日記不要有提升水平效果

        //要綁資料的話宣告在這裡
        val tvRecordDate = diaryView.findViewById<TextView>(R.id.tvRecordDate)
        val tvDiaryNote = diaryView.findViewById<TextView>(R.id.tvDiaryNote)
        //要動態生成活動物件的ll
        val llDiaryBookItemMood = diaryView.findViewById<LinearLayout>(R.id.llDiaryBookItemMood)
        val llDiaryBookItemAct = diaryView.findViewById<LinearLayout>(R.id.llDiaryBookItemAct)

        //橫著塞mood或event的ll
        var llMood = LinearLayout(context);
        llMood.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        var llEvent = LinearLayout(context);
        llEvent.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        //mood的名字
        val moodNames = mutableListOf<String>()
        //mood的Icon
        val moodIcons = mutableListOf<String>()
        //mood的score
        val moodScores = mutableListOf<String>()
        //event的名字
        val eventNames = mutableListOf<String>()
        //event的Icon
        val eventIcons = mutableListOf<String>()

        //傳送訊息、開新房間: etSquareNickName、etSquareMessage、diaryId
        //需要連續呼叫兩隻API: 1. 確認房間，儲存房號、2. 將訊息傳至聊天室
        //將使用的暱稱記住
        ivSquareSend.setOnClickListener {
            //若暱稱超過10字跳通知，結束
            if (etSquareNickName.text.length > 10) {
                Toast.makeText(activity!!.applicationContext, "暱稱字數過長", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            NetworkController.chatChatRoomCreate(
                diaryId.toString(),
                tvSquareHostName.text as String,
                token!!,
                etSquareNickName.text.toString(),
                etSquareMessage.text.toString()
            ).onResponse {
                Log.d("square", it.toString())
                activity!!.runOnUiThread {
                    when (it.getString("msg")) {
                        "已有此房間" -> {
                            Toast.makeText(
                                context,
                                "已有此聊天室，快去跟對方交流吧",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        "對方聊天室達上限" -> {
                            Toast.makeText(
                                context,
                                "OH~NO~此貼文聊天室已達上限，下次手腳要快喔",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            //存入這次的暱稱
                            sharedPreferences.edit()
                                .putString(NICK_NAME, etSquareNickName.text.toString())
                                .apply()

                            //跳轉至聊天室
                            val intent = Intent(context, ChatRoomActivity::class.java)
                            val bundle = Bundle()
                            bundle.putInt(CHAT_ROOM_ID, it.getInt("chatRoomId"))
                            bundle.putString(MY_NAME, etSquareNickName.text.toString())
                            bundle.putString(OTHERS_NAME, tvSquareHostName.text as String)
                            intent.putExtras(bundle)
                            //告訴service進入哪間房
                            Global.whereChatroomId = it.getInt("chatRoomId")

                            startActivity(intent)
                            Toast.makeText(
                                context,
                                "聊天室建立成功，快與對方交流吧!",
                                Toast.LENGTH_SHORT
                            ).show()
                            activity!!.finish()
                        }
                    }
                }
            }.onFailure {}.onComplete {}.exec()
        }

        //取得自己貼文資訊
        tvSquareEditPost.setOnClickListener {
            //取得po文API，並填入當初的日記
            NetworkController.chatSquareMine(token!!).onResponse {
                //印出收到的東西
                Log.d(
                    "chatSquareMine",
                    "chatSquareMineResponse: $it"
                )
                activity?.runOnUiThread {
                    //沒貼文只跳Toast訊息
                    if (it.getString("msg") == "該用戶沒有貼文") {
                        Toast.makeText(
                            context,
                            "目前還沒有貼文，快去分享吧!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        //用dialog時，context要用對，不然程式會死
                        val alertDialogContentView =
                            LayoutInflater.from(context)
                                .inflate(R.layout.alertdialog_square_get_my_post, null)
                        //當初發文的暱稱，不可更改
                        val tvAlrtdialogSquareGetMyPostNickName =
                            alertDialogContentView.findViewById<TextView>(R.id.tvAlertdialogSquareGetMyPostNickName)
                        //發文時間，不可更改
                        val tvAlertdialogSquareGetMyPostPostDate =
                            alertDialogContentView.findViewById<TextView>(R.id.tvAlertdialogSquareGetMyPostPostDate)
                        //裝日記的ll
                        val llAlertdialogSquareGetMyPostDiary =
                            alertDialogContentView.findViewById<LinearLayout>(R.id.llAlertdialogSquareGetMyPostDiary)
                        //貼文內容，可更改
                        val etAlertdialogSquareGetMyPostPost =
                            alertDialogContentView.findViewById<EditText>(R.id.etAlertdialogSquareGetMyPostPost)
                        //取消鍵
                        val btnAlertdialogSquareGetMyPostCancel =
                            alertDialogContentView.findViewById<Button>(R.id.btnAlertdialogSquareGetMyPostCancel)
                        //刪除貼文鍵
                        val btnAlertdialogSquareGetMyPostDeletePost =
                            alertDialogContentView.findViewById<Button>(R.id.btnAlertdialogSquareGetMyPostDeletePost)
                        //更新貼文鍵
                        val btnAlertdialogSquareGetMyPostUpdatePost =
                            alertDialogContentView.findViewById<Button>(R.id.btnAlertdialogSquareGetMyPostUpdatePost)

                        val alertDialogEditPost =
                            AlertDialog.Builder(context)
                                .setView(alertDialogContentView)
                                .setCancelable(false)//避免別人填東西過程中被突然取消
                                .show()

                        //取消
                        btnAlertdialogSquareGetMyPostCancel.setOnClickListener {
                            alertDialogEditPost.dismiss()
                        }
                        //刪除貼文
                        btnAlertdialogSquareGetMyPostDeletePost.setOnClickListener {
                            NetworkController.chatSquareDelete(token!!).onResponse {
                                //印出收到的東西
                                Log.d(
                                    "chatSquareDelete",
                                    "chatSquareDeleteResponse: $it"
                                )

                                activity?.runOnUiThread {
                                    val msg = it.getString("msg")
                                    if (msg == "success") {
                                        Toast.makeText(
                                            context,
                                            "貼文刪除成功",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "目前還沒有貼文，快去分享吧!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }.onFailure {}.onComplete {}.exec()

                            alertDialogEditPost.dismiss()
                        }
                        //更新貼文
                        btnAlertdialogSquareGetMyPostUpdatePost.setOnClickListener {
                            NetworkController.chatSquareEdit(
                                token!!,
                                etAlertdialogSquareGetMyPostPost.text.toString()
                            ).onResponse {
                                //印出收到的東西
                                Log.d(
                                    "chatSquareEdit",
                                    "chatSquareEditResponse: $it"
                                )

                                activity?.runOnUiThread {
                                    val msg = it.getString("msg")
                                    if (msg == "success") {
                                        Toast.makeText(
                                            context,
                                            "貼文更新成功",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "目前還沒有貼文，快去分享吧!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }.onFailure {}.onComplete {}.exec()
                            alertDialogEditPost.dismiss()
                        }


                        //填入po文資料
                        //po文時的暱稱
                        tvAlrtdialogSquareGetMyPostNickName.text = it.getString("hostName")
                        //po文時間
                        tvAlertdialogSquareGetMyPostPostDate.text = it.getString("postDate")
                        //po文內容
                        etAlertdialogSquareGetMyPostPost.setText(it.getString("post"))

                        //填入日記
                        //日記的整個view
                        val myPostDiaryView = LayoutInflater.from(context)
                            .inflate(R.layout.diary_book_item, container, false)
                        myPostDiaryView.elevation = 0.0f
                        //要綁資料的話宣告在這裡
                        val tvRecordDateMyPost =
                            myPostDiaryView.findViewById<TextView>(R.id.tvRecordDate)
                        val tvDiaryNoteMyPost =
                            myPostDiaryView.findViewById<TextView>(R.id.tvDiaryNote)
                        //要動態生成活動物件的ll
                        val llDiaryBookItemMoodMyPost =
                            myPostDiaryView.findViewById<LinearLayout>(R.id.llDiaryBookItemMood)
                        val llDiaryBookItemActMyPost =
                            myPostDiaryView.findViewById<LinearLayout>(R.id.llDiaryBookItemAct)

                        //橫著塞mood或event的ll
                        var llMoodMyPost = LinearLayout(context);
                        llMoodMyPost.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        var llEventMyPost = LinearLayout(context);
                        llEventMyPost.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                        //mood的名字
                        val moodNamesMyPost = mutableListOf<String>()
                        //mood的score
                        val moodScoresMyPost = mutableListOf<String>()
                        //event的名字
                        val eventNamesMyPost = mutableListOf<String>()


                        val myPostDiary = it.getJSONObject("diary")
                        tvRecordDateMyPost.text = myPostDiary.getString("record_date")
                        tvDiaryNoteMyPost.text = myPostDiary.getString("content")


                        for (i in myPostDiary.getJSONObject("mood").keys()) {
                            moodNamesMyPost.add(i)
                            moodScoresMyPost.add(
                                myPostDiary.getJSONObject("mood").getInt(i).toString()
                            )
                        }

                        for (i in 0 until myPostDiary.getJSONArray("event").length()) {
                            eventNamesMyPost.add(myPostDiary.getJSONArray("event").getString(i))
                        }


                        //裝日記的ll先清空
                        llAlertdialogSquareGetMyPostDiary.removeAllViews()

                        //想辦法動態生成view!!!!!!!!!!!!
                        //當篇日記心情
                        llDiaryBookItemMoodMyPost.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                        for (i in moodNamesMyPost.indices) {
                            if (context == null) {
                                return@runOnUiThread
                            }

                            val viewInside: View = LayoutInflater.from(context)
                                .inflate(R.layout.diary_book_item_mood_item, container, false)

                            val tvDiaryBookItemMoodItemName =
                                viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemName)
                            val tvDiaryBookItemMoodItemScore =
                                viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemScore)
                            val ivDiaryBookItemMoodItemImage =
                                viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemMoodItemImage)
                            tvDiaryBookItemMoodItemName.text = moodNamesMyPost[i]
                            tvDiaryBookItemMoodItemScore.text = moodScoresMyPost[i]

                            //圖示選擇，名稱對照
                            Global.iconPairing(
                                ivDiaryBookItemMoodItemImage,
                                moodPathMap[moodNamesMyPost[i]]!!
                            )

                            //自動換行(心情item塞進橫的ll固定數量自動換塞下一行)
                            llMoodMyPost.addView(viewInside);
                            if (i % 4 == 3 || i == moodNames.size - 1) {
                                llDiaryBookItemMoodMyPost.addView(llMoodMyPost);
                                llMoodMyPost = LinearLayout(context);
                                llMoodMyPost.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            }
                        }

                        //當篇日記活動
                        llDiaryBookItemActMyPost.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                        llEventMyPost.removeAllViews()
                        var currentLength = 0;
                        val maxStringLength = 11;          //設定換行時機 (暫定為11)
                        for (i in eventNamesMyPost.indices) {
                            //資料設定
                            if (context == null) {
                                return@runOnUiThread
                            }
                            val viewInside: View = LayoutInflater.from(context)
                                .inflate(R.layout.diary_book_item_act_item, container, false)

                            val tvDiaryBookItemActItemName =
                                viewInside.findViewById<TextView>(R.id.tvDiaryBookItemActItemName)
                            val ivDiaryBookItemActItemImage =
                                viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemActItemImage)
                            tvDiaryBookItemActItemName.text = eventNamesMyPost[i]


                            //圖示選擇，名稱對照
                            Global.iconPairing(
                                ivDiaryBookItemActItemImage,
                                actPathMap[eventNamesMyPost[i]]!!
                            )

                            //利用字串總長度控制換行時機。若下個活動加上去就會太長則換行
                            if ((eventNamesMyPost[i]?.length
                                    ?: 0) + currentLength <= maxStringLength
                            ) {
                                llEventMyPost.addView(viewInside);
                                currentLength += eventNamesMyPost[i]?.length ?: 0;
                                //剛好是最後一圈則動態新增llEvent
                                if (i == eventNamesMyPost.size - 1) {
                                    llDiaryBookItemActMyPost.addView(llEventMyPost);
                                    currentLength = 0;
                                }
                            } else {
                                llDiaryBookItemActMyPost.addView(llEventMyPost);
                                llEventMyPost = LinearLayout(context);
                                llEventMyPost.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                llEventMyPost.addView(viewInside);
                                currentLength = eventNamesMyPost[i]?.length ?: 0;
                                //剛好是最後一圈則動態新增llEvent
                                if (i == eventNamesMyPost.size - 1) {
                                    llDiaryBookItemActMyPost.addView(llEventMyPost);
                                    currentLength = 0;
                                }
                            }
                        }

                        //裝日記的ll裝入diaryView
                        llAlertdialogSquareGetMyPostDiary.addView(myPostDiaryView)
                    }
                }
            }.onFailure {}.onComplete {}.exec()
        }

        //刷新貼文
        tvSquareRefresh.setOnClickListener {

            NetworkController.chatSquareAll(token!!).onResponse {
                //印出收到的東西
                Log.d("square", "squarePosts: $it")

                activity?.runOnUiThread {
                    squarePosts = it.getJSONArray("square")
                    tvSquarePostNum.text = "無貼文"
                    //如果有貼文才做事
                    if (squarePosts.length() > 0) {
                        tvSquarePostNum.text = "第${postNum + 1}篇，共${squarePosts.length()}篇"
                        val firstPost = squarePosts.getJSONObject(0)
                        tvSquareHostName.text = firstPost.getString("hostName")
                        tvSquarePostDate.text = firstPost.getString("postDate")
                        tvSquarePost.text = firstPost.getString("post")
                        diaryId = firstPost.getInt("diaryId")
                        diary = firstPost.getJSONObject("diary")

                        //日記內容...
                        //日記日期
                        tvRecordDate.text = diary.getString("record_date")
                        //心情各陣列清空
                        moodNames.clear()
                        moodIcons.clear()
                        moodScores.clear()
                        //填入心情各值
                        val moodsJsonObject = diary.getJSONObject("mood")
                        for (i in moodsJsonObject.keys()) {
                            val moodJsonObject = moodsJsonObject.getJSONObject(i)
                            moodNames.add(i)
                            moodIcons.add(moodJsonObject.getInt("icon").toString())
                            moodScores.add(moodJsonObject.getInt("score").toString())
                        }
                        //活動各陣列清空
                        eventNames.clear()
                        eventIcons.clear()
                        //填入活動各值
                        val eventsJsonObject = diary.getJSONObject("event")
                        for (i in eventsJsonObject.keys()) {
                            eventNames.add(i)
                            eventIcons.add(eventsJsonObject.getInt(i).toString())
                        }
                        //日記備註
                        tvDiaryNote.text = diary.getString("content")

                        //裝日記的ll先清空
                        llSquareDiary.removeAllViews()

                        //想辦法動態生成view!!!!!!!!!!!!
                        //當篇日記心情
                        llDiaryBookItemMood.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                        for (i in moodNames.indices) {
                            if (context == null) {
                                return@runOnUiThread
                            }

                            val viewInside: View = LayoutInflater.from(context)
                                .inflate(R.layout.diary_book_item_mood_item, container, false)

                            val tvDiaryBookItemMoodItemName =
                                viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemName)
                            val tvDiaryBookItemMoodItemScore =
                                viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemScore)
                            val ivDiaryBookItemMoodItemImage =
                                viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemMoodItemImage)
                            tvDiaryBookItemMoodItemName.text = moodNames[i]
                            tvDiaryBookItemMoodItemScore.text = moodScores[i]

                            //圖示選擇，名稱對照
                            Global.iconPairing(
                                ivDiaryBookItemMoodItemImage,
                                moodIcons[i]
                            )

                            //自動換行(心情item塞進橫的ll固定數量自動換塞下一行)
                            llMood.addView(viewInside);
                            if (i % 4 == 3 || i == moodNames.size - 1) {
                                llDiaryBookItemMood.addView(llMood);
                                llMood = LinearLayout(context);
                                llMood.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            }
                        }

                        //當篇日記活動
                        llDiaryBookItemAct.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                        llEvent.removeAllViews()
                        var currentLength = 0;
                        val maxStringLength = 11;          //設定換行時機 (暫定為11)
                        for (i in eventNames.indices) {
                            //資料設定
                            if (context == null) {
                                return@runOnUiThread
                            }
                            val viewInside: View = LayoutInflater.from(context)
                                .inflate(R.layout.diary_book_item_act_item, container, false)

                            val tvDiaryBookItemActItemName =
                                viewInside.findViewById<TextView>(R.id.tvDiaryBookItemActItemName)
                            val ivDiaryBookItemActItemImage =
                                viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemActItemImage)
                            tvDiaryBookItemActItemName.text = eventNames[i]


                            //圖示選擇，名稱對照
                            Global.iconPairing(
                                ivDiaryBookItemActItemImage,
                                eventIcons[i]
                            )

                            //利用字串總長度控制換行時機。若下個活動加上去就會太長則換行
                            if ((eventNames[i]?.length
                                    ?: 0) + currentLength <= maxStringLength
                            ) {
                                llEvent.addView(viewInside);
                                currentLength += eventNames[i]?.length ?: 0;
                                //剛好是最後一圈則動態新增llEvent
                                if (i == eventNames.size - 1) {
                                    llDiaryBookItemAct.addView(llEvent);
                                    currentLength = 0;
                                }
                            } else {
                                llDiaryBookItemAct.addView(llEvent);
                                llEvent = LinearLayout(context);
                                llEvent.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                llEvent.addView(viewInside);
                                currentLength = eventNames[i]?.length ?: 0;
                                //剛好是最後一圈則動態新增llEvent
                                if (i == eventNames.size - 1) {
                                    llDiaryBookItemAct.addView(llEvent);
                                    currentLength = 0;
                                }
                            }
                        }

                        //裝日記的ll裝入diaryView
                        llSquareDiary.addView(diaryView)
                    }


                }
            }.onFailure {}.onComplete {}.exec()

            postNum = 0
            tvSquarePostNum.text = "無貼文"//不知道為何前面參數都不會重設
            if (squarePosts.length() > 0) {
                tvSquarePostNum.text = "第${postNum + 1}篇，共${squarePosts.length()}篇"//不知道為何前面參數都不會重設
                val post = squarePosts.getJSONObject(postNum)
                tvSquareHostName.text = post.getString("hostName")
                tvSquarePostDate.text = post.getString("postDate")
                tvSquarePost.text = post.getString("post")
                diaryId = post.getInt("diaryId")
                diary = post.getJSONObject("diary")
            }

            Toast.makeText(
                context,
                "刷新成功",
                Toast.LENGTH_SHORT
            ).show()
        }

        //下一篇貼文
        tvSquareNext.setOnClickListener {
            when {
                squarePosts.length() == 0 -> {
                    Toast.makeText(
                        context,
                        "目前還沒有貼文喔，快去分享吧！",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                postNum == (squarePosts.length() - 1) -> {
                    Toast.makeText(
                        context,
                        "無更多貼文",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    postNum += 1
                    tvSquarePostNum.text = "第${postNum + 1}篇，共${squarePosts.length()}篇"
                    val post = squarePosts.getJSONObject(postNum)
                    tvSquareHostName.text = post.getString("hostName")
                    tvSquarePostDate.text = post.getString("postDate")
                    tvSquarePost.text = post.getString("post")
                    diaryId = post.getInt("diaryId")
                    diary = post.getJSONObject("diary")

                    //日記內容...
                    //日記日期
                    tvRecordDate.text = diary.getString("record_date")
                    //心情各陣列清空
                    moodNames.clear()
                    moodIcons.clear()
                    moodScores.clear()
                    //填入心情各值
                    val moodsJsonObject = diary.getJSONObject("mood")
                    for (i in moodsJsonObject.keys()) {
                        val moodJsonObject = moodsJsonObject.getJSONObject(i)
                        moodNames.add(i)
                        moodIcons.add(moodJsonObject.getInt("icon").toString())
                        moodScores.add(moodJsonObject.getInt("score").toString())
                    }
                    //活動各陣列清空
                    eventNames.clear()
                    eventIcons.clear()
                    //填入活動各值
                    val eventsJsonObject = diary.getJSONObject("event")
                    for (i in eventsJsonObject.keys()) {
                        eventNames.add(i)
                        eventIcons.add(eventsJsonObject.getInt(i).toString())
                    }
                    //日記備註
                    tvDiaryNote.text = diary.getString("content")

                    //裝日記的ll先清空
                    llSquareDiary.removeAllViews()

                    //想辦法動態生成view!!!!!!!!!!!!
                    //當篇日記心情
                    llDiaryBookItemMood.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                    for (i in moodNames.indices) {

                        val viewInside: View = LayoutInflater.from(context)
                            .inflate(R.layout.diary_book_item_mood_item, container, false)

                        val tvDiaryBookItemMoodItemName =
                            viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemName)
                        val tvDiaryBookItemMoodItemScore =
                            viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemScore)
                        val ivDiaryBookItemMoodItemImage =
                            viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemMoodItemImage)
                        tvDiaryBookItemMoodItemName.text = moodNames[i]
                        tvDiaryBookItemMoodItemScore.text = moodScores[i]

                        //圖示選擇，名稱對照
                        Global.iconPairing(
                            ivDiaryBookItemMoodItemImage,
                            moodIcons[i]
                        )

                        //自動換行(心情item塞進橫的ll固定數量自動換塞下一行)
                        llMood.addView(viewInside);
                        if (i % 4 == 3 || i == moodNames.size - 1) {
                            llDiaryBookItemMood.addView(llMood);
                            llMood = LinearLayout(context);
                            llMood.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }

                    //當篇日記活動
                    llDiaryBookItemAct.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                    llEvent.removeAllViews()
                    var currentLength = 0;
                    val maxStringLength = 11;          //設定換行時機 (暫定為11)
                    for (i in eventNames.indices) {
                        //資料設定

                        val viewInside: View = LayoutInflater.from(context)
                            .inflate(R.layout.diary_book_item_act_item, container, false)

                        val tvDiaryBookItemActItemName =
                            viewInside.findViewById<TextView>(R.id.tvDiaryBookItemActItemName)
                        val ivDiaryBookItemActItemImage =
                            viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemActItemImage)
                        tvDiaryBookItemActItemName.text = eventNames[i]


                        //圖示選擇，名稱對照
                        Global.iconPairing(
                            ivDiaryBookItemActItemImage,
                            eventIcons[i]
                        )

                        //利用字串總長度控制換行時機。若下個活動加上去就會太長則換行
                        if ((eventNames[i]?.length
                                ?: 0) + currentLength <= maxStringLength
                        ) {
                            llEvent.addView(viewInside);
                            currentLength += eventNames[i]?.length ?: 0;
                            //剛好是最後一圈則動態新增llEvent
                            if (i == eventNames.size - 1) {
                                llDiaryBookItemAct.addView(llEvent);
                                currentLength = 0;
                            }
                        } else {
                            llDiaryBookItemAct.addView(llEvent);
                            llEvent = LinearLayout(context);
                            llEvent.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            llEvent.addView(viewInside);
                            currentLength = eventNames[i]?.length ?: 0;
                            //剛好是最後一圈則動態新增llEvent
                            if (i == eventNames.size - 1) {
                                llDiaryBookItemAct.addView(llEvent);
                                currentLength = 0;
                            }
                        }
                    }

                    //裝日記的ll裝入diaryView
                    llSquareDiary.addView(diaryView)


                }
            }
        }

        //上一篇貼文
        tvSquarePrior.setOnClickListener {
            when {
                squarePosts.length() == 0 -> {
                    Toast.makeText(
                        context,
                        "目前還沒有貼文喔，快去分享吧！",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                postNum == 0 -> {
                    Toast.makeText(
                        context,
                        "已經是第一篇貼文",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    postNum -= 1
                    tvSquarePostNum.text = "第${postNum + 1}篇，共${squarePosts.length()}篇"
                    val post = squarePosts.getJSONObject(postNum)
                    tvSquareHostName.text = post.getString("hostName")
                    tvSquarePostDate.text = post.getString("postDate")
                    tvSquarePost.text = post.getString("post")
                    diaryId = post.getInt("diaryId")
                    diary = post.getJSONObject("diary")

                    //日記內容...
                    //日記日期
                    tvRecordDate.text = diary.getString("record_date")
                    //心情各陣列清空
                    moodNames.clear()
                    moodIcons.clear()
                    moodScores.clear()
                    //填入心情各值
                    val moodsJsonObject = diary.getJSONObject("mood")
                    for (i in moodsJsonObject.keys()) {
                        val moodJsonObject = moodsJsonObject.getJSONObject(i)
                        moodNames.add(i)
                        moodIcons.add(moodJsonObject.getInt("icon").toString())
                        moodScores.add(moodJsonObject.getInt("score").toString())
                    }
                    //活動各陣列清空
                    eventNames.clear()
                    eventIcons.clear()
                    //填入活動各值
                    val eventsJsonObject = diary.getJSONObject("event")
                    for (i in eventsJsonObject.keys()) {
                        eventNames.add(i)
                        eventIcons.add(eventsJsonObject.getInt(i).toString())
                    }
                    //日記備註
                    tvDiaryNote.text = diary.getString("content")

                    //裝日記的ll先清空
                    llSquareDiary.removeAllViews()

                    //想辦法動態生成view!!!!!!!!!!!!
                    //當篇日記心情
                    llDiaryBookItemMood.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                    for (i in moodNames.indices) {

                        val viewInside: View = LayoutInflater.from(context)
                            .inflate(R.layout.diary_book_item_mood_item, container, false)

                        val tvDiaryBookItemMoodItemName =
                            viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemName)
                        val tvDiaryBookItemMoodItemScore =
                            viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemScore)
                        val ivDiaryBookItemMoodItemImage =
                            viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemMoodItemImage)
                        tvDiaryBookItemMoodItemName.text = moodNames[i]
                        tvDiaryBookItemMoodItemScore.text = moodScores[i]

                        //圖示選擇，名稱對照
                        Global.iconPairing(
                            ivDiaryBookItemMoodItemImage,
                            moodIcons[i]
                        )

                        //自動換行(心情item塞進橫的ll固定數量自動換塞下一行)
                        llMood.addView(viewInside);
                        if (i % 4 == 3 || i == moodNames.size - 1) {
                            llDiaryBookItemMood.addView(llMood);
                            llMood = LinearLayout(context);
                            llMood.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }

                    //當篇日記活動
                    llDiaryBookItemAct.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                    llEvent.removeAllViews()
                    var currentLength = 0;
                    val maxStringLength = 11;          //設定換行時機 (暫定為11)
                    for (i in eventNames.indices) {
                        //資料設定

                        val viewInside: View = LayoutInflater.from(context)
                            .inflate(R.layout.diary_book_item_act_item, container, false)

                        val tvDiaryBookItemActItemName =
                            viewInside.findViewById<TextView>(R.id.tvDiaryBookItemActItemName)
                        val ivDiaryBookItemActItemImage =
                            viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemActItemImage)
                        tvDiaryBookItemActItemName.text = eventNames[i]


                        //圖示選擇，名稱對照
                        Global.iconPairing(
                            ivDiaryBookItemActItemImage,
                            eventIcons[i]
                        )

                        //利用字串總長度控制換行時機。若下個活動加上去就會太長則換行
                        if ((eventNames[i]?.length
                                ?: 0) + currentLength <= maxStringLength
                        ) {
                            llEvent.addView(viewInside);
                            currentLength += eventNames[i]?.length ?: 0;
                            //剛好是最後一圈則動態新增llEvent
                            if (i == eventNames.size - 1) {
                                llDiaryBookItemAct.addView(llEvent);
                                currentLength = 0;
                            }
                        } else {
                            llDiaryBookItemAct.addView(llEvent);
                            llEvent = LinearLayout(context);
                            llEvent.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            llEvent.addView(viewInside);
                            currentLength = eventNames[i]?.length ?: 0;
                            //剛好是最後一圈則動態新增llEvent
                            if (i == eventNames.size - 1) {
                                llDiaryBookItemAct.addView(llEvent);
                                currentLength = 0;
                            }
                        }
                    }

                    //裝日記的ll裝入diaryView
                    llSquareDiary.addView(diaryView)
                }
            }
        }

        //網路取得全po文資料
        NetworkController.chatSquareAll(token!!).onResponse {
            //印出收到的東西
            Log.d("square", "squarePosts: $it")

            activity?.runOnUiThread {
                squarePosts = it.getJSONArray("square")
                tvSquarePostNum.text = "無貼文"
                //如果有貼文才做事
                if (squarePosts.length() > 0) {
                    tvSquarePostNum.text = "第${postNum + 1}篇，共${squarePosts.length()}篇"
                    val firstPost = squarePosts.getJSONObject(0)
                    tvSquareHostName.text = firstPost.getString("hostName")
                    tvSquarePostDate.text = firstPost.getString("postDate")
                    tvSquarePost.text = firstPost.getString("post")
                    diaryId = firstPost.getInt("diaryId")
                    diary = firstPost.getJSONObject("diary")

                    //日記內容...
                    //日記日期
                    tvRecordDate.text = diary.getString("record_date")
                    //心情各陣列清空
                    moodNames.clear()
                    moodIcons.clear()
                    moodScores.clear()
                    //填入心情各值
                    val moodsJsonObject = diary.getJSONObject("mood")
                    for (i in moodsJsonObject.keys()) {
                        val moodJsonObject = moodsJsonObject.getJSONObject(i)
                        moodNames.add(i)
                        moodIcons.add(moodJsonObject.getInt("icon").toString())
                        moodScores.add(moodJsonObject.getInt("score").toString())
                    }
                    //活動各陣列清空
                    eventNames.clear()
                    eventIcons.clear()
                    //填入活動各值
                    val eventsJsonObject = diary.getJSONObject("event")
                    for (i in eventsJsonObject.keys()) {
                        eventNames.add(i)
                        eventIcons.add(eventsJsonObject.getInt(i).toString())
                    }
                    //日記備註
                    tvDiaryNote.text = diary.getString("content")

                    //裝日記的ll先清空
                    llSquareDiary.removeAllViews()

                    //想辦法動態生成view!!!!!!!!!!!!
                    //當篇日記心情
                    llDiaryBookItemMood.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                    for (i in moodNames.indices) {
                        if (context == null) {
                            return@runOnUiThread
                        }

                        val viewInside: View = LayoutInflater.from(context)
                            .inflate(R.layout.diary_book_item_mood_item, container, false)

                        val tvDiaryBookItemMoodItemName =
                            viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemName)
                        val tvDiaryBookItemMoodItemScore =
                            viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemScore)
                        val ivDiaryBookItemMoodItemImage =
                            viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemMoodItemImage)
                        tvDiaryBookItemMoodItemName.text = moodNames[i]
                        tvDiaryBookItemMoodItemScore.text = moodScores[i]

                        //圖示選擇，名稱對照
                        Global.iconPairing(
                            ivDiaryBookItemMoodItemImage,
                            moodIcons[i]
                        )

                        //自動換行(心情item塞進橫的ll固定數量自動換塞下一行)
                        llMood.addView(viewInside);
                        if (i % 4 == 3 || i == moodNames.size - 1) {
                            llDiaryBookItemMood.addView(llMood);
                            llMood = LinearLayout(context);
                            llMood.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }

                    //當篇日記活動
                    llDiaryBookItemAct.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                    llEvent.removeAllViews()
                    var currentLength = 0;
                    val maxStringLength = 11;          //設定換行時機 (暫定為11)
                    for (i in eventNames.indices) {
                        //資料設定
                        if (context == null) {
                            return@runOnUiThread
                        }
                        val viewInside: View = LayoutInflater.from(context)
                            .inflate(R.layout.diary_book_item_act_item, container, false)

                        val tvDiaryBookItemActItemName =
                            viewInside.findViewById<TextView>(R.id.tvDiaryBookItemActItemName)
                        val ivDiaryBookItemActItemImage =
                            viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemActItemImage)
                        tvDiaryBookItemActItemName.text = eventNames[i]

                        //圖示選擇，名稱對照
                        Global.iconPairing(
                            ivDiaryBookItemActItemImage,
                            eventIcons[i]
                        )

                        //利用字串總長度控制換行時機。若下個活動加上去就會太長則換行
                        if ((eventNames[i]?.length
                                ?: 0) + currentLength <= maxStringLength
                        ) {
                            llEvent.addView(viewInside);
                            currentLength += eventNames[i]?.length ?: 0;
                            //剛好是最後一圈則動態新增llEvent
                            if (i == eventNames.size - 1) {
                                llDiaryBookItemAct.addView(llEvent);
                                currentLength = 0;
                            }
                        } else {
                            llDiaryBookItemAct.addView(llEvent);
                            llEvent = LinearLayout(context);
                            llEvent.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            llEvent.addView(viewInside);
                            currentLength = eventNames[i]?.length ?: 0;
                            //剛好是最後一圈則動態新增llEvent
                            if (i == eventNames.size - 1) {
                                llDiaryBookItemAct.addView(llEvent);
                                currentLength = 0;
                            }
                        }
                    }

                    //裝日記的ll裝入diaryView
                    llSquareDiary.addView(diaryView)
                }
            }
        }.onFailure {}.onComplete {}.exec()
        return fragmentView
    }

    companion object {
        private const val NICK_NAME = "NICK_NAME"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SquareFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic

        fun newInstance(param1: String, param2: String) =
            SquareFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}