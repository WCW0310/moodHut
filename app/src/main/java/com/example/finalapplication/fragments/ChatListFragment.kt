package com.example.finalapplication.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.ChatRoomActivity
import com.example.finalapplication.R
import com.example.finalapplication.items.ChatListItem
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.CHAT_ROOM_ID
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.MY_NAME
import com.example.finalapplication.utils.Global.OTHERS_NAME
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.NetworkController
import com.example.finalapplication.utils.adapters.CommonAdapter
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListFragment : Fragment() {
    //測試
    lateinit var token: String;
    lateinit var adapter: CommonAdapter;

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
        val chatListView = inflater.inflate(R.layout.fragment_chat_list, container, false)

//再用view變數找其他元件
        //要傳出去的東西、要使用的東西
        //把傳過來的TOKEN撈出來
        val sharedPreferences = activity?.getSharedPreferences(
            DATA,
            Context.MODE_PRIVATE
        )//好像是特殊用法
        token = sharedPreferences?.getString(TOKEN, "") ?: "";
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


//聊天室recyclerView
        val rvChatList = chatListView.findViewById<RecyclerView>(R.id.rvChatList)
        //設定佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = LinearLayoutManager(activity)
        //設定layoutManager進去rv讓他們有關聯
        rvChatList.layoutManager = layoutManager
        //recyclerView設定區
        adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chat_list_item, it, false)

                //要綁資料的話宣告在這裡
                val tvChatListItemOthersName =
                    view.findViewById<TextView>(R.id.tvChatListItemOthersName)
                val tvChatListItemLatestMsg =
                    view.findViewById<TextView>(R.id.tvChatListItemLatestMsg)
                val tvChatListItemLatestTime =
                    view.findViewById<TextView>(R.id.tvChatListItemLatestTime)

                return@Factory object : BaseViewHolder<ChatListItem>(view) {
                    override fun bind(item: ChatListItem) {
                        tvChatListItemOthersName.text = item.othersName
                        tvChatListItemLatestMsg.text = item.latestMsg
                        tvChatListItemLatestTime.text = item.latestTime

                        //點擊後進入特定聊天室
                        view.setOnClickListener {
                            //將chatRoomId、myName、othersName帶到聊天室頁面
                            val intent = Intent(view.context, ChatRoomActivity::class.java)
                            val bundle = Bundle()
//            bundle.clear()
                            bundle.putInt(CHAT_ROOM_ID, item.chatroomId)
                            bundle.putString(MY_NAME, item.myName)
                            bundle.putString(OTHERS_NAME, item.othersName)
                            intent.putExtras(bundle)

                            //告訴service進入哪間房
                            Global.whereChatroomId = item.chatroomId

                            startActivity(intent)
                            activity!!.finish()
                        }
                    }
                }
            }, type = ChatListItem.TYPE).build()
        rvChatList.adapter = adapter
        adapter.clear()//開始讀之前先清空


        //網路取得聊天室列表
        NetworkController.chatChatListAll(token!!).onResponse {
            //印出收到的東西
            Log.d("chatList", "chatList: $it")

            activity?.runOnUiThread {
                val chats = it.getJSONArray("chats")
                for (i in 0 until chats.length()) {
                    val item = ChatListItem(
                        chats.getJSONObject(i).getInt("chatroomId"),
                        chats.getJSONObject(i).getString("myName"),
                        chats.getJSONObject(i).getString("othersName"),
                        chats.getJSONObject(i).getString("latestMsg"),
                        chats.getJSONObject(i).getString("latestTime")
                    )
                    adapter.add(item)
                }
            }
        }.onFailure {}.onComplete {}.exec()

        //在Global設定方法，會在service中常態刷新
        Global.chatList = this
        return chatListView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //頁面刷新
    fun refresh() {
        //網路取得聊天室列表
        NetworkController.chatChatListAll(token!!).onResponse {
            //印出收到的東西
            Log.d("chatList", "chatList: $it")
            activity?.runOnUiThread {
                adapter.clear();
                val chats = it.getJSONArray("chats")
                for (i in 0 until chats.length()) {
                    val item = ChatListItem(
                        chats.getJSONObject(i).getInt("chatroomId"),
                        chats.getJSONObject(i).getString("myName"),
                        chats.getJSONObject(i).getString("othersName"),
                        chats.getJSONObject(i).getString("latestMsg"),
                        chats.getJSONObject(i).getString("latestTime")
                    )
                    adapter.add(item)
                }
                adapter.notifyDataSetChanged();
            }
        }.onFailure {}.onComplete {}.exec()
    }
}