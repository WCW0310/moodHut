package com.example.finalapplication.utils

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

//泛型讓它能處理多種IType的子類
abstract class BaseViewHolder<T : IType?>(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    //專門做某種特定view的工廠
    fun interface Factory {
        fun onCreateViewHolder(parent: ViewGroup?): BaseViewHolder<*>?
    }

    //寫方法要知道要用在哪，要理解繼承能做到那些事
    abstract fun bind(item: T)
}