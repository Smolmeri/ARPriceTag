package com.example.myapplication

import android.content.Context
import android.telecom.Call
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.security.AccessControlContext

class CustomAdapter(context: Context,arrayListDetails: ArrayList<Model>) : BaseAdapter(){
    private val layoutInflater: LayoutInflater
    private val arrayListDetails:ArrayList<Model>

    init {
        this.layoutInflater = LayoutInflater.from(context)
        this.arrayListDetails=arrayListDetails
    }

    override fun getCount(): Int {
        return arrayListDetails.size
    }

    override fun getItem(position: Int): Any {
        return arrayListDetails.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View?
        //val listRowHolder: ListRowHolder
        val output = (R.id.basicInfoCard) as TextView
        if (convertView == null) {
            view = this.layoutInflater.inflate(R.layout.price_tag, parent, false)

            //listRowHolder = ListRowHolder(view)
            //view.tag = listRowHolder

        } else {
            view = convertView
        }
        output.text = "CUSTOMADAPTER SAYS HELLO!"

        //listRowHolder.tvName.text = arrayListDetails.get(position).name
        //listRowHolder.tvEmail.text = arrayListDetails.get(position).item
        //listRowHolder.tvId.text = arrayListDetails.get(position).id
        return view
    }
}