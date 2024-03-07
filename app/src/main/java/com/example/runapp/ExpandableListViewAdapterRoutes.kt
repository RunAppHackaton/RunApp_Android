package com.example.runapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class ExpandableListViewAdapterRoutes internal constructor(
    private val context: Context, private val routeList : List<String>, private val routeChildList : HashMap<String, List<String>>): BaseExpandableListAdapter() {

    private var onChapterFooterClickListener: OnChapterFooterClickListener? = null

    interface OnChapterFooterClickListener {
        fun onChapterFooterClicked(chapter: String)
    }

    fun setOnChapterFooterClickListener(listener: OnChapterFooterClickListener) {
        this.onChapterFooterClickListener = listener
    }
        override fun getGroupCount(): Int {
        return routeList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return this.routeChildList[this.routeList[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return routeList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return this.routeChildList[this.routeList[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val routeTitle: String = getGroup(groupPosition) as String

        if (convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.route_list, null)
        }
        val routeTv = convertView?.findViewById<TextView>(R.id.route_tv)
        routeTv?.text = routeTitle

        val footerText = convertView?.findViewById<TextView>(R.id.footer_text)
        footerText?.setOnClickListener {
            val route = getGroup(groupPosition) as String
            onChapterFooterClickListener?.onChapterFooterClicked(route)
        }

        return convertView!!
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val routeChildTitle = getChild(groupPosition, childPosition  ) as String

        if (convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.route_child_list, null)
        }
        val routeChildTv = convertView?.findViewById<TextView>(R.id.route_child_tv)
        routeChildTv?.text = routeChildTitle

        return convertView!!
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }
}