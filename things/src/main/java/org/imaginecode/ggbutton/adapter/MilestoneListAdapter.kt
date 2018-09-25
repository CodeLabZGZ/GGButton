package org.imaginecode.ggbutton.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.imaginecode.ggbutton.R
import org.imaginecode.ggbutton.model.ListModel

class MilestoneListAdapter(context: Context?, objects: Array<out ListModel>?) : ArrayAdapter<ListModel>(context, R.layout.milestone_item, objects) {

    override fun getView(position: Int, convertViewOld: View?, parent: ViewGroup?): View {

        val item = getItem(position)

        var convertView = convertViewOld
        if(convertViewOld == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.milestone_item, parent, false)
        }

        val amount = convertView?.findViewById<TextView>(R.id.mlAmount)

        amount?.text = item.mlAmount.toString()

        return convertView!!
    }
}