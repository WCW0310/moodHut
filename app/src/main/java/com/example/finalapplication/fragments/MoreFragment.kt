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
import com.example.finalapplication.EditActTypeActivity
import com.example.finalapplication.EditMoodActivity
import com.example.finalapplication.R
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.NetworkController
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MoreFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        //要傳出去的東西、要使用的東西
        //把傳過來的TOKEN撈出來
        val sharedPreferences = activity?.getSharedPreferences(
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
        val tvMoreEditMood = view.findViewById<TextView>(R.id.tvMoreEditMood)
        val tvMoreEditEvent = view.findViewById<TextView>(R.id.tvMoreEditEvent)
        val tvMoreMyPost = view.findViewById<TextView>(R.id.tvMoreMyPost)

        //前往更改心情頁面
        tvMoreEditMood.setOnClickListener {
            val intent = Intent(view.context, EditMoodActivity::class.java)
            startActivity(intent)
        }

        //前往更改活動頁面
        tvMoreEditEvent.setOnClickListener {
            val intent = Intent(view.context, EditActTypeActivity::class.java)
            startActivity(intent)
        }

        //mood的名字
        val moodNames = mutableListOf<String>()
        tvMoreMyPost.setOnClickListener {
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

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MoreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MoreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}