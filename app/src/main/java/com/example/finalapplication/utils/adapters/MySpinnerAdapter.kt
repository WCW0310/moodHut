package com.example.finalapplication.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.example.finalapplication.R
import com.example.finalapplication.utils.Global.iconPairing


class MySpinnerAdapter(
    val context: Context,
    val names: MutableList<String>,
    val iconsPair: MutableMap<String, String>,
    @LayoutRes private val layoutResource: Int
) :
    BaseAdapter() {
    override fun getCount(): Int {
        return names.size
    }

    override fun getItem(position: Int): Any {
        return names[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(layoutResource, parent, false)

        val text = view.findViewById<TextView>(R.id.tvStatisticSpinnerItem)
        val icon = view.findViewById<ImageView>(R.id.ivStatisticSpinnerItem)
        text.text = names[position]
        iconPairing(icon, iconsPair[names[position]]!!)

        return view
    }
}