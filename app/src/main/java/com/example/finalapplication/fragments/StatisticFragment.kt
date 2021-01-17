package com.example.finalapplication.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.R
import com.example.finalapplication.items.StatisticMostOftenMoodItem
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.Global
import com.example.finalapplication.utils.Global.DATA
import com.example.finalapplication.utils.Global.FIRST_DIARY_DATE
import com.example.finalapplication.utils.Global.MOOD_AND_EVENT
import com.example.finalapplication.utils.Global.TOKEN
import com.example.finalapplication.utils.Global.iconPairing
import com.example.finalapplication.utils.NetworkController
import com.example.finalapplication.utils.adapters.CommonAdapter
import com.example.finalapplication.utils.adapters.MySpinnerAdapter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import org.json.JSONArray
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StatisticFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatisticFragment : Fragment() {
    //barChart
    lateinit var barChart1: BarChart
    lateinit var barChart2: BarChart
    lateinit var barChart4: BarChart
    lateinit var barChart5: BarChart

    //pieChart
    lateinit var pieChartN1: PieChart
    lateinit var pieChartN0: PieChart


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //被觀察者(類別)與觀察者(介面)
    private class MyObservable {
        var arr = ArrayList<MyObserver>()
        fun register(observer: MyObserver) {
            arr.add(observer)
        }

        fun notifyAllBut(observer: MyObserver) {
            val j = arr.indexOf(observer)
            for (i in arr.indices) {
                if (i != j) {
                    arr[i].update()
                }
            }
        }

        fun notifyAllByIndexBut(index: Int) {
            for (i in arr.indices) {
                if (i != index) {
                    arr[i].update()
                }
            }
        }

        fun notifyOneByIndex(index: Int) {
            arr[index].update()
        }

        fun countObserver(): Int {
            return arr.size;
        }

        fun clear() {
            arr = ArrayList<MyObserver>()
        }
    }

    interface MyObserver {
        fun update()
    }

    //被觀察者屬性
    private val revertChart1Observable = MyObservable()
    private val changeChart1Observable = MyObservable()
    private val revertChart3Observable = MyObservable()
    private val changeChart3Observable = MyObservable()
    private val revertChart5Observable = MyObservable()
    private val changeChart5Observable = MyObservable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Log.d("statistic", "Statistic!!!!!!")
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //要先宣告view變數
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_statistic, container, false)
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

        //填入活動心情Icon鍵值對
        val moodJSONObject = moodAndEventJson.getJSONObject("mood")
        val eventJSONObject = moodAndEventJson.getJSONObject("event")
        val moodNames: MutableList<String> = mutableListOf()
        val eventNames: MutableList<String> = mutableListOf()

        //順便填入心情名List
        for (key in moodJSONObject.keys()) {
            moodPathMap[key] = moodJSONObject.getInt(key).toString()
            moodNames.add(key)
        }
        //順便填入活動名List
        for (key in eventJSONObject.keys()) {
            for (key2 in eventJSONObject.getJSONObject(key).keys()) {
                actPathMap[key2] = eventJSONObject.getJSONObject(key).getInt(key2).toString()
                eventNames.add(key2)
            }
        }
        actPathMap["null"] = "0"
        moodPathMap["null"] = "0"


        //填入統計圖的ll，所有Chart要加進它
        val llStatistic = view.findViewById<LinearLayout>(R.id.llStatistic)

        //選擇月份元件
        val tvStatisticChooseMonth = view.findViewById<TextView>(R.id.tvStatisticChooseMonth)
        val tvStatisticChooseMonthD =
            view.findViewById<TextView>(R.id.tvStatisticChooseMonthD)
        val tvStatisticChooseMonthI =
            view.findViewById<TextView>(R.id.tvStatisticChooseMonthI)

        //一開始先取得當前日期與時間，之後可透過dataPickerDialog與timePickerDialog更新日記的設定時間，傳送時應該同時傳當前時間的Long與設定時間
        val calendar =
            Calendar.getInstance()//1970年1月1日開始計算到目前為止的格林威治標準時間的milliseconds，所以不管在哪一個時區所儲存的時間都是一樣的
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        tvStatisticChooseMonth.text = "$month 月 $year"//預設為當前月份

        var selectedMonth = "$month"
        var selectedYear = "$year"

        //可以選的月份陣列
        val monthOfChoosable = mutableListOf<String>()
        //for迴圈跑月份陣列，最小是第一篇日記的月份
        //"2020-1-1 9:26"
        val dateOfFistDiary = sharedPreferences.getString(FIRST_DIARY_DATE, "")
        var firstDiaryYear = 0
        var firstDiaryMonth = 0


        //重新載App會沒有dateOfFistDiary的資料，所以將月份設為當前月份
        if (dateOfFistDiary == "") {
            monthOfChoosable.add("$month 月 $year")
        } else {
            firstDiaryYear = dateOfFistDiary!!.split(" ")[0].split("-")[0].toInt()
            firstDiaryMonth = dateOfFistDiary.split(" ")[0].split("-")[1].toInt()
            if (year - firstDiaryYear == 0) {
                for (i in month downTo firstDiaryMonth) {
                    monthOfChoosable.add("$i 月 $year")
                }
            } else if (year - firstDiaryYear > 0) {
                for (i in month downTo 1) {
                    monthOfChoosable.add("$i 月 $year")
                }

                for (i in year - 1 downTo firstDiaryYear) {
                    if (i - firstDiaryYear > 0) {
                        for (j in 12 downTo 1) {
                            monthOfChoosable.add("$j 月 $i")
                        }
                    } else {
                        for (j in 12 downTo firstDiaryMonth) {
                            monthOfChoosable.add("$j 月 $i")
                        }
                    }
                }
            }
        }


        //選月份的AlertDialog
        val alertDialogContentView = LayoutInflater.from(view.context)
            .inflate(R.layout.alertdialog_choose_mood_score, null)
        val npChooseMood =
            alertDialogContentView.findViewById<NumberPicker>(R.id.npChooseMood)


        npChooseMood.minValue = 1
        npChooseMood.maxValue = monthOfChoosable.size
        npChooseMood.displayedValues = monthOfChoosable.toTypedArray()
        npChooseMood.value = 1
        val btnChooseMoodCancel =
            alertDialogContentView.findViewById<Button>(R.id.btnChooseMoodCancel)
        val btnChooseMoodConfirm =
            alertDialogContentView.findViewById<Button>(R.id.btnChooseMoodConfirm)
        val alertDialogChooseMoodScore = AlertDialog.Builder(view.context)
            .setView(alertDialogContentView)
            .setCancelable(false)//避免別人填東西過程中被突然取消
            .setTitle("選擇一個月份")
            .create()//show創建出來同時顯示()；create是創建出來不顯示，可以不用每次點擊都創建，效能消耗有差

        tvStatisticChooseMonth.setOnClickListener {
            alertDialogChooseMoodScore.show()
        }

        btnChooseMoodCancel.setOnClickListener {
            alertDialogChooseMoodScore.dismiss()
        }


        //ChartN1
        var chartN1View: View = LayoutInflater.from(view.context)
            .inflate(R.layout.statistic_chart_n1, container, false)

        val rvChartN1AllMoodCount =
            chartN1View.findViewById<RecyclerView>(R.id.rvChartN1AllMoodCount)

        //pieChart
        pieChartN1 = chartN1View.findViewById(R.id.pieChartN1)

        //設定rvAllMoodCount佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManagerChartN1 = GridLayoutManager(activity, 3)
//        layoutManager.orientation = HORIZONTAL
        //設定layoutManager進去rv讓他們有關聯
        rvChartN1AllMoodCount.layoutManager = layoutManagerChartN1
        //recyclerView設定區
        val adapterChartN1 = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chart1_most_often_mood_item, it, false)

                view.background = resources.getDrawable(R.drawable.radius_all)
                view.elevation = 0.0f

                //要綁資料的話宣告在這裡
                //mostOftenMood
                val ivChart1MostOftenMoodItem =
                    view.findViewById<ImageView>(R.id.ivChart1MostOftenMoodItem)
                val tvChart1MostOftenMoodItemName =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemName)
                val tvChart1MostOftenMoodItemTimes =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemTimes)

                return@Factory object : BaseViewHolder<StatisticMostOftenMoodItem>(view) {
                    override fun bind(item: StatisticMostOftenMoodItem) {
                        tvChart1MostOftenMoodItemName.text = item.moodName
                        when (super.getAdapterPosition()) {
                            0 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.MOMO
                                    )
                                )
                            }
                            1 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.ARAISYU
                                    )
                                )
                            }
                            2 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.USUKI
                                    )
                                )
                            }
                            3 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.HIWA
                                    )
                                )
                            }
                            4 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.MIZU
                                    )
                                )
                            }
                            else -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.SHIRONEZUMI
                                    )
                                )
                            }
                        }


                        tvChart1MostOftenMoodItemTimes.text = item.moodTimes
                        Global.iconPairing(
                            ivChart1MostOftenMoodItem,
                            moodPathMap[item.moodName!!]!!
                        )
                    }
                }
            }, type = StatisticMostOftenMoodItem.TYPE).build()
        rvChartN1AllMoodCount.adapter = adapterChartN1
        adapterChartN1.clear()//開始讀之前先清空


        //ChartN0
        val chartN0View: View = LayoutInflater.from(view.context)
            .inflate(R.layout.statistic_chart_n1, container, false)
        val rvChartN0AllMoodCount =
            chartN0View.findViewById<RecyclerView>(R.id.rvChartN1AllMoodCount)

        //textview設定為活動統計
        chartN0View.findViewById<TextView>(R.id.tvChartN1Name).setText("活動統計")

        //piechart
        pieChartN0 = chartN0View.findViewById(R.id.pieChartN1)

        //設定rvAllMoodCount佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManagerChartN0 = GridLayoutManager(activity, 3)
//        layoutManager.orientation = HORIZONTAL
        //設定layoutManager進去rv讓他們有關聯
        rvChartN0AllMoodCount.layoutManager = layoutManagerChartN0
        //recyclerView設定區
        val adapterChartN0 = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chart1_most_often_mood_item, it, false)
                view.background = resources.getDrawable(R.drawable.radius_all)
                view.elevation = 0.0f

                //要綁資料的話宣告在這裡
                //mostOftenMood
                val ivChart1MostOftenMoodItem =
                    view.findViewById<ImageView>(R.id.ivChart1MostOftenMoodItem)
                val tvChart1MostOftenMoodItemName =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemName)
                val tvChart1MostOftenMoodItemTimes =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemTimes)

                return@Factory object : BaseViewHolder<StatisticMostOftenMoodItem>(view) {
                    override fun bind(item: StatisticMostOftenMoodItem) {
                        tvChart1MostOftenMoodItemName.text = item.moodName
                        when (super.getAdapterPosition()) {
                            0 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.MOMO
                                    )
                                )
                            }
                            1 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.ARAISYU
                                    )
                                )
                            }
                            2 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.USUKI
                                    )
                                )
                            }
                            3 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.HIWA
                                    )
                                )
                            }
                            4 -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.MIZU
                                    )
                                )
                            }
                            else -> {
                                tvChart1MostOftenMoodItemName.setBackgroundColor(
                                    resources.getColor(
                                        R.color.SHIRONEZUMI
                                    )
                                )
                            }
                        }
                        tvChart1MostOftenMoodItemTimes.text = item.moodTimes
                        Global.iconPairing(
                            ivChart1MostOftenMoodItem,
                            actPathMap[item.moodName!!]!!
                        )
                    }
                }
            }, type = StatisticMostOftenMoodItem.TYPE).build()
        rvChartN0AllMoodCount.adapter = adapterChartN0
        adapterChartN0.clear()//開始讀之前先清空


        //Chart1
        val chart1View: View = LayoutInflater.from(view.context)
            .inflate(R.layout.statistic_chart1, container, false)

        val spinnerSelectEvent = chart1View.findViewById<Spinner>(R.id.spinnerSelectEvent)

        val rvAllMoodCount = chart1View.findViewById<RecyclerView>(R.id.rvAllMoodCount)

        val llChart1AllMoodScore = chart1View.findViewById<LinearLayout>(R.id.llChart1AllMoodScore)

        //barchart指定
        barChart1 = chart1View.findViewById(R.id.barChart1)

        //設定rvAllMoodCount佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManager = GridLayoutManager(context, 3)
//        layoutManager.orientation = HORIZONTAL
        //設定layoutManager進去rv讓他們有關聯
        rvAllMoodCount.layoutManager = layoutManager
        //recyclerView設定區
        val adapter = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chart1_most_often_mood_item, it, false)
                //要綁資料的話宣告在這裡
                //mostOftenMood
                val ivChart1MostOftenMoodItem =
                    view.findViewById<ImageView>(R.id.ivChart1MostOftenMoodItem)
                val tvChart1MostOftenMoodItemName =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemName)
                val tvChart1MostOftenMoodItemTimes =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemTimes)

                //test 存下換回原色的介面
                val revert = object : MyObserver {
                    override fun update() {
                        tvChart1MostOftenMoodItemName.setBackgroundColor(
                            resources.getColor(
                                R.color.SHIRONERI
                            )
                        )
                    }
                }
                revertChart1Observable.register(observer = revert)

                //test 存下變色用介面
                val change = object : MyObserver {
                    override fun update() {
                        tvChart1MostOftenMoodItemName.setBackgroundColor(
                            resources.getColor(
                                R.color.KAMENOZOKI
                            )
                        )
                    }
                }
                changeChart1Observable.register(observer = change)


                //背景色
                view.background = resources.getDrawable(R.drawable.radius_all)


                return@Factory object : BaseViewHolder<StatisticMostOftenMoodItem>(view) {
                    override fun bind(item: StatisticMostOftenMoodItem) {
                        //顏色設定
                        if (super.getAdapterPosition() == 0) {
                            tvChart1MostOftenMoodItemName.setBackgroundColor(
                                resources.getColor(
                                    R.color.KAMENOZOKI
                                )
                            )
                        } else {
                            tvChart1MostOftenMoodItemName.setBackgroundColor(
                                resources.getColor(
                                    R.color.SHIRONERI
                                )
                            )
                        }

                        tvChart1MostOftenMoodItemName.text = item.moodName
                        tvChart1MostOftenMoodItemTimes.text = item.moodTimes
                        Global.iconPairing(
                            ivChart1MostOftenMoodItem,
                            moodPathMap[item.moodName!!]!!
                        )

                        //心情統計的點擊事件，切換llAllMoodScore中填入的值(5分到1分的次數)的API
                        view.setOnClickListener {
                            //test，點擊後就變色，其他元件換回原色
                            tvChart1MostOftenMoodItemName.setBackgroundColor(
                                resources.getColor(
                                    R.color.KAMENOZOKI
                                )
                            )
                            revertChart1Observable.notifyAllBut(revert)

                            NetworkController.statisticChart1OnClocked(
                                token!!, "$selectedYear-$selectedMonth",
                                item.defaultEvent!!, item.moodName
                            )
                                .onResponse {
                                    //印出收到的東西
                                    Log.d("statistic", "chart1ChosenMoodScore: $it")

                                    activity?.runOnUiThread {
                                        var defaultMood = item.moodName

                                        //barChart setting
                                        var dataArray1 = ArrayList<Int>()

                                        llChart1AllMoodScore.removeAllViews()
                                        for (key in it.keys()) {
                                            if (context == null) {
                                                return@runOnUiThread
                                            }
                                            //barChart setting
                                            dataArray1.add(it[key].toString().toInt())
                                        }
                                        //barChart1
                                        showBarChart1(dataArray1, defaultMood)
                                    }
                                }.onFailure {}.onComplete {}.exec()
                        }
                    }
                }
            }, type = StatisticMostOftenMoodItem.TYPE).build()
        rvAllMoodCount.adapter = adapter
        adapter.clear()//開始讀之前先清空

        spinnerSelectEvent.adapter =
            MySpinnerAdapter(view.context, eventNames, actPathMap, R.layout.statistic_spinner_item)
//選擇選項之後要做的事
        spinnerSelectEvent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                NetworkController.statisticChart1Update(
                    token!!,
                    "$selectedYear-$selectedMonth",
                    eventNames[position]
                )
                    .onResponse {

                        //印出收到的東西
                        Log.d("statistic", "chart1Update: $it")
                        activity?.runOnUiThread {
//                            revertChart1Observable.clear()
//                            changeChart1Observable.clear()
                            adapter.clear()//開始讀之前先清空
                            for (key in it.getJSONObject("mostOftenMood").keys()) {
                                val item = StatisticMostOftenMoodItem(
                                    key,
                                    it.getJSONObject("mostOftenMood").getInt(key).toString(),
                                    eventNames[position]
                                )
                                adapter.add(item)
                            }

                            var defaultMood = ""
                            for (key in it.getJSONObject("mostOftenMood").keys()) {
                                defaultMood = key
                                break
                            }

                            llChart1AllMoodScore.removeAllViews()


                            //barChart setting
                            var dataArray1 = ArrayList<Int>()

                            for (key in it.getJSONObject("chosenMoodScore").keys()) {
                                if (context == null) {
                                    return@runOnUiThread
                                }
                                //barChart setting
                                dataArray1.add(
                                    it.getJSONObject("chosenMoodScore")[key].toString().toInt()
                                )
                            }
                            //barChart setting
                            showBarChart1(dataArray1, defaultMood)
                        }
                    }.onFailure {}.onComplete {}.exec()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //Chart2
        val chart2View: View = LayoutInflater.from(view.context)
            .inflate(R.layout.statistic_chart2, container, false)

        val spinnerChart2SelectMood = chart2View.findViewById<Spinner>(R.id.spinnerChart2SelectMood)
        val llChart2EventCount = chart2View.findViewById<LinearLayout>(R.id.llChar2EventCount)
        val llChart2AllMoodScore = chart2View.findViewById<LinearLayout>(R.id.llChart2AllMoodScore)

        barChart2 = chart2View.findViewById(R.id.barChart2)

        spinnerChart2SelectMood.adapter =
            MySpinnerAdapter(view.context, moodNames, moodPathMap, R.layout.statistic_spinner_item)
//選擇選項之後要做的事
        spinnerChart2SelectMood.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    NetworkController.statisticChart2Update(
                        token!!,
                        "$selectedYear-$selectedMonth",
                        moodNames[position]
                    )
                        .onResponse {
                            //印出收到的東西
                            Log.d("statistic", "chart2Update: $it")

                            activity?.runOnUiThread {
                                //chart2 chart2MostOftenEvent
                                llChart2EventCount.removeAllViews()
                                //將圖隔開的空ll
                                llChart2EventCount.addView(
                                    LayoutInflater.from(context)
                                        .inflate(R.layout.statistic_null_ll_v, container, false)
                                )
                                for (key in it.getJSONObject("mostOftenEvent").keys()) {
                                    if (context == null) {
                                        return@runOnUiThread
                                    }

                                    val chart2MostOftenEventView = LayoutInflater.from(context)
                                        .inflate(
                                            R.layout.chart1_most_often_mood_item,
                                            container,
                                            false
                                        )
                                    val ivChart2MostOftenMoodItem =
                                        chart2MostOftenEventView.findViewById<ImageView>(R.id.ivChart1MostOftenMoodItem)
                                    val tvChart2MostOftenEventItemName =
                                        chart2MostOftenEventView.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemName)
                                    val tvChart2MostOftenEventItemTimes =
                                        chart2MostOftenEventView.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemTimes)
                                    Global.iconPairing(ivChart2MostOftenMoodItem, actPathMap[key]!!)
                                    tvChart2MostOftenEventItemName.text = key
                                    tvChart2MostOftenEventItemTimes.text =
                                        it.getJSONObject("mostOftenEvent")[key].toString()

                                    chart2MostOftenEventView.background =
                                        resources.getDrawable(R.drawable.radius_all)
                                    chart2MostOftenEventView.elevation = 0.0f
                                    llChart2EventCount.addView(chart2MostOftenEventView)

                                    //想要將圖分開的linearLayout
                                    llChart2EventCount.addView(
                                        LayoutInflater.from(context)
                                            .inflate(R.layout.statistic_null_ll_v, container, false)
                                    )
                                }

                                //chart2 chart2EventByMoodScore
                                llChart2AllMoodScore.removeAllViews()
                                //barChart2 setting
                                var dataArray2 = ArrayList<Int>()
                                for (key in it.getJSONObject("eventByMoodScore").keys()) {
                                    if (context == null) {
                                        return@runOnUiThread
                                    }

                                    val chart2EventByMoodScoreView = LayoutInflater.from(context)
                                        .inflate(
                                            R.layout.chart1_default_mood_scores_item,
                                            container,
                                            false
                                        )
                                    val ivEventByMoodScoreItem =
                                        chart2EventByMoodScoreView.findViewById<ImageView>(R.id.ivChart1DefaultMoodScoresItem)
                                    val tvEventByMoodScoreItemName =
                                        chart2EventByMoodScoreView.findViewById<TextView>(R.id.tvChart1DefaultMoodScoresItemName)
//                                    val tvEventByMoodScoreItemTimes =
//                                        chart2EventByMoodScoreView.findViewById<TextView>(R.id.tvChart1DefaultMoodScoresItemTimes)
                                    val tvEventByMoodScoreItemScore =
                                        chart2EventByMoodScoreView.findViewById<TextView>(R.id.tvChart1DefaultMoodScoresItemScore)
                                    //只有一圈
                                    for (keyInside in it.getJSONObject("eventByMoodScore")
                                        .getJSONObject(key).keys()) {
                                        //barChart2 setting
                                        dataArray2.add(
                                            it.getJSONObject("eventByMoodScore").getJSONObject(key)
                                                .getString(keyInside)
                                                .toString().toInt()
                                        )

                                        Global.iconPairing(
                                            ivEventByMoodScoreItem,
                                            actPathMap[keyInside]!!
                                        )
                                        if (keyInside == "null") {
                                            tvEventByMoodScoreItemName.text = "無活動"
                                        } else {
                                            tvEventByMoodScoreItemName.text = keyInside
                                        }
//                                        tvEventByMoodScoreItemTimes.text =
//                                            it.getJSONObject("eventByMoodScore").getJSONObject(key)
//                                                .getString(keyInside)
//                                                .toString()
                                    }
                                    tvEventByMoodScoreItemScore.text = key
                                    llChart2AllMoodScore.addView(chart2EventByMoodScoreView)
                                }
                                showBarChart2(dataArray2)
                            }
                        }.onFailure {}.onComplete {}.exec()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }


        //chart3
        val chart3View: View = LayoutInflater.from(view.context)
            .inflate(R.layout.statistic_chart3, container, false)
        val llChart3ForAdd = chart3View.findViewById<LinearLayout>(R.id.llChart3ForAdd)
        val spinnerChart3SelectEvent =
            chart3View.findViewById<Spinner>(R.id.spinnerChart3SelectEvent)
        val rvChart3MostInfluenceMood =
            chart3View.findViewById<RecyclerView>(R.id.rvChart3MostInfluenceMood)

        //設定rvAllMoodCount佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManagerChart3 = GridLayoutManager(activity, 3)
//        layoutManagerChart3.orientation = HORIZONTAL
        //設定layoutManager進去rv讓他們有關聯
        rvChart3MostInfluenceMood.layoutManager = layoutManagerChart3
        //recyclerView設定區
        val adapterChart3 = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chart1_most_often_mood_item, it, false)

                //背景色
                view.background = resources.getDrawable(R.drawable.radius_all)

                //要綁資料的話宣告在這裡
                //mostOftenMood
                val ivChart1MostOftenMoodItem =
                    view.findViewById<ImageView>(R.id.ivChart1MostOftenMoodItem)
                val tvChart1MostOftenMoodItemName =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemName)
                val tvChart1MostOftenMoodItemTimes =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemTimes)

                //test 存下換回原色的介面
                val revert = object : MyObserver {
                    override fun update() {
                        tvChart1MostOftenMoodItemName.setBackgroundColor(
                            resources.getColor(
                                R.color.SHIRONERI
                            )
                        )
                    }
                }
                revertChart3Observable.register(observer = revert);
                //test 存下變色用介面
                val change = object : MyObserver {
                    override fun update() {
                        Log.d("yamiew4", "change 3 is executed")
                        tvChart1MostOftenMoodItemName.setBackgroundColor(
                            resources.getColor(
                                R.color.KAMENOZOKI
                            )
                        )
                    }
                }
                changeChart3Observable.register(observer = change);




                return@Factory object : BaseViewHolder<StatisticMostOftenMoodItem>(view) {
                    override fun bind(item: StatisticMostOftenMoodItem) {
                        //顏色設定
                        if (super.getAdapterPosition() == 0) {
                            tvChart1MostOftenMoodItemName.setBackgroundColor(
                                resources.getColor(
                                    R.color.KAMENOZOKI
                                )
                            )
                        } else {
                            tvChart1MostOftenMoodItemName.setBackgroundColor(
                                resources.getColor(
                                    R.color.SHIRONERI
                                )
                            )
                        }

                        tvChart1MostOftenMoodItemName.text = item.moodName
                        tvChart1MostOftenMoodItemTimes.text = item.moodTimes
                        Global.iconPairing(
                            ivChart1MostOftenMoodItem,
                            moodPathMap[item.moodName!!]!!
                        )


                        //心情統計的點擊事件，切換llAllMoodScore中填入的值(5分到1分的次數)的API
                        view.setOnClickListener {
                            //test，點擊後就變色，其他元件換回原色
                            tvChart1MostOftenMoodItemName.setBackgroundColor(
                                resources.getColor(
                                    R.color.KAMENOZOKI
                                )
                            )
                            revertChart3Observable.notifyAllBut(revert)

                            //網路連線
                            NetworkController.statisticChart3OnClocked(
                                token!!,
                                "$selectedYear-$selectedMonth",
                                item.defaultEvent!!,
                                item.moodName
                            )
                                .onResponse {
                                    //印出收到的東西
                                    Log.d("statistic", "chart3onClicked: $it")

                                    activity?.runOnUiThread {
                                        //chart3 defaultMoodAvg
                                        llChart3ForAdd.removeAllViews()
                                        val chart3DefaultMoodAvgView = LayoutInflater.from(context)
                                            .inflate(
                                                R.layout.chart3_default_mood_avg,
                                                container,
                                                false
                                            )
                                        val ivChart3DefaultMoodAvgMoodImage =
                                            chart3DefaultMoodAvgView.findViewById<ImageView>(R.id.ivChart3DefaultMoodAvgMoodImage)
                                        val tvChart3DefaultMoodAvgMoodName =
                                            chart3DefaultMoodAvgView.findViewById<TextView>(R.id.tvChart3DefaultMoodAvgMoodName)
                                        val tvChart3DefaultMoodAvgHaveEvent =
                                            chart3DefaultMoodAvgView.findViewById<TextView>(R.id.tvChart3DefaultMoodAvgHaveEvent)
                                        val tvChart3DefaultMoodAvgNoEvent =
                                            chart3DefaultMoodAvgView.findViewById<TextView>(R.id.tvChart3DefaultMoodAvgNoEvent)

                                        Global.iconPairing(
                                            ivChart3DefaultMoodAvgMoodImage,
                                            moodPathMap[item.moodName]!!
                                        )
                                        tvChart3DefaultMoodAvgMoodName.text = item.moodName
                                        tvChart3DefaultMoodAvgHaveEvent.text =
                                            it.getJSONArray("moodAvg")[0].toString()
                                        tvChart3DefaultMoodAvgNoEvent.text =
                                            it.getJSONArray("moodAvg")[1].toString()
                                        llChart3ForAdd.addView(chart3DefaultMoodAvgView)
                                    }
                                }.onFailure {}.onComplete {}.exec()
                        }
                    }
                }
            }, type = StatisticMostOftenMoodItem.TYPE).build()
        rvChart3MostInfluenceMood.adapter = adapterChart3
        adapterChart3.clear()//開始讀之前先清空

        spinnerChart3SelectEvent.adapter =
            MySpinnerAdapter(view.context, eventNames, actPathMap, R.layout.statistic_spinner_item)
        //選擇選項之後要做的事
        spinnerChart3SelectEvent.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    //test，spinner點擊後只讓第一個元件發亮
                    revertChart3Observable.notifyAllByIndexBut(0);
                    if (changeChart3Observable.countObserver() > 0) {
                        changeChart3Observable.notifyOneByIndex(0)
                    }
                    //網路連線
                    NetworkController.statisticChart3Update(
                        token!!,
                        "$selectedYear-$selectedMonth",
                        eventNames[position]
                    )
                        .onResponse {
                            //印出收到的東西
                            Log.d("statistic", "chart3Update: $it")

                            activity?.runOnUiThread {
                                adapterChart3.clear()//開始讀之前先清空
                                for (key in it.getJSONObject("mostInfluenceMood").keys()) {
                                    val item = StatisticMostOftenMoodItem(
                                        key,
                                        it.getJSONObject("mostInfluenceMood").getDouble(key)
                                            .toString(),
                                        eventNames[position]
                                    )
                                    adapterChart3.add(item)
                                }

                                var defaultMood = "null"
                                for (key in it.getJSONObject("mostInfluenceMood").keys()) {
                                    defaultMood = key
                                    break
                                }
                                //chart3 defaultMoodAvg
                                llChart3ForAdd.removeAllViews()
                                val chart3DefaultMoodAvgView = LayoutInflater.from(context)
                                    .inflate(R.layout.chart3_default_mood_avg, container, false)
                                val ivChart3DefaultMoodAvgMoodImage =
                                    chart3DefaultMoodAvgView.findViewById<ImageView>(R.id.ivChart3DefaultMoodAvgMoodImage)
                                val tvChart3DefaultMoodAvgMoodName =
                                    chart3DefaultMoodAvgView.findViewById<TextView>(R.id.tvChart3DefaultMoodAvgMoodName)
                                val tvChart3DefaultMoodAvgHaveEvent =
                                    chart3DefaultMoodAvgView.findViewById<TextView>(R.id.tvChart3DefaultMoodAvgHaveEvent)
                                val tvChart3DefaultMoodAvgNoEvent =
                                    chart3DefaultMoodAvgView.findViewById<TextView>(R.id.tvChart3DefaultMoodAvgNoEvent)

                                iconPairing(
                                    ivChart3DefaultMoodAvgMoodImage,
                                    moodPathMap[defaultMood]!!
                                )
                                if (defaultMood == "null") {
                                    tvChart3DefaultMoodAvgMoodName.text = "無心情"
                                    tvChart3DefaultMoodAvgHaveEvent.text = "無結果"
                                    tvChart3DefaultMoodAvgNoEvent.text = "無結果"
                                } else {
                                    tvChart3DefaultMoodAvgMoodName.text = defaultMood

                                    if (it.getJSONArray("moodAvg").length() == 0) {
                                        tvChart3DefaultMoodAvgHaveEvent.text = "無結果"
                                        tvChart3DefaultMoodAvgNoEvent.text = "無結果"
                                    } else {
                                        tvChart3DefaultMoodAvgHaveEvent.text =
                                            it.getJSONArray("moodAvg")[0].toString()
                                        tvChart3DefaultMoodAvgNoEvent.text =
                                            it.getJSONArray("moodAvg")[1].toString()
                                    }
                                }
                                llChart3ForAdd.addView(chart3DefaultMoodAvgView)
                            }
                        }.onFailure {}.onComplete {}.exec()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        //chart4
        val chart4View: View = LayoutInflater.from(view.context)
            .inflate(R.layout.statistic_chart4, container, false)
        val ivChart4Mood = chart4View.findViewById<ImageView>(R.id.ivChart4Mood)
        val tvChart4MoodStability = chart4View.findViewById<TextView>(R.id.tvChart4MoodStability)
        val tvChart4MoodName = chart4View.findViewById<TextView>(R.id.tvChart4MoodName)
        val llChart4AllMoodScore = chart4View.findViewById<LinearLayout>(R.id.llChart4AllMoodScore)

        val spinnerChart4SelectMood = chart4View.findViewById<Spinner>(R.id.spinnerChart4SelectMood)

        //barChart setting
        barChart4 = chart4View.findViewById(R.id.barChart4)
//        spinnerChart4ArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChart4SelectMood.adapter =
            MySpinnerAdapter(view.context, moodNames, moodPathMap, R.layout.statistic_spinner_item)
        //選擇選項之後要做的事
        spinnerChart4SelectMood.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    NetworkController.statisticChart4Update(
                        token!!,
                        "$selectedYear-$selectedMonth",
                        moodNames[position]
                    )
                        .onResponse {
                            //印出收到的東西
                            Log.d("statistic", "chart4Update: $it")

                            activity?.runOnUiThread {

                                Global.iconPairing(
                                    ivChart4Mood,
                                    moodPathMap[moodNames[position]]!!
                                )

                                if (it.getString("moodStability") == "需要更多紀錄!") {
                                    tvChart4MoodStability.text = it.getString("moodStability")
                                } else {
                                    tvChart4MoodStability.text =
                                        it.getString("moodStability") + " / 100"
                                }

                                tvChart4MoodName.text = moodNames[position]

                                llChart4AllMoodScore.removeAllViews()

                                var dataArray4 = ArrayList<Int>()
                                for (key in it.getJSONObject("moodScore").keys()) {
                                    if (context == null) {
                                        return@runOnUiThread
                                    }
                                    dataArray4.add(it.getJSONObject("moodScore").getInt(key))
                                }
                                showBarChart4(dataArray4, moodNames[position])
                            }
                        }.onFailure {}.onComplete {}.exec()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }


        //chart5
        val chart5View: View = LayoutInflater.from(view.context)
            .inflate(R.layout.statistic_chart5, container, false)
//        val llChart5AllMoodScore = chart5View.findViewById<LinearLayout>(R.id.llChart5AllMoodScore)
        val rvChart5AllMoodRelation =
            chart5View.findViewById<RecyclerView>(R.id.rvChart5AllMoodRelation)

        val ivChart5Mood = chart5View.findViewById<ImageView>(R.id.ivChart5Mood)
        val tvChart5MoodName = chart5View.findViewById<TextView>(R.id.tvChart5MoodName)
        val tvChart5MoodStability = chart5View.findViewById<TextView>(R.id.tvChart5MoodStability)

        //barChart setting
        barChart5 = chart5View.findViewById(R.id.barChart5)

        //設定rvChart5AllMoodRelation佈局樣式，要傳Context，Activity是其子類，所以傳當前Activity進去就好(this)
        val layoutManagerChart5 = GridLayoutManager(activity, 3)
//        layoutManagerChart5.orientation = HORIZONTAL
        //設定layoutManager進去rv讓他們有關聯
        rvChart5AllMoodRelation.layoutManager = layoutManagerChart5
        //recyclerView設定區
        val adapterChart5 = CommonAdapter.Builder()
            .addType(factory = BaseViewHolder.Factory {
                val view: View = LayoutInflater.from(it?.context)
                    .inflate(R.layout.chart1_most_often_mood_item, it, false)

                //背景色
                view.background = resources.getDrawable(R.drawable.radius_all)

                //要綁資料的話宣告在這裡
                //mostOftenMood
                val ivChart1MostOftenMoodItem =
                    view.findViewById<ImageView>(R.id.ivChart1MostOftenMoodItem)
                val tvChart1MostOftenMoodItemName =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemName)
                val tvChart1MostOftenMoodItemTimes =
                    view.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemTimes)

                //test 存下換回原色的介面
                val revert = object : MyObserver {
                    override fun update() {
                        tvChart1MostOftenMoodItemName.setBackgroundColor(
                            resources.getColor(
                                R.color.SHIRONERI
                            )
                        )
                    }
                }
                revertChart5Observable.register(observer = revert);

                //test 存下變色用介面
                val change = object : MyObserver {
                    override fun update() {
                        tvChart1MostOftenMoodItemName.setBackgroundColor(
                            resources.getColor(
                                R.color.KAMENOZOKI
                            )
                        )
                    }
                }
                changeChart5Observable.register(observer = change)

                return@Factory object : BaseViewHolder<StatisticMostOftenMoodItem>(view) {
                    override fun bind(item: StatisticMostOftenMoodItem) {
                        println(super.getAdapterPosition())
                        if (super.getAdapterPosition() == 0) {
                            tvChart1MostOftenMoodItemName.setBackgroundColor(
                                resources.getColor(
                                    R.color.KAMENOZOKI
                                )
                            )
                        } else {
                            tvChart1MostOftenMoodItemName.setBackgroundColor(
                                resources.getColor(
                                    R.color.SHIRONERI
                                )
                            )
                        }



                        tvChart1MostOftenMoodItemName.text = item.moodName

                        if (item.moodTimes == "null") {
                            tvChart1MostOftenMoodItemTimes.text = "無結果"
                        } else {
                            tvChart1MostOftenMoodItemTimes.text = item.moodTimes
                        }

                        iconPairing(
                            ivChart1MostOftenMoodItem,
                            moodPathMap[item.moodName!!]!!
                        )

                        //心情統計的點擊事件，切換llAllMoodScore中填入的值(5分到1分的次數)的API
                        view.setOnClickListener {
                            //test，點擊後就變色，其他元件換回原色
                            tvChart1MostOftenMoodItemName.setBackgroundColor(
                                resources.getColor(
                                    R.color.KAMENOZOKI
                                )
                            )
                            revertChart5Observable.notifyAllBut(revert)


                            iconPairing(ivChart5Mood, moodPathMap[item.moodName]!!)
                            tvChart5MoodName.text = item.moodName
                            if (item.moodTimes == "null"
                            ) {
                                tvChart5MoodStability.text = "無結果"
                            } else {
                                tvChart5MoodStability.text =
                                    item.moodTimes + " / 100"
                            }

                            //網路連線
                            NetworkController.statisticChart5OnClocked(
                                token!!, "$selectedYear-$selectedMonth",
                                item.defaultEvent!!, item.moodName
                            )
                                .onResponse {
                                    //印出收到的東西
                                    Log.d("statistic", "chart5ChosenMoodScore: $it")

                                    activity?.runOnUiThread {
                                        var defaultMood = item.moodName

//                                        llChart5AllMoodScore.removeAllViews()

                                        var dataArray5 = ArrayList<Float>()
                                        for (key in it.getJSONObject("avgMoodByMood").keys()) {
                                            if (context == null) {
                                                return@runOnUiThread
                                            }
                                            dataArray5.add(
                                                if (it.getJSONObject("avgMoodByMood")[key].toString() == "null") 0f
                                                else it.getJSONObject("avgMoodByMood")[key].toString()
                                                    .toFloat()
                                            )
                                        }
                                        showBarChart5(dataArray5, defaultMood!!)
                                    }
                                }.onFailure {}.onComplete {}.exec()
                        }
                    }
                }
            }, type = StatisticMostOftenMoodItem.TYPE).build()
        rvChart5AllMoodRelation.adapter = adapterChart5
        adapterChart5.clear()//開始讀之前先清空

        val spinnerChart5SelectMood = chart5View.findViewById<Spinner>(R.id.spinnerChart5SelectMood)
        spinnerChart5SelectMood.adapter =
            MySpinnerAdapter(view.context, moodNames, moodPathMap, R.layout.statistic_spinner_item)
        //選擇選項之後要做的事
        spinnerChart5SelectMood.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    //網路連線
                    NetworkController.statisticChart5Update(
                        token!!,
                        "$selectedYear-$selectedMonth",
                        moodNames[position]
                    )
                        .onResponse {
                            //印出收到的東西
                            Log.d("statistic", "chart5Update: $it")

                            activity?.runOnUiThread {
                                adapterChart5.clear()//開始讀之前先清空
                                for (key in it.getJSONObject("mostRelevantMood").keys()) {
                                    val item = StatisticMostOftenMoodItem(
                                        key,
                                        it.getJSONObject("mostRelevantMood").getString(key),
                                        moodNames[position]
                                    )
                                    adapterChart5.add(item)
                                }

                                //設定第一個顏色
//                                rvChart5AllMoodRelation.get(3).setBackgroundColor(R.color.black)

                                var defaultMood = ""
                                for (key in it.getJSONObject("mostRelevantMood").keys()) {
                                    defaultMood = key
                                    break
                                }

                                iconPairing(ivChart5Mood, moodPathMap[defaultMood]!!)
                                tvChart5MoodName.text = defaultMood
                                if (it.getJSONObject("mostRelevantMood")
                                        .getString(defaultMood) == "null"
                                ) {
                                    tvChart5MoodStability.text = "無結果"
                                } else {
                                    tvChart5MoodStability.text =
                                        it.getJSONObject("mostRelevantMood")
                                            .getString(defaultMood) + " / 100"
                                }


//                                llChart5AllMoodScore.removeAllViews()
                                var dataArray5 = ArrayList<Float>()
                                for (key in it.getJSONObject("avgMoodByMood").keys()) {
                                    if (context == null) {
                                        return@runOnUiThread
                                    }
                                    dataArray5.add(
                                        if (it.getJSONObject("avgMoodByMood")[key].toString() == "null") 0f
                                        else it.getJSONObject("avgMoodByMood")[key].toString()
                                            .toFloat()
                                    )
                                }
                                showBarChart5(dataArray5, defaultMood)
                            }
                        }.onFailure {}.onComplete {}.exec()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        //不想在NetworkController裡面創造View，先宣告
        //chartN1資料
        var chartN1MoodDistribution: JSONObject
        //chartN0資料
        var chartN0EventDistribution: JSONObject
        //chart1資料
        var mostOftenMood: JSONObject
        var defaultMoodScores: JSONObject
        //chart2資料
        var chart2MostOftenEvent: JSONObject
        var chart2EventByMoodScore: JSONObject
        //chart3資料
        var chart3MostInfluenceMood: JSONObject
        var chart3DefaultMoodAvg: JSONArray
        //chart4資料
        var chart4MoodStability: String
        var chart4DefaultMoodScore: JSONObject
        var chart4DefaultMood: String
        //chart5資料
        var chart5DefaultMood: String
        var chart5MostRelevantMood: JSONObject
        var chart5AvgMoodByMood: JSONObject

        //千行
        fun default() {
            NetworkController.statistic(token!!, "$selectedYear-$selectedMonth").onResponse {
                //印出收到的東西
                Log.d("statistic", "statisticCharts: $it")

                val chartN1 = it.getJSONObject("chartN1")
                val chartN0 = it.getJSONObject("chartN0")
                val chart1 = it.getJSONObject("chart1")
                val chart2 = it.getJSONObject("chart2")
                val chart3 = it.getJSONObject("chart3")
                val chart4 = it.getJSONObject("chart4")
                val chart5 = it.getJSONObject("chart5")

                //chartN1的資料
                chartN1MoodDistribution = chartN1.getJSONObject("moodDistribution")
                //chartN0的資料
                chartN0EventDistribution = chartN0.getJSONObject("eventDistribution")
                //chart1的資料
                mostOftenMood = chart1.getJSONObject("mostOftenMood")
                defaultMoodScores = chart1.getJSONObject("defaultMoodScores")
                //chart2的資料
                chart2MostOftenEvent = chart2.getJSONObject("mostOftenEvent")
                chart2EventByMoodScore = chart2.getJSONObject("eventByMoodScore")
                //chart3的資料
                chart3MostInfluenceMood = chart3.getJSONObject("mostInfluenceMood")
                chart3DefaultMoodAvg = chart3.getJSONArray("defaultMoodAvg")
                //chart4的資料
                chart4MoodStability = chart4.getString("moodStability")
                chart4DefaultMoodScore = chart4.getJSONObject("defaultMoodScore")
                chart4DefaultMood = chart4.getString("defaultMood")
                //chart5的資料
                chart5DefaultMood = chart5.getString("defaultMood")
                chart5MostRelevantMood = chart5.getJSONObject("mostRelevantMood")
                chart5AvgMoodByMood = chart5.getJSONObject("avgMoodByMood")

                activity?.runOnUiThread {
                    //清空所有統計圖
                    llStatistic.removeAllViews()

                    //chartN1 rv
                    adapterChartN1.clear()//開始讀之前先清空
                    var keyDataArrayN1 = ArrayList<String>()
                    var valueDataArrayN1 = ArrayList<Int>()
                    for (key in chartN1MoodDistribution.keys()) {
                        keyDataArrayN1.add(key)
                        valueDataArrayN1.add(chartN1MoodDistribution.getInt(key))
                        Log.d("yamiew", key)
                        val item = StatisticMostOftenMoodItem(
                            key,
                            chartN1MoodDistribution.getInt(key).toString(),
                            ""
                        )
                        adapterChartN1.add(item)
                    }
                    showPieChartN1(keyDataArrayN1, valueDataArrayN1)
                    llStatistic.addView(chartN1View)
                    llStatistic.addView(
                        LayoutInflater.from(view.context)
                            .inflate(R.layout.statistic_null_ll, container, false)
                    )

                    //chartN0 rv
                    adapterChartN0.clear()//開始讀之前先清空
                    var keyDataArrayN0 = ArrayList<String>()
                    var valueDataArrayN0 = ArrayList<Int>()
                    for (key in chartN0EventDistribution.keys()) {
                        keyDataArrayN0.add(key)
                        valueDataArrayN0.add(chartN0EventDistribution.getInt(key))
                        val item = StatisticMostOftenMoodItem(
                            key,
                            chartN0EventDistribution.getInt(key).toString(),
                            ""
                        )
                        adapterChartN0.add(item)
                    }
                    showPieChartN0(keyDataArrayN0, valueDataArrayN0)
                    llStatistic.addView(chartN0View)
                    llStatistic.addView(
                        LayoutInflater.from(view.context)
                            .inflate(R.layout.statistic_null_ll, container, false)
                    )

                    //chart1 rv
                    adapter.clear()//開始讀之前先清空

                    for (key in mostOftenMood.keys()) {
                        val item = StatisticMostOftenMoodItem(
                            key,
                            mostOftenMood.getInt(key).toString(),
                            chart1.getString("defaultEvent")
                        )
                        adapter.add(item)
                    }

                    //barChart1 setting
                    var dataArray1 = ArrayList<Int>()

                    //chart1 ll defaultMoodScores部分
                    var defaultMood = ""
                    for (key in mostOftenMood.keys()) {
                        defaultMood = key
                        break
                    }

                    for (key in defaultMoodScores.keys()) {
                        if (context == null) {
                            return@runOnUiThread
                        }
                        //barChart1 setting
                        dataArray1.add(defaultMoodScores[key].toString().toInt())
                    }
                    showBarChart1(dataArray1, defaultMood)
                    llStatistic.addView(chart1View)
                    llStatistic.addView(
                        LayoutInflater.from(view.context)
                            .inflate(R.layout.statistic_null_ll, container, false)
                    )

                    //chart2 chart2MostOftenEvent
                    llChart2EventCount.removeAllViews()
                    //想要將圖分開的linearLayout
                    llChart2EventCount.addView(
                        LayoutInflater.from(context)
                            .inflate(R.layout.statistic_null_ll_v, container, false)
                    )
                    for (key in chart2MostOftenEvent.keys()) {
                        if (context == null) {
                            return@runOnUiThread
                        }
                        val chart2MostOftenEventView = LayoutInflater.from(context)
                            .inflate(R.layout.chart1_most_often_mood_item, container, false)

                        val ivChart2MostOftenMoodItem =
                            chart2MostOftenEventView.findViewById<ImageView>(R.id.ivChart1MostOftenMoodItem)
                        val tvChart2MostOftenEventItemName =
                            chart2MostOftenEventView.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemName)
                        val tvChart2MostOftenEventItemTimes =
                            chart2MostOftenEventView.findViewById<TextView>(R.id.tvChart1MostOftenMoodItemTimes)
                        Global.iconPairing(ivChart2MostOftenMoodItem, actPathMap[key]!!)
                        tvChart2MostOftenEventItemName.text = key
                        tvChart2MostOftenEventItemTimes.text = chart2MostOftenEvent[key].toString()

                        chart2MostOftenEventView.background =
                            resources.getDrawable(R.drawable.radius_all)
                        chart2MostOftenEventView.elevation = 0.0f
                        llChart2EventCount.addView(chart2MostOftenEventView)

                        //想要將圖分開的linearLayout
                        llChart2EventCount.addView(
                            LayoutInflater.from(context)
                                .inflate(R.layout.statistic_null_ll_v, container, false)
                        )
                    }

                    //chart2 chart2EventByMoodScore
                    llChart2AllMoodScore.removeAllViews()
                    var dataArray2 = ArrayList<Int>()
                    for (key in chart2EventByMoodScore.keys()) {
                        if (context == null) {
                            return@runOnUiThread
                        }
                        val chart2EventByMoodScoreView = LayoutInflater.from(context)
                            .inflate(R.layout.chart1_default_mood_scores_item, container, false)
                        val ivEventByMoodScoreItem =
                            chart2EventByMoodScoreView.findViewById<ImageView>(R.id.ivChart1DefaultMoodScoresItem)
                        val tvEventByMoodScoreItemName =
                            chart2EventByMoodScoreView.findViewById<TextView>(R.id.tvChart1DefaultMoodScoresItemName)
//                        val tvEventByMoodScoreItemTimes =
//                            chart2EventByMoodScoreView.findViewById<TextView>(R.id.tvChart1DefaultMoodScoresItemTimes)
                        val tvEventByMoodScoreItemScore =
                            chart2EventByMoodScoreView.findViewById<TextView>(R.id.tvChart1DefaultMoodScoresItemScore)
                        //只有一圈
                        for (keyInside in chart2EventByMoodScore.getJSONObject(key).keys()) {
                            dataArray2.add(
                                chart2EventByMoodScore.getJSONObject(key)
                                    .getString(keyInside)
                                    .toString().toInt()
                            )

                            Global.iconPairing(ivEventByMoodScoreItem, actPathMap[keyInside]!!)
                            if (keyInside == "null") {
                                tvEventByMoodScoreItemName.text = "無活動"
                            } else {
                                tvEventByMoodScoreItemName.text = keyInside
                            }
//                            tvEventByMoodScoreItemTimes.text =
//                                chart2EventByMoodScore.getJSONObject(key).getString(keyInside)
//                                    .toString()
                        }
                        tvEventByMoodScoreItemScore.text = key

                        llChart2AllMoodScore.addView(chart2EventByMoodScoreView)
                    }
                    showBarChart2(dataArray2)
                    llStatistic.addView(chart2View)
                    llStatistic.addView(
                        LayoutInflater.from(view.context)
                            .inflate(R.layout.statistic_null_ll, container, false)
                    )

                    //chart3 rv
                    llChart3ForAdd.removeAllViews()
                    adapterChart3.clear()//開始讀之前先清空
                    for (key in chart3MostInfluenceMood.keys()) {
                        val item = StatisticMostOftenMoodItem(
                            key,
                            chart3MostInfluenceMood.getDouble(key).toString(),
                            chart3.getString("defaultEvent")
                        )
                        adapterChart3.add(item)
                    }

                    //chart3 defaultMoodAvg
                    val chart3DefaultMoodAvgView = LayoutInflater.from(context)
                        .inflate(R.layout.chart3_default_mood_avg, container, false)
                    val ivChart3DefaultMoodAvgMoodImage =
                        chart3DefaultMoodAvgView.findViewById<ImageView>(R.id.ivChart3DefaultMoodAvgMoodImage)
                    val tvChart3DefaultMoodAvgMoodName =
                        chart3DefaultMoodAvgView.findViewById<TextView>(R.id.tvChart3DefaultMoodAvgMoodName)
                    val tvChart3DefaultMoodAvgHaveEvent =
                        chart3DefaultMoodAvgView.findViewById<TextView>(R.id.tvChart3DefaultMoodAvgHaveEvent)
                    val tvChart3DefaultMoodAvgNoEvent =
                        chart3DefaultMoodAvgView.findViewById<TextView>(R.id.tvChart3DefaultMoodAvgNoEvent)
                    var defaultMoodChart3 = "null"
                    for (key in chart3MostInfluenceMood.keys()) {
                        defaultMoodChart3 = key
                        break
                    }
                    Global.iconPairing(
                        ivChart3DefaultMoodAvgMoodImage,
                        moodPathMap[defaultMoodChart3]!!
                    )
                    if (defaultMoodChart3 == "null") {
                        tvChart3DefaultMoodAvgMoodName.text = "無心情"
                        tvChart3DefaultMoodAvgHaveEvent.text = "無結果"
                        tvChart3DefaultMoodAvgNoEvent.text = "無結果"
                    } else {

                        tvChart3DefaultMoodAvgMoodName.text = defaultMoodChart3

                        if (chart3DefaultMoodAvg.length() == 0) {
                            tvChart3DefaultMoodAvgHaveEvent.text = "無結果"
                            tvChart3DefaultMoodAvgNoEvent.text = "無結果"
                        } else {
                            tvChart3DefaultMoodAvgHaveEvent.text =
                                chart3DefaultMoodAvg[0].toString()
                            tvChart3DefaultMoodAvgNoEvent.text = chart3DefaultMoodAvg[1].toString()
                        }
                    }
                    llChart3ForAdd.addView(chart3DefaultMoodAvgView)

                    llStatistic.addView(chart3View)
                    llStatistic.addView(
                        LayoutInflater.from(view.context)
                            .inflate(R.layout.statistic_null_ll, container, false)
                    )

                    //chart4
                    Global.iconPairing(
                        ivChart4Mood,
                        moodPathMap[chart4DefaultMood]!!
                    )

                    if (chart4MoodStability == "需要更多紀錄!") {
                        tvChart4MoodStability.text = chart4MoodStability
                    } else {
                        tvChart4MoodStability.text = chart4MoodStability + " / 100"
                    }

                    tvChart4MoodName.text = chart4DefaultMood

                    llChart4AllMoodScore.removeAllViews()
                    var dataArray4 = ArrayList<Int>()
                    for (key in chart4DefaultMoodScore.keys()) {
                        if (context == null) {
                            return@runOnUiThread
                        }
                        dataArray4.add(chart4DefaultMoodScore.getInt(key))
                    }
                    showBarChart4(dataArray4, chart4DefaultMood)
                    llStatistic.addView(chart4View)
                    llStatistic.addView(
                        LayoutInflater.from(view.context)
                            .inflate(R.layout.statistic_null_ll, container, false)
                    )

                    //chart5
                    adapterChart5.clear()//開始讀之前先清空
                    for (key in chart5MostRelevantMood.keys()) {
                        val item = StatisticMostOftenMoodItem(
                            key,
                            chart5MostRelevantMood.getString(key),
                            chart5DefaultMood
                        )
                        adapterChart5.add(item)
                    }
                    //chart5 ll defaultMoodScores部分
                    var defaultMoodChart5 = ""
                    for (key in chart5MostRelevantMood.keys()) {
                        defaultMoodChart5 = key
                        break
                    }

                    iconPairing(ivChart5Mood, moodPathMap[defaultMoodChart5]!!)
                    tvChart5MoodName.text = defaultMoodChart5
                    if (chart5MostRelevantMood.getString(defaultMoodChart5) == "null") {
                        tvChart5MoodStability.text = "無結果"
                    } else {
                        tvChart5MoodStability.text =
                            chart5MostRelevantMood.getString(defaultMoodChart5) + " / 100"
                    }


//                    llChart5AllMoodScore.removeAllViews()
                    var dataArray5 = ArrayList<Float>()

                    for (key in chart5AvgMoodByMood.keys()) {
                        if (context == null) {
                            return@runOnUiThread
                        }
                        dataArray5.add(
                            if (chart5AvgMoodByMood[key].toString() == ("null")) 0f
                            else chart5AvgMoodByMood[key].toString().toFloat()
                        )
                    }
                    showBarChart5(dataArray5, defaultMoodChart5)
                    llStatistic.addView(chart5View)
                }
            }.onFailure {}.onComplete {}.exec()
        }

        //因為要操作所有圖表的東西，所以拉到後面放
        //選擇月份
        btnChooseMoodConfirm.setOnClickListener {
            tvStatisticChooseMonth.text = monthOfChoosable[npChooseMood.value - 1]//不知道為何要-1
            selectedMonth = tvStatisticChooseMonth.text.split(" ")[0]
            selectedYear = tvStatisticChooseMonth.text.split(" ")[2]

            spinnerSelectEvent.setSelection(0, true)
            spinnerChart2SelectMood.setSelection(0, true)
            spinnerChart3SelectEvent.setSelection(0, true)
            spinnerChart4SelectMood.setSelection(0, true)
            spinnerChart5SelectMood.setSelection(0, true)


            //網路取得統計資料
            default()
            alertDialogChooseMoodScore.dismiss()
        }
        //減少月份
        tvStatisticChooseMonthD.setOnClickListener {
            npChooseMood.value += 1//不知為何是+1
            tvStatisticChooseMonth.text = monthOfChoosable[npChooseMood.value - 1]//不知道為何要-1
            selectedMonth = tvStatisticChooseMonth.text.split(" ")[0]
            selectedYear = tvStatisticChooseMonth.text.split(" ")[2]

            spinnerSelectEvent.setSelection(0, true)
            spinnerChart2SelectMood.setSelection(0, true)
            spinnerChart3SelectEvent.setSelection(0, true)
            spinnerChart4SelectMood.setSelection(0, true)
            spinnerChart5SelectMood.setSelection(0, true)
            //網路取得統計資料
            default()
        }
        //增加月份
        tvStatisticChooseMonthI.setOnClickListener {
            npChooseMood.value -= 1//不知為何是-1
            tvStatisticChooseMonth.text = monthOfChoosable[npChooseMood.value - 1]//不知道為何要-1
            selectedMonth = tvStatisticChooseMonth.text.split(" ")[0]
            selectedYear = tvStatisticChooseMonth.text.split(" ")[2]

            spinnerSelectEvent.setSelection(0, true)
            spinnerChart2SelectMood.setSelection(0, true)
            spinnerChart3SelectEvent.setSelection(0, true)
            spinnerChart4SelectMood.setSelection(0, true)
            spinnerChart5SelectMood.setSelection(0, true)
            //網路取得統計資料
            default()
        }

        //網路取得統計資料
        default()
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StatisticFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StatisticFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    //barChart setting
    //barChart1
    private fun initBarChart1() {
        baseOfBarChart(barChart1)
    }

    private fun initBarDataSet1(barDataSet: BarDataSet) {
        baseOfBarDataSet(barDataSet)
    }

    fun showBarChart1(arr: ArrayList<Int>, mood: String) {
        //barChart1的設定
        initBarChart1()

        //寫進資料
        var entries = ArrayList<BarEntry>();
        for (i in arr.indices) {
            entries.add(BarEntry(i.toFloat(), arr[i].toFloat()))
        }

        //把設定好的資料放在BarDataSet，字串弄成空的
        var barDataSet = BarDataSet(entries, "")


        //設定好之後存進data
        initBarDataSet1(barDataSet)
        var data = BarData(barDataSet)

        //bar的寬度
        data.barWidth = 0.5f

        //存進barChart
        barChart1.data = data

        //設定完成後一定要invalidate
        barChart1.invalidate()
    }

    //barChart2
    private fun initBarChart2() {
        baseOfBarChart(barChart2)
    }

    private fun initBarDataSet2(barDataSet: BarDataSet) {
        baseOfBarDataSet(barDataSet)
    }

    fun showBarChart2(arr: ArrayList<Int>) {
        //barChart2的設定
        initBarChart2()

        //寫進資料
        var entries = ArrayList<BarEntry>();
        for (i in arr.indices) {
            entries.add(BarEntry(i.toFloat(), arr[i].toFloat()))
        }

        //把設定好的資料放在BarDataSet，弄成空字串
        var barDataSet = BarDataSet(entries, "")


        //設定好之後存進data
        initBarDataSet2(barDataSet)
        var data = BarData(barDataSet)

        //bar的寬度
        data.barWidth = 0.5f

        //存進barChart
        barChart2.data = data

        //設定完成後一定要invalidate
        barChart2.invalidate()
    }

    //barChart4
    private fun initBarChart4() {
        baseOfBarChart(barChart4)
    }

    private fun initBarDataSet4(barDataSet: BarDataSet) {
        baseOfBarDataSet(barDataSet)
    }

    fun showBarChart4(arr: ArrayList<Int>, mood: String) {
        //barChart4的設定
        initBarChart4()

        //寫進資料
        var entries = ArrayList<BarEntry>();
        for (i in arr.indices) {
            entries.add(BarEntry(i.toFloat(), arr[i].toFloat()))
        }
        //把設定好的資料放在BarDataSet，弄成空字串
        var barDataSet = BarDataSet(entries, "")

        //設定好之後存進data
        initBarDataSet4(barDataSet)
        var data = BarData(barDataSet)

        //bar的寬度
        data.barWidth = 0.5f

        //存進barChart
        barChart4.data = data

        //設定完成後一定要invalidate
        barChart4.invalidate()
    }

    //barChart5
    private fun initBarChart5(mood: String) {
        baseOfBarChart(barChart5)

        //設定description
//        barChart5.description.text = "(平均)"

        //x軸的label
        barChart5.xAxis.valueFormatter = object : ValueFormatter() {
            val labels = arrayOf("5分", "4分", "3分", "2分", "1分")
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return labels[value.toInt()]
            }
        }
    }

    private fun initBarDataSet5(barDataSet: BarDataSet) {
        baseOfBarDataSet(barDataSet)
        //數字規格化(顯示到小數第一位)
        barDataSet.valueFormatter = DefaultValueFormatter(1)
        //bar上方數字大小
        barDataSet.valueTextSize = 25f
    }

    fun showBarChart5(arr: ArrayList<Float>, mood: String) {
        //barChart5的設定
        initBarChart5(mood)

        //寫進資料
        var entries = ArrayList<BarEntry>();
        for (i in arr.indices) {
            entries.add(BarEntry(i.toFloat(), arr[i]))
        }
        //把設定好的資料放在BarDataSet，弄成空字串
        var barDataSet = BarDataSet(entries, "")

        //設定好之後存進data
        initBarDataSet5(barDataSet)
        var data = BarData(barDataSet)

        //bar的寬度
        data.barWidth = 0.5f

        //存進barChart
        barChart5.data = data

        //設定完成後一定要invalidate
        barChart5.invalidate()
    }

    //barChart base setting
    private fun baseOfBarChart(barChart: BarChart) {
        //點擊不會放大
        barChart.setScaleEnabled(false);

        //動畫(持續時間)
        barChart.animateX(1000)
        barChart.animateY(1000)

        //description設定
        barChart.description = null
//        barChart.description.text = "(次數)"
//        barChart.description.setPosition(70f, 70f)
        //x軸設定
        val xAxis = barChart.xAxis
        //設定水平間距為1 (不設定的話圖會跑掉)
        xAxis.granularity = 1f
        //x軸的label
        xAxis.valueFormatter = object : ValueFormatter() {
            val labels = arrayOf("5分", "4分", "3分", "2分", "1分")
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return labels[value.toInt()]
            }
        }
        //label置底
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //不顯示x軸
        xAxis.setDrawAxisLine(false)
        //不顯示垂直線
        xAxis.setDrawGridLines(false)
        //labal字體大小
        xAxis.textSize = 20f


        //左y軸設定
        val leftAxis = barChart.axisLeft
        //垂直間距為1
        leftAxis.granularity = 1f
        //不顯示左格線
        leftAxis.setDrawGridLines(false)
        //不顯示lable
        leftAxis.setDrawLabels(false)
        //不顯示左y軸線
        leftAxis.setDrawAxisLine(false)
        //右y軸設定
        val rightAxis = barChart.axisRight
        //不顯示label
        rightAxis.setDrawLabels(false)
        //不顯示右y軸線
        rightAxis.setDrawAxisLine(false)
        //不顯示右格線
        rightAxis.setDrawGridLines(false)

        //Legend設定
        val legend = barChart.legend
        //legend字型
        legend.textSize = 15f
        //校準(alignment) legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        //微調legend位置
//        legend.xEntrySpace = 2.0f

    }

    private fun baseOfBarDataSet(barDataSet: BarDataSet) {
        //改變bar的顏色
        barDataSet.color = Color.parseColor("#304567")
        //設定legend圖示大小，設成0(因為設isEnable = false 顯示會有問題)
        barDataSet.formSize = 0f
        //bar上方數字的大小
        barDataSet.valueTextSize = 36f
        //bar上方數字顏色
        barDataSet.valueTextColor = R.color.teal_200

        //數字規格化(顯示到整數)
        barDataSet.valueFormatter = DefaultValueFormatter(0)

    }

    //pieChartN1
    private fun initPieDataSetN1(pieDataSet: PieDataSet) {
        //要填入的顏色 (第1、第2、第3、第4、第5、其他)
        val dataSetColors = java.util.ArrayList<Int>()
        dataSetColors.add(resources.getColor(R.color.MOMO))
        dataSetColors.add(resources.getColor(R.color.ARAISYU))
        dataSetColors.add(resources.getColor(R.color.USUKI))
        dataSetColors.add(resources.getColor(R.color.HIWA))
        dataSetColors.add(resources.getColor(R.color.MIZU))
        dataSetColors.add(resources.getColor(R.color.SHIRONEZUMI))
        //填入資料的背景色
        pieDataSet.colors = dataSetColors

        //數字的大小
        pieDataSet.valueTextSize = 18f
        //數字顯示到小數點後第0位
        pieDataSet.valueFormatter = DefaultValueFormatter(0)
        //左下圖示大小，設為0
        pieDataSet.formSize = 0f

        //讓數字在圓餅外面呈現
//        pieDataSet.valueLinePart1Length = 0.3f
//        pieDataSet.valueLinePart2Length = 0.5f
//        pieDataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        //valueline的顏色
        pieDataSet.valueLineColor = resources.getColor(R.color.KARAKURENAI)


    }

    private fun showPieChartN1(keyArr: ArrayList<String>, valueArr: ArrayList<Int>) {
        //重新建構key、value Array
        var newKeyArr = ArrayList<String>()
        var newValueArr = ArrayList<Int>()
        var count = 0
        for (i in keyArr.indices) {
            if (i < 5) {
                newKeyArr.add(keyArr[i])
                newValueArr.add(valueArr[i])
            } else {
                count += valueArr[i]
                if (i == (keyArr.size - 1)) {
                    newKeyArr.add("其他")
                    newValueArr.add(count)
                }
            }
        }

        //entries設定
        var entries = ArrayList<PieEntry>()
        for (i in newValueArr.indices) {
            entries.add(PieEntry(newValueArr[i].toFloat(), newKeyArr[i]))
        }
        //把資料放進dataset
        val pieDataSet = PieDataSet(entries, "")
        initPieDataSetN1(pieDataSet)

        //放入piedata
        val pieData = PieData(pieDataSet)

        //chart設定

        //中間空洞的半徑 (使用預設)
        pieChartN1.holeRadius = 55f
        //中間空洞的顏色
//        pieChartN1.setHoleColor(android.graphics.Color.GRAY)
        //背景色
//        pieChartN1.setBackgroundColor(android.graphics.Color.WHITE)
        //內部的文字大小
        pieChartN1.setEntryLabelTextSize(16f)
        //內部的文字顏色
        pieChartN1.setEntryLabelColor(resources.getColor(R.color.black))
        //中間的文字
        pieChartN1.centerText = "心情"
        if (keyArr.size == 0) {
            pieChartN1.centerText = "快去寫日記吧!"
        }
        //中間文字大小
        pieChartN1.setCenterTextSize(30f)
        //中間文字顏色
        pieChartN1.setCenterTextColor(resources.getColor(R.color.KARAKURENAI))
        //刪除description
        pieChartN1.description = null

        //把legend刪掉
        val legend = pieChartN1.legend
        legend.isEnabled = false

        pieChartN1.data = pieData
        pieChartN1.invalidate()
    }

    //pieChartN0
    private fun initPieDataSetN0(pieDataSet: PieDataSet) {
        //要填入的顏色 (第1、第2、第3、第4、第5、其他)
        val dataSetColors = java.util.ArrayList<Int>()
        dataSetColors.add(resources.getColor(R.color.MOMO))
        dataSetColors.add(resources.getColor(R.color.ARAISYU))
        dataSetColors.add(resources.getColor(R.color.USUKI))
        dataSetColors.add(resources.getColor(R.color.HIWA))
        dataSetColors.add(resources.getColor(R.color.MIZU))
        dataSetColors.add(resources.getColor(R.color.SHIRONEZUMI))
        //填入資料的背景色
        pieDataSet.colors = dataSetColors

        //數字的大小
        pieDataSet.valueTextSize = 18f
        //數字顯示到小數點後第0位
        pieDataSet.valueFormatter = DefaultValueFormatter(0)
        //左下圖示大小
        pieDataSet.formSize = 15f

        //讓數字在圓餅外面呈現
//        pieDataSet.valueLinePart1Length = 0.3f
//        pieDataSet.valueLinePart2Length = 0.5f
//        pieDataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        //valueline的顏色
        pieDataSet.valueLineColor = resources.getColor(R.color.KARAKURENAI)
    }

    private fun showPieChartN0(keyArr: ArrayList<String>, valueArr: ArrayList<Int>) {

        //重新建構key、value Array
        var newKeyArr = ArrayList<String>()
        var newValueArr = ArrayList<Int>()
        var count = 0
        for (i in keyArr.indices) {
            if (i < 5) {
                newKeyArr.add(keyArr[i])
                newValueArr.add(valueArr[i])
            } else {
                count += valueArr[i]
                if (i == (keyArr.size - 1)) {
                    newKeyArr.add("其他")
                    newValueArr.add(count)
                }
            }
        }

        //entries設定
        var entries = ArrayList<PieEntry>()
        for (i in newValueArr.indices) {
            entries.add(PieEntry(newValueArr[i].toFloat(), newKeyArr[i]))
        }
        //把資料放進dataset
        val pieDataSet = PieDataSet(entries, "")
        initPieDataSetN0(pieDataSet)

        //放入piedata
        val pieData = PieData(pieDataSet)


        //chart設定

        //中間空洞的半徑 (使用預設)
        pieChartN0.holeRadius = 55f
        //中間空洞的顏色
//        pieChartN0.setHoleColor(android.graphics.Color.GRAY)
        //背景色
//        pieChartN0.setBackgroundColor(android.graphics.Color.WHITE)
        //內部的文字大小
        pieChartN0.setEntryLabelTextSize(16f)
        //內部的文字顏色
        pieChartN0.setEntryLabelColor(resources.getColor(R.color.black))
        //中間的文字
        pieChartN0.centerText = "活動"
        if (keyArr.size == 0) {
            pieChartN0.centerText = "快去寫日記吧!"
        }
        //中間文字大小
        pieChartN0.setCenterTextSize(30f)
        //中間文字顏色
        pieChartN0.setCenterTextColor(resources.getColor(R.color.KARAKURENAI))
        //刪除description
        pieChartN0.description = null

        //把legend刪掉
        val legend = pieChartN0.legend
        legend.isEnabled = false

        pieChartN0.data = pieData
        pieChartN0.invalidate()
    }
}