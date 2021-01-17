package com.example.finalapplication.fragments

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.*
import com.example.finalapplication.items.DiaryItem
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.adapters.CommonAdapter
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.FIRST_DIARY_DATE
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.Global.fillPathMap
import com.example.finalapplication.utils.NetworkController
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DiaryBookFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiaryBookFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Log.d("diaryBook", "DiaryBook!!!!!!")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //要先宣告view變數
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_diary_book, container, false)
        //再用view變數找其他元件
        //要傳出去的東西、要使用的東西
        //把傳過來的TOKEN撈出來
        val sharedPreferences = activity?.getSharedPreferences(DATA, MODE_PRIVATE)//好像是特殊用法
        val token = sharedPreferences?.getString(TOKEN, "")
        val moodAndEvent = sharedPreferences?.getString(MOOD_AND_EVENT, "")
        val moodAndEventJson = JSONObject(moodAndEvent!!)
        //心情圖示配對
        val moodPathMap: MutableMap<String, String> = mutableMapOf()

        //活動圖示配對
        val actPathMap: MutableMap<String, String> = mutableMapOf()

        //填入心情與活動的圖示配對
        fillPathMap(moodAndEventJson, moodPathMap, actPathMap)


        //新增日記按鈕，切換至新增日記心情頁面
        val llAddDiary = view.findViewById<LinearLayout>(R.id.llAddDiary)
        llAddDiary!!.setOnClickListener {
            val intent = Intent(activity, AddDiaryMoodActivity::class.java)
            startActivity(intent)
            activity!!.finish()
        }

        //日記recyclerView
        val rvDiary = view.findViewById<RecyclerView>(R.id.rvDiary)
        //設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = LinearLayoutManager(activity)
        //設定layoutManager進去rv讓他們有關聯
        rvDiary.layoutManager = layoutManager
        //recyclerView設定區
        val adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.diary_book_item, it, false)

                //要綁資料的話宣告在這裡
                val tvRecordDate = view.findViewById<TextView>(R.id.tvRecordDate)
                val tvDiaryNote = view.findViewById<TextView>(R.id.tvDiaryNote)
                //要動態生成活動物件的ll
                val llDiaryBookItemMood = view.findViewById<LinearLayout>(R.id.llDiaryBookItemMood)
                val llDiaryBookItemAct = view.findViewById<LinearLayout>(R.id.llDiaryBookItemAct)

                //橫著塞mood或event的ll
                var llMood = LinearLayout(context);
                llMood.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

                var llEvent = LinearLayout(context);
                llEvent.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                return@Factory object : BaseViewHolder<DiaryItem>(view) {

                    override fun bind(item: DiaryItem) {

                        //日記的點擊事件，能更改或刪除日記。後續心情與活動部分的監聽會被覆寫
                        view.setOnClickListener { diaryView: View ->
                            pop(
                                diaryView,
                                item,
                                sharedPreferences
                            )
                        }

                        tvRecordDate.text = item.recordDate
                        //日記備註會自己換行
                        tvDiaryNote.text = item.content
                        //想辦法動態生成view!!!!!!!!!!!!
                        //當篇日記心情
                        llDiaryBookItemMood.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                        for (i in item.moodNameArray.indices) {

                            val viewInside: View = LayoutInflater.from(it?.context)
                                .inflate(R.layout.diary_book_item_mood_item, it, false)

                            val tvDiaryBookItemMoodItemName =
                                viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemName)
                            val tvDiaryBookItemMoodItemScore =
                                viewInside.findViewById<TextView>(R.id.tvDiaryBookItemMoodItemScore)
                            val ivDiaryBookItemMoodItemImage =
                                viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemMoodItemImage)
                            tvDiaryBookItemMoodItemName.text = item.moodNameArray[i]
                            tvDiaryBookItemMoodItemScore.text = item.moodScoreArray[i]

                            //圖示選擇，名稱對照
                            Global.iconPairing(
                                ivDiaryBookItemMoodItemImage,
                                moodPathMap[item.moodNameArray[i]]!!
                            )

                            //把viewInside加進去, 需要設定weight(動態生成的預設weight會失效)
                            llMood.addView(
                                viewInside, LinearLayout.LayoutParams(
                                    0,
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    0.25f
                                )
                            );

                            //
                            if (i % 4 == 3) {
                                llDiaryBookItemMood.addView(llMood);

                                llMood = LinearLayout(context);
                                llMood.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                            }

                            //若是迴圈最後一圈則補上空白區塊
                            if (i == item.moodNameArray.size - 1 && (i % 4) != 3) {
                                val blankMood = LayoutInflater.from(it?.context)
                                    .inflate(R.layout.blank_diary_mood, it, false);
                                llMood.addView(
                                    blankMood, LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        (0.75f - i * 0.25f)
                                    )
                                );

                                llDiaryBookItemMood.addView(llMood);
                                llMood = LinearLayout(context);
                                llMood.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                            }
                        }

                        //當篇日記活動
                        llDiaryBookItemAct.removeAllViews()//先清空，不然滑動RecyclerView回來時會再填入一次
                        llEvent.removeAllViews()
                        var currentLength = 0;
                        val maxStringLength = 11;          //設定換行時機 (暫定為11)
                        for (i in item.eventArray.indices) {
                            //資料設定
                            val viewInside: View = LayoutInflater.from(it?.context)
                                .inflate(R.layout.diary_book_item_act_item, it, false)

                            val tvDiaryBookItemActItemName =
                                viewInside.findViewById<TextView>(R.id.tvDiaryBookItemActItemName)
                            val ivDiaryBookItemActItemImage =
                                viewInside.findViewById<ImageView>(R.id.ivDiaryBookItemActItemImage)
                            tvDiaryBookItemActItemName.text = item.eventArray[i]

                            //圖示選擇，名稱對照
                            Global.iconPairing(
                                ivDiaryBookItemActItemImage,
                                actPathMap[item.eventArray[i]]!!
                            )

                            //利用字串總長度控制換行時機。若下個活動加上去就會太長則換行
                            if ((item.eventArray[i]?.length
                                    ?: 0) + currentLength <= maxStringLength
                            ) {
                                llEvent.addView(viewInside);
                                currentLength += item.eventArray[i]?.length ?: 0;
                                //剛好是最後一圈則動態新增llEvent
                                if (i == item.eventArray.size - 1) {
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
                                currentLength = item.eventArray[i]?.length ?: 0;
                                //剛好是最後一圈則動態新增llEvent
                                if (i == item.eventArray.size - 1) {
                                    llDiaryBookItemAct.addView(llEvent);
                                    currentLength = 0;
                                }
                            }
                        }
                    }
                }
            }, type = DiaryItem.TYPE).build()
        rvDiary.adapter = adapter
        adapter.clear()//開始讀之前先清空


        //網路取得日記資料
        NetworkController.getAllDiary(token!!).onResponse {
            //印出收到的東西
            Log.d("diaryBook", "diaries: $it")

            activity?.runOnUiThread {
                val diaryArray = it.getJSONArray("diary")
                //存入第一篇日記的DATE
                if (diaryArray.length() > 0) {
                    sharedPreferences.edit()
                        .putString(
                            FIRST_DIARY_DATE,
                            diaryArray.getJSONObject(0).getString("recordDate")
                        )
                        .apply()
                } else {
                    sharedPreferences.edit()
                        .putString(
                            FIRST_DIARY_DATE,
                            ""
                        )
                        .apply()
                }

                //將DiaryItem加入adapter
                //日記新顯示到舊
                //倒著輸出時(diaryArray.length() - 1 downTo 0)、順著輸出時(0 until diaryArray.length())
                for (i in diaryArray.length() - 1 downTo 0) {
                    val moodLength = diaryArray.getJSONObject(i).getJSONObject("mood").length()
                    val eventLength = diaryArray.getJSONObject(i).getJSONArray("event").length()
                    val diaryId = diaryArray.getJSONObject(i).getString("diaryId")
                    val recordDate = diaryArray.getJSONObject(i).getString("recordDate")
                    val moodNameArray: Array<String?> = arrayOfNulls(moodLength)
                    val moodScoreArray: Array<String?> = arrayOfNulls(moodLength)
                    val eventArray: Array<String?> = arrayOfNulls(eventLength)
                    val content = diaryArray.getJSONObject(i).getString("content")

                    //填入moodNameArray內容
                    //填入moodScoreArray內容
                    val moodNames = diaryArray.getJSONObject(i).getJSONObject("mood").keys()
                    for (name in 0 until moodLength) {
                        var moodName = moodNames.next()
                        moodNameArray[name] = moodName
                        moodScoreArray[name] =
                            diaryArray.getJSONObject(i).getJSONObject("mood").getInt(moodName)
                                .toString()
                    }

                    //填入eventArray內容
                    for (event in 0 until eventLength) {
                        eventArray[event] =
                            diaryArray.getJSONObject(i).getJSONArray("event").getString(event)
                    }

                    val item =
                        DiaryItem(
                            diaryId,
                            recordDate,
                            moodNameArray,
                            moodScoreArray,
                            eventArray,
                            content
                        )
                    adapter.add(item)
                }
            }
        }.onFailure {}.onComplete {}.exec()

        return view
    }

    companion object {
        private const val DIARY_ID = "DIARY_ID"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DiaryBookFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DiaryBookFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //彈出更改或刪除視窗
    private fun pop(view: View, diaryItem: DiaryItem, sharedPreferences: SharedPreferences) {
        Log.d("yamiew", "pop is running");

        //PopupMenu物件的佈局檔
        val popupMenu = PopupMenu(context, view);
        popupMenu.inflate(R.menu.diarybook_menu);

        //點擊事件
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                //更改與刪除事件
                R.id.diaryBook_modify -> {
                    //傳出當篇日記的內容
                    val intent = Intent(view.context, UpdateDiaryActivity::class.java)
                    val bundle = Bundle()
//            bundle.clear()
                    bundle.putString(DIARY_ID, diaryItem.diaryId)
                    intent.putExtras(bundle)

                    startActivity(intent)
                    activity!!.finish()

                    true
                }
                R.id.diaryBook_delete -> {
                    //用dialog時，context要用對，不然程式會死
                    val alertDialogContentViewInside =
                        LayoutInflater.from(view.context)
                            .inflate(R.layout.alertdialog_delete_mood, null)
                    val tvAlertdialogDeleteMoodHint =
                        alertDialogContentViewInside.findViewById<TextView>(R.id.tvAlertdialogDeleteMoodHint)
                    val btnAlertdialogDeleteMoodConfirm =
                        alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertdialogDeleteMoodConfirm)
                    val btnAlertdialogDeleteMoodCancel =
                        alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertdialogDeleteMoodCancel)

                    tvAlertdialogDeleteMoodHint.text = "確定要刪除這篇日記嗎?"

                    val alertDialogDeleteMood =
                        AlertDialog.Builder(view.context)
                            .setView(alertDialogContentViewInside)
                            .setCancelable(false)//避免別人填東西過程中被突然取消
                            .show()

                    btnAlertdialogDeleteMoodCancel.setOnClickListener {
                        alertDialogDeleteMood.dismiss()
                    }

                    btnAlertdialogDeleteMoodConfirm.setOnClickListener {
                        Log.d(
                            "deleteDiary",
                            "userId: ${sharedPreferences.getString(TOKEN, "")}"
                        )
                        Log.d(
                            "deleteDiary",
                            "diaryId: ${diaryItem.diaryId}"
                        )

                        //呼叫刪除API
                        NetworkController.modifyDiaryDelete(
                            diaryItem.diaryId!!.toInt()
                        ).onResponse {
                            //印出收到的東西
                            Log.d(
                                "deleteDiary",
                                "deleteDiaryResponse: $it"
                            )

                            activity?.runOnUiThread {
                                //可以丟Toast訊息告知心情刪除成功
                                Toast.makeText(
                                    view.context,
                                    "日記刪除成功",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(
                                    activity,
                                    BottomNavigationActivity::class.java
                                )
                                startActivity(intent)
                                activity!!.finish()
                            }
                        }.onFailure {}.onComplete {}.exec()

                        alertDialogDeleteMood.dismiss()
                    }
                    true
                }
                R.id.diaryBook_share -> {
                    //用dialog時，context要用對，不然程式會死
                    val alertDialogContentViewInside =
                        LayoutInflater.from(view.context)
                            .inflate(R.layout.alertdialog_share_diary, null)
                    val etAlertDialogShareDiaryNickName =
                        alertDialogContentViewInside.findViewById<EditText>(R.id.etAlertDialogShareDiaryNickName)
                    val etAlertDialogShareDiaryPostContent =
                        alertDialogContentViewInside.findViewById<EditText>(R.id.etAlertDialogShareDiaryPostContent)
                    val btnAlertDialogShareDiaryShare =
                        alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertDialogShareDiaryShare)
                    val btnAlertDialogShareDiaryCancel =
                        alertDialogContentViewInside.findViewById<Button>(R.id.btnAlertDialogShareDiaryCancel)


                    val alertDialogShareDiary =
                        AlertDialog.Builder(view.context)
                            .setView(alertDialogContentViewInside)
                            .setCancelable(false)//避免別人填東西過程中被突然取消
                            .show()

                    btnAlertDialogShareDiaryCancel.setOnClickListener {
                        alertDialogShareDiary.dismiss()
                    }

                    btnAlertDialogShareDiaryShare.setOnClickListener {
                        //若暱稱超過10字跳通知，結束
                        if (etAlertDialogShareDiaryNickName.text.length > 10) {
                            Toast.makeText(
                                activity!!.applicationContext,
                                "暱稱字數過長",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }

                        //po文時間即現在時間，其實應該跟訊息一樣由後端決定
                        val calendar =
                            Calendar.getInstance()//1970年1月1日開始計算到目前為止的格林威治標準時間的milliseconds，所以不管在哪一個時區所儲存的時間都是一樣的
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH) + 1
                        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)

                        NetworkController.chatSquarePost(
                            etAlertDialogShareDiaryNickName.text.toString(),
                            "$year-$month-$dayOfMonth $hourOfDay:$minute",
                            etAlertDialogShareDiaryPostContent.text.toString(),
                            diaryItem.diaryId!!
                        ).onResponse {
                            //印出收到的東西
                            Log.d(
                                "shareDiary",
                                "shareDiaryResponse: $it"
                            )

                            activity?.runOnUiThread {
                                val msg = it.getString("msg")
                                if (msg == "success") {
                                    Toast.makeText(
                                        view.context,
                                        "日記分享成功",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        view.context,
                                        "一次只能有一篇分享的日記",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.onFailure {}.onComplete {}.exec()

                        alertDialogShareDiary.dismiss()
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
        popupMenu.show();
    }
}