package com.example.finalapplication.utils.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalapplication.utils.BaseViewHolder
import com.example.finalapplication.utils.IType
import java.util.*

class CommonAdapter private constructor(
    modelViewMap: Map<Int, BaseViewHolder.Factory>, header: IType?, footer: IType?
) :
    RecyclerView.Adapter<BaseViewHolder<IType?>>() {
    //自己建立一找不到ViewType時的類
    class ViewTypeNotFoundException(message: String?) : Exception(message)

    //加入Header與Footer變數，跟Array內容物會是相同型態，但不可能知道是哪個子類別，且header跟footer沒有要綁定資料，所以用IType
    private val header //要綁資料的話要另外設計，不能跟bind一起
            : IType?
    private val footer: IType?

    //只允許丟IType跟其子類
    private val items //因為他們都有實現IType介面，所以能包含無數種IType。
            : MutableList<IType?>

    //需要讓Type直接對上ViewHolder
    private val modelViewMap: Map<Int, BaseViewHolder.Factory>

    class Builder {
        private val modelViewMap: MutableMap<Int, BaseViewHolder.Factory>

        //Builder也要
        private var header: IType? = null
        private var footer: IType? = null

        //填入的type會讓Map增加一種對應的BaseViewHolder
        fun addType(factory: BaseViewHolder.Factory, type: Int): Builder {
            modelViewMap[type] = factory
            return this
        }

        //鍊式Header與Footer方法
        fun header(factory: BaseViewHolder.Factory): Builder {
            modelViewMap[TYPE_HEADER] = factory
            header = IType { TYPE_HEADER }
            return this
        }

        fun footer(factory: BaseViewHolder.Factory): Builder {
            modelViewMap[TYPE_FOOTER] = factory
            footer = IType { TYPE_FOOTER }
            return this
        }

        fun build(): CommonAdapter {
            return CommonAdapter(modelViewMap, header, footer)
        }

        //Builder一開始會初始化一個Map
        init {
            modelViewMap = HashMap() //外面不需要知道裡面用的是Map
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<IType?> {
        //能包含無數種ViewHolder
        //1. ViewHolder必須在這裡產生(所以外面用HashMap<Integer, RecyclerView.ViewHolder>沒用)
        //2. 決定畫面的是ViewHolder，它才決定資料怎麼bind到畫面上
        if (!modelViewMap.containsKey(viewType)) {
            //要拋出例外，因為這方法是Override的所以不能拋。。。要換地方拋出例外，應該要在Builder或是CommonAdapter的Build
        }
        val factory = modelViewMap[viewType]
        return (factory!!.onCreateViewHolder(parent) as BaseViewHolder<IType?>?)!!
    }

    //自己要記得加
    override fun getItemViewType(position: Int): Int {
        if (header != null && position == 0) {
            return TYPE_HEADER
        }
        return if (footer != null && position == itemCount - 1) {
            TYPE_FOOTER
        } else items[position - (if (header == null) 0 else 1)]!!.getType()
    }

    override fun onBindViewHolder(holder: BaseViewHolder<IType?>, position: Int) {
        if (header != null && position == 0) {
            holder.bind(header)
            return
        }
        if (footer != null && position == itemCount - 1) {
            holder.bind(footer)
            return
        }
        holder.bind(items[position - (if (header == null) 0 else 1)]) //隱含轉換跳警告，可以加註解通知系統沒問題
    }

    override fun getItemCount(): Int {
        return items.size + (if (header == null) 0 else 1) + if (footer == null) 0 else 1
    }

    @Throws(ViewTypeNotFoundException::class)
    fun add(items: List<IType?>) { //這裡拋出例外，使用者在build時就必須進行try-catch處理
        //每次新增資料的時候就要先驗證有沒有可能會找不到，找不到就拋出錯誤，直接不讓它新增
        for (item in items) {
            if (!modelViewMap.containsKey(item!!.getType())) {
                throw ViewTypeNotFoundException(item.getType().toString() + " not found.")
            }
        }
        this.items.addAll(items)
        notifyDataSetChanged()
        //        notifyItemRangeInserted(getItemCount(), items.size());//這邊可能會有問題，因為有header跟footer可能會影響位置
    }

    //寫多個add的接口給它
    @Throws(ViewTypeNotFoundException::class)
    fun add(item: IType) {
        if (!modelViewMap.containsKey(item.getType())) {
            throw ViewTypeNotFoundException(item.getType().toString() + " not found.")
        }
        items.add(item)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    companion object {
        //header跟footer本身不需要取得type，但本身在使用時還是要做到type，因為要符合統一規律，所以做二常數給它
        private const val TYPE_HEADER = -100
        private const val TYPE_FOOTER = -101
    }

    //要先思考需不需要把東西加進建構子
    init {
        items = ArrayList()
        this.modelViewMap = modelViewMap
        this.header = header
        this.footer = footer
    }
}