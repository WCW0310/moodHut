package com.example.finalapplication.utils

import android.widget.ImageView
import com.example.finalapplication.AddDiaryActActivity
import com.example.finalapplication.AddDiaryMoodActivity
import com.example.finalapplication.EditActActivity
import com.example.finalapplication.R
import com.example.finalapplication.fragments.ChatListFragment
import org.json.JSONObject

object Global {
    //常用常數

    //叫出SharedPreferences時用的名字
    const val DATA = "DATA"

    //userId
    const val TOKEN = "TOKEN"

    //存在SharedPreferences的全心情與活動資料的key
    const val MOOD_AND_EVENT = "MOOD_AND_EVENT"

    //填入心情與活動的圖示配對
    fun fillPathMap(
        moodAndEventJson: JSONObject,
        moodPathMap: MutableMap<String, String>,
        actPathMap: MutableMap<String, String>
    ) {
        //填入心情活動Icon鍵值對
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
    }


    //存在SharedPreferences的第一篇日記時間的key，限制統計頁面月份選擇的上限
    const val FIRST_DIARY_DATE = "FIRST_DIARY_DATE"

    //在貼文廣場取得聊天室Id，利用bundle傳至聊天室頁面的key
    const val CHAT_ROOM_ID = "CHAT_ROOM_ID"

    //在貼文廣場或聊天室列表，取得自己設定的暱稱，利用bundle傳至聊天室頁面的key
    const val MY_NAME = "MY_NAME"

    //在貼文廣場或聊天室列表，取得對方暱稱，利用bundle傳至聊天室頁面的key
    const val OTHERS_NAME = "OTHERS_NAME"

    //寫日記的時間利用bundle傳至選活動頁面的key
    const val TIME = "TIME"

    //寫日記的心情利用bundle傳至選活動頁面的key
    const val MOOD = "MOOD"

    //編輯心情的舊心情名利用bundle傳送時的key
    const val OLD_MOOD_NAME = "OLD_MOOD_NAME"

    //編輯心情或活動的舊圖片路徑利用bundle傳送時的key
    const val OLD_ICON_PATH = "OLD_ICON_PATH"

    //編輯活動的活動類別名利用bundle傳送時的key
    const val EVENT_TYPE_NAME = "EVENT_TYPE_NAME"

    //編輯活動的活動名利用bundle傳送時的key
    const val OLD_EVENT_NAME = "OLD_EVENT_NAME"


    //圖片路徑管理

    //心情圖片的總量加一
    const val MOOD_ICON_NUM = 29//要多一

    //活動圖片的總量加一
    const val EVENT_ICON_NUM = 164//要多一

    //圖片配對的方法
    fun iconPairing(imageView: ImageView, imageResource: String) {
        when (imageResource) {
            //null
            "0" -> imageView.setImageResource(R.drawable.delete_52px)
            //高興
            "1" -> imageView.setImageResource(R.drawable.happy_96px)
            //厭煩
            "2" -> imageView.setImageResource(R.drawable.boring_96px)
            //平靜
            "3" -> imageView.setImageResource(R.drawable.neutral_96px)
            //焦慮
            "4" -> imageView.setImageResource(R.drawable.nerd_96px)

            "5" -> imageView.setImageResource(R.drawable.in_love_48px)

            "6" -> imageView.setImageResource(R.drawable.angry_96px)

            "7" -> imageView.setImageResource(R.drawable.angel_96px)

            "8" -> imageView.setImageResource(R.drawable.wink_96px)

            "9" -> imageView.setImageResource(R.drawable.anime_emoji_96px)

            "10" -> imageView.setImageResource(R.drawable.uwu_emoji_96px)

            "11" -> imageView.setImageResource(R.drawable.bored_96px)

            "12" -> imageView.setImageResource(R.drawable.surprised_96px)

            "13" -> imageView.setImageResource(R.drawable.confused_96px)

            "14" -> imageView.setImageResource(R.drawable.cool_96px)

            "15" -> imageView.setImageResource(R.drawable.crazy_96px)

            "16" -> imageView.setImageResource(R.drawable.fat_emoji_96px)

            "17" -> imageView.setImageResource(R.drawable.smiling_face_with_heart_96px)

            "18" -> imageView.setImageResource(R.drawable.kiss_96px)

            "19" -> imageView.setImageResource(R.drawable.puzzled_96px)

            "20" -> imageView.setImageResource(R.drawable.sad_96px)

            "21" -> imageView.setImageResource(R.drawable.savouring_delicious_food_face_96px)

            "22" -> imageView.setImageResource(R.drawable.shocker_emoji_96px)

            "23" -> imageView.setImageResource(R.drawable.silent_96px)

            "24" -> imageView.setImageResource(R.drawable.sleeping_96px)

            "25" -> imageView.setImageResource(R.drawable.animation_96px)

            "26" -> imageView.setImageResource(R.drawable.iron_man_96px)

            "27" -> imageView.setImageResource(R.drawable.anonymous_mask_96px)

            "28" -> imageView.setImageResource(R.drawable.pikachu_pokemon_96px)


            //朋友
            "100" -> imageView.setImageResource(R.drawable.gay_128px)
            //家庭
            "101" -> imageView.setImageResource(R.drawable.family_96px)
            //約會
            "102" -> imageView.setImageResource(R.drawable.romance_80px)
            //水果
            "103" -> imageView.setImageResource(R.drawable.group_of_fruits_96px)
            //運動
            "104" -> imageView.setImageResource(R.drawable.sports_mode_80px)
            //喝水
            "105" -> imageView.setImageResource(R.drawable.water_bottle_96px)
            //遊戲
            "106" -> imageView.setImageResource(R.drawable.game_controller_128px)
            //閱讀
            "107" -> imageView.setImageResource(R.drawable.read_96px)
            //影片
            "108" -> imageView.setImageResource(R.drawable.video_conference_128px)
            //晴天
            "109" -> imageView.setImageResource(R.drawable.sun_96px)
            //多雲
            "110" -> imageView.setImageResource(R.drawable.partly_cloudy_day_200px)
            //雨天
            "111" -> imageView.setImageResource(R.drawable.heavy_rain_96px)

            "112" -> imageView.setImageResource(R.drawable.french_fries_60px)

            "113" -> imageView.setImageResource(R.drawable.cockroach_96px)

            "114" -> imageView.setImageResource(R.drawable.calendar_96px)

            "115" -> imageView.setImageResource(R.drawable.chart_96px)

            "116" -> imageView.setImageResource(R.drawable.clock_200px)

            "117" -> imageView.setImageResource(R.drawable.code_96px)

            "118" -> imageView.setImageResource(R.drawable.metal_music_96px)

            "119" -> imageView.setImageResource(R.drawable.pill_128px)

            "120" -> imageView.setImageResource(R.drawable.plus_96px)

            "121" -> imageView.setImageResource(R.drawable.pokeball_96px)

            "122" -> imageView.setImageResource(R.drawable.thumbs_down_96px)

            "123" -> imageView.setImageResource(R.drawable.thumbs_up_96px)

            "124" -> imageView.setImageResource(R.drawable.airplane_take_off_96px)

            "125" -> imageView.setImageResource(R.drawable.babys_room_96px)

            "126" -> imageView.setImageResource(R.drawable.baseball_96px)

            "127" -> imageView.setImageResource(R.drawable.basketball_96px)

            "128" -> imageView.setImageResource(R.drawable.bath_96px)

            "129" -> imageView.setImageResource(R.drawable.brain_96px)

            "130" -> imageView.setImageResource(R.drawable.bride_96px)

            "131" -> imageView.setImageResource(R.drawable.buy_96px)

            "132" -> imageView.setImageResource(R.drawable.card_payment_96px)

            "133" -> imageView.setImageResource(R.drawable.cash_in_hand_96px)

            "134" -> imageView.setImageResource(R.drawable.china_96px)

            "135" -> imageView.setImageResource(R.drawable.citrus_96px)

            "136" -> imageView.setImageResource(R.drawable.combo_chart_96px)

            "137" -> imageView.setImageResource(R.drawable.confetti_96px)

            "138" -> imageView.setImageResource(R.drawable.dental_braces_96px)

            "139" -> imageView.setImageResource(R.drawable.detain_96px)

            "140" -> imageView.setImageResource(R.drawable.dog_96px)

            "141" -> imageView.setImageResource(R.drawable.drop_of_blood_96px)

            "142" -> imageView.setImageResource(R.drawable.food_96px)

            "143" -> imageView.setImageResource(R.drawable.gift_96px)

            "144" -> imageView.setImageResource(R.drawable.google_images_96px)

            "145" -> imageView.setImageResource(R.drawable.liver_96px)

            "146" -> imageView.setImageResource(R.drawable.micro_96px)

            "147" -> imageView.setImageResource(R.drawable.money_bag_96px)

            "148" -> imageView.setImageResource(R.drawable.money_box_96px)

            "149" -> imageView.setImageResource(R.drawable.pizza_96px)

            "150" -> imageView.setImageResource(R.drawable.rainbow_96px)

            "151" -> imageView.setImageResource(R.drawable.scales_96px)

            "152" -> imageView.setImageResource(R.drawable.sloth_96px)

            "153" -> imageView.setImageResource(R.drawable.snorlax_96px)

            "154" -> imageView.setImageResource(R.drawable.sos_96px)

            "155" -> imageView.setImageResource(R.drawable.steam_96px)

            "156" -> imageView.setImageResource(R.drawable.tomato_96px)

            "157" -> imageView.setImageResource(R.drawable.trash_can_96px)

            "158" -> imageView.setImageResource(R.drawable.trophy_96px)

            "159" -> imageView.setImageResource(R.drawable.wifi_off_96px)

            "160" -> imageView.setImageResource(R.drawable.lungs_96px)

            "161" -> imageView.setImageResource(R.drawable.no_one_under_eighteen_emoji_96px)

            "162" -> imageView.setImageResource(R.drawable.microbe_96px)

            "163" -> imageView.setImageResource(R.drawable.kidney_80px)


        }
    }

    //service，常駐更新chatList
    var chatList: ChatListFragment? = null;
    val refreshChatList = fun() {
        chatList?.refresh();
    };

    //service，判讀現在位於哪間聊天室
    var whereChatroomId: Int? = null

    //service，刷新chatroom的方法

    var roomLoad: (() -> Unit)? = null;

    //service，控制進入聊天室並返回的情況
    var toChatList = false

    //記住選心情的Activity，讓他在完成日記時一起被finish()
    //或從日記本進入更改頁面更改心情時殺掉
    var addDiaryMoodActivity: AddDiaryMoodActivity? = null

    //是否從日記本進入更改頁面boolean
    var isFromAddDiary = false

    //是否有更改心情的Boolean，若有更改心情要殺掉記住的addDiaryActivity
    var isChangeMoodOrAct = false

    //記住選活動的Activity，從日記本進入更改頁面更改活動時殺掉
    var addDiaryActActivity: AddDiaryActActivity? = null

    //從日記本進入更改活動時，存入寫日記的時間跟活動分數陣列
    var addDiaryTime: String? = null
    var addDiaryMood: IntArray? = null

    //記住編輯活動頁面(該頁面包含特定活動類別名不好取得)，讓他在新增日記完成時一起被finish()
    //或編輯活動完成時一起被finish()
    var editActActivity: EditActActivity? = null
}