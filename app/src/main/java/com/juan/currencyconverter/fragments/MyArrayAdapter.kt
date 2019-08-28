package com.juan.currencyconverter.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.juan.currencyconverter.R
import kotlinx.android.synthetic.main.spinner_layout.view.*

open class MyArrayAdapter(context: Context, countryList:Array<String>, drawableList:TypedArray) : BaseAdapter ()
{
    private var itemList = mutableListOf<Any>()
    private val mContext = context
    private val mCountryList = countryList
    private val mDrawableList = drawableList

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(mContext).inflate(R.layout.spinner_layout,parent,false)
        view.text_spinner_item.text = mCountryList[position]
        view.image_spinner_item.setImageDrawable(mDrawableList.getDrawable(position))
        itemList.add(view)
        return view
    }

    override fun getItem(position: Int): Any {
        return itemList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount() = mCountryList.size
}