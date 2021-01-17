package com.example.finalapplication.utils

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class NetworkController {
    fun interface OnResponseInterface {
        fun action(data: JSONObject)
    }

    fun interface OnFailureInterface {
        fun action()
    }

    fun interface OnCompleteInterface {
        fun action()
    }

    companion object Instance {
        val JSON = "application/json; charset=utf-8".toMediaType() //Postman也會帶的header
        private const val URL_ROOT = "http://35.229.145.199:3000"
        val client = OkHttpClient()


        //中介層
        class CCall(private var request: Request) {
            private lateinit var onFailureInterface: OnFailureInterface
            private lateinit var onResponseInterface: OnResponseInterface
            private lateinit var onCompleteInterface: OnCompleteInterface

            fun onFailure(onFailureInterface: OnFailureInterface): CCall {
                this.onFailureInterface = onFailureInterface
                return this
            }

            fun onResponse(onResponseInterface: OnResponseInterface): CCall {
                this.onResponseInterface = onResponseInterface
                return this
            }

            fun onComplete(onCompleteInterface: OnCompleteInterface): CCall {
                this.onCompleteInterface = onCompleteInterface
                return this
            }

            //可以留一般執行跟背景執行兩種
            fun exec(): Call {
                val call = client.newCall(request)
                call.enqueue(
                    CallbackAdapter(
                        onFailureInterface,
                        onResponseInterface,
                        onCompleteInterface
                    )
                )//不同callback間的轉換
                return call//把call回傳回去，它可以拿去自己做呼叫或是取消等等
            }


        }

        class CallbackAdapter(
            private var onFailure: OnFailureInterface,
            private var onResponse: OnResponseInterface,
            private var onComplete: OnCompleteInterface
        ) : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("!!!!!!!!", "?????????????")
                onFailure.action()
                onComplete.action()
            }

            //真正的回傳會到這邊來
            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                //轉成JSON格式
                val jsonObject = JSONObject(res!!)
                onResponse.action(jsonObject)
            }
        }

        //初次使用時註冊
        fun register(imei: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/register/$imei")
                .get()
                .build()
            return CCall(request)
        }

        //登入
        fun login(token: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/login/$token")
                .get()
                .build()
            return CCall(request)
        }

        //取得所有心情與所有活動(管理者功能)
        fun getMoodAndEvent(token: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/admin/getMoodAndEvent/$token")
                .get()
                .build()
            return CCall(request)
        }

        //新增日記
        fun addDiary(
            token: String,
            record_date: String,
            mood: IntArray,
            event: Array<IntArray?>?,
            content: String
        ): CCall {
            //心情分數Arr
            val moodJSONArray = JSONArray()
            for (i in mood.indices) {
                moodJSONArray.put(i, mood[i])
            }

            //活動雙層Arr
            val eventJSONArray = JSONArray()
            for (i in event?.indices!!) {
                val eventInnerJSONArray = JSONArray()
                for (j in event[i]?.indices!!) {
                    eventInnerJSONArray.put(event[i]?.get(j))
                }
                eventJSONArray.put(i, eventInnerJSONArray)
            }

            val jsonObject = JSONObject()
            jsonObject
                .put("record_date", record_date)
                .put("mood", moodJSONArray)
                .put("event", eventJSONArray)
                .put("content", content)

            val requestBody: RequestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/main/addDiary/$token")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //取得所有日記
        fun getAllDiary(token: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/main/getAllDiary/$token")
                .get()
                .build()
            return CCall(request)
        }

        //取得預設統計結果
        fun statistic(token: String, date: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/statistic/default?userId=$token&date=$date")
                .get()
                .build()
            return CCall(request)
        }

        //統計chart1更新活動
        fun statisticChart1Update(token: String, date: String, event: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/statistic/chart1/update?userId=$token&date=$date&event=$event")
                .get()
                .build()
            return CCall(request)
        }

        //統計chart1更新心情
        fun statisticChart1OnClocked(
            token: String,
            date: String,
            event: String,
            mood: String
        ): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/statistic/chart1/onClicked?userId=$token&date=$date&event=$event&mood=$mood")
                .get()
                .build()
            return CCall(request)
        }

        //統計chart2更新心情
        fun statisticChart2Update(token: String, date: String, mood: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/statistic/chart2/update?userId=$token&date=$date&mood=$mood")
                .get()
                .build()
            return CCall(request)
        }

        //統計chart3更新活動
        fun statisticChart3Update(token: String, date: String, event: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/statistic/chart3/update?userId=$token&date=$date&event=$event")
                .get()
                .build()
            return CCall(request)
        }

        //統計chart3更新心情
        fun statisticChart3OnClocked(
            token: String,
            date: String,
            event: String,
            mood: String
        ): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/statistic/chart3/onClicked?userId=$token&date=$date&event=$event&mood=$mood")
                .get()
                .build()
            return CCall(request)
        }

        //統計chart4更新心情
        fun statisticChart4Update(token: String, date: String, mood: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/statistic/chart4/update?userId=$token&date=$date&mood=$mood")
                .get()
                .build()
            return CCall(request)
        }

        //統計chart5更新心情 spinner
        fun statisticChart5Update(token: String, date: String, mood: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/statistic/chart5/update?userId=$token&date=$date&mood=$mood")
                .get()
                .build()
            return CCall(request)
        }

        //統計chart5更新心情 rv
        fun statisticChart5OnClocked(
            token: String,
            date: String,
            defaultMood: String,
            chosenMood: String
        ): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/statistic/chart5/onClicked?userId=$token&date=$date&defaultMood=$defaultMood&chosenMood=$chosenMood")
                .get()
                .build()
            return CCall(request)
        }

        //新增心情
        fun modifyMoodAdd(
            token: String,
            moodName: String,
            iconPath: Int

        ): CCall {

            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("moodName", moodName)
                .put("iconPath", iconPath)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/mood/add")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //更改心情
        fun modifyMoodUpdate(
            token: String,
            oldMoodName: String,
            newMoodName: String,
            iconPath: Int

        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("oldMoodName", oldMoodName)
                .put("newMoodName", newMoodName)
                .put("iconPath", iconPath)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/mood/update")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //刪除心情
        fun modifyMoodDelete(
            token: String,
            moodName: String

        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("moodName", moodName)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/mood/delete")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //新增活動種類
        fun modifyEventTypeAdd(
            token: String,
            eventTypeName: String,

            ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("eventTypeName", eventTypeName)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/eventType/add")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //更新活動種類
        fun modifyEventTypeUpdate(
            token: String,
            oldEventTypeName: String,
            newEventTypeName: String

        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("oldEventTypeName", oldEventTypeName)
                .put("newEventTypeName", newEventTypeName)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/eventType/update")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //刪除活動種類
        fun modifyEventTypeDelete(
            token: String,
            eventTypeName: String

        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("eventTypeName", eventTypeName)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/eventType/delete")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //新增活動
        fun modifyEventAdd(
            token: String,
            eventTypeName: String,
            eventName: String,
            iconPath: Int

        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("eventTypeName", eventTypeName)
                .put("eventName", eventName)
                .put("iconPath", iconPath)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/event/add")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //更新活動
        fun modifyEventUpdate(
            token: String,
            oldEventName: String,
            newEventName: String,
            iconPath: Int

        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("oldEventName", oldEventName)
                .put("newEventName", newEventName)
                .put("iconPath", iconPath)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/event/update")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //刪除活動
        fun modifyEventDelete(
            token: String,
            eventName: String

        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("eventName", eventName)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/event/delete")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //更新活動2
        fun modifyEventUpdate2(
            token: String,
            oldEventName: String,
            newEventName: String,
            iconPath: Int

        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", token)
                .put("oldEventName", oldEventName)
                .put("newEventName", newEventName)
                .put("iconPath", iconPath)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/event/update2")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //刪除日記
        fun modifyDiaryDelete(
            diaryId: Int
        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("diaryId", diaryId)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/diary/delete")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //取得要更改的日記
        fun modifyDiaryGetDiary(
            diaryId: Int
        ): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/modify/diary/$diaryId")
                .get()
                .build()
            return CCall(request)
        }

        //更改日記
        fun modifyDiaryUpdate(
            diaryId: Int,
            record_date: String,
            mood: IntArray,
            event: Array<IntArray?>?,
            content: String
        ): CCall {
            //心情分數Arr
            val moodJSONArray = JSONArray()
            for (i in mood.indices) {
                moodJSONArray.put(i, mood[i])
            }

            //活動雙層Arr
            val eventJSONArray = JSONArray()
            for (i in event?.indices!!) {
                val eventInnerJSONArray = JSONArray()
                for (j in event[i]?.indices!!) {
                    eventInnerJSONArray.put(event[i]?.get(j))
                }
                eventJSONArray.put(i, eventInnerJSONArray)
            }

            val jsonObject = JSONObject()
            jsonObject
                .put("diaryId", diaryId)
                .put("recordDate", record_date)
                .put("mood", moodJSONArray)
                .put("event", eventJSONArray)
                .put("content", content)

            val requestBody: RequestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/modify/diary/update")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //取得廣場全部貼文
        fun chatSquareAll(userId: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/chat/square/all/$userId")
                .get()
                .build()
            return CCall(request)
        }

        //post貼文到廣場
        fun chatSquarePost(
            hostName: String,
            postDate: String,
            post: String,
            diaryId: String
        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("hostName", hostName)
                .put("postDate", postDate)
                .put("post", post)
                .put("diaryId", diaryId)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/chat/square/post")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //取得自己的貼文資訊
        fun chatSquareMine(userId: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/chat/square/mine/$userId")
                .get()
                .build()
            return CCall(request)
        }

        //編輯自己的貼文(只能改內文)
        fun chatSquareEdit(
            userId: String,
            post: String
        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", userId)
                .put("post", post)


            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/chat/square/edit")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //刪除自己的貼文
        fun chatSquareDelete(userId: String): CCall {
            val jsonObject = JSONObject()
            val requestBody = jsonObject.toString().toRequestBody(JSON)
            val request = Request.Builder()
                .url("$URL_ROOT/chat/square/delete/$userId")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //編輯自己的貼文(只能改內文)
        fun chatChatRoomCreate(
            diaryId: String,
            hostName: String,
            userId: String,
            guestName: String,
            text: String
        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("diaryId", diaryId)
                .put("hostName", hostName)
                .put("userId", userId)
                .put("guestName", guestName)
                .put("text", text)


            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/chat/chatroom/create")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //取得聊天室列表
        fun chatChatListAll(userId: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/chat/chatlist/all/$userId")
                .get()
                .build()
            return CCall(request)
        }

        //離開聊天室
        fun chatChatRoomLeave(
            userId: String,
            chatroomId: Int
        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("userId", userId)
                .put("chatroomId", chatroomId)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/chat/chatroom/leave")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //傳送訊息
        fun chatChatRoomSend(
            chatroomId: Int,
            userId: String,
            text: String
        ): CCall {
            val jsonObject = JSONObject()

            jsonObject
                .put("chatroomId", chatroomId)
                .put("userId", userId)
                .put("text", text)

            val requestBody = jsonObject.toString().toRequestBody(JSON)

            val request = Request.Builder()
                .url("$URL_ROOT/chat/chatroom/send")
                .post(requestBody)
                .build()
            return CCall(request)
        }

        //取得訊息(最新的20個)
        fun chatChatRoomLoad(chatRoomId: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/chat/chatroom/load/$chatRoomId")
                .get()
                .build()
            return CCall(request)
        }

        //取得訊息(最新的20個)
        fun chatChatRoomHistory(chatRoomId: String, messageId: String): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/chat/chatroom/history?chatroomId=$chatRoomId&messageId=$messageId")
                .get()
                .build()
            return CCall(request)
        }

        //判斷要不要推播
        fun notificationIsNotified(
            userId: String,
            chatroomId: Int?
        ): CCall {
            val request = Request.Builder()
                .url("$URL_ROOT/notification/isNotified/?userId=$userId" + if (chatroomId == null) "" else "&chatroomId=$chatroomId")
                .get()
                .build()
            return CCall(request);
        }
    }
}