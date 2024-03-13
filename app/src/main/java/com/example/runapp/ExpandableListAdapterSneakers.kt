package com.example.runapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.group_layout.*

class ExpandableListAdapterSneakers(
    private val context: Context,
    private val sneakerList: List<String>,
    private val sneakerChildList: HashMap<String, List<String>>,
    private val sneakerImageList: HashMap<String, List<String>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return sneakerList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return sneakerChildList[sneakerList[groupPosition]]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any {
        return sneakerList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return sneakerChildList[sneakerList[groupPosition]]?.get(childPosition) ?: ""
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

    private var onChapterFooterClickListener: OnChapterFooterClickListener? = null

    interface OnChapterFooterClickListener {
        fun onChapterFooterClicked(chapter: String)
    }

    fun setOnChapterFooterClickListener(listener: OnChapterFooterClickListener) {
        this.onChapterFooterClickListener = listener
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView

        val sneakerTitle = getGroup(groupPosition) as String

        // Inflate the layout if convertView is null
        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.group_layout, null)
        }

        val headerTextView = convertView?.findViewById<TextView>(R.id.header_tv_sneaker)
        headerTextView?.text = sneakerTitle

        val footerTextView = convertView?.findViewById<TextView>(R.id.footer_text_sneaker)
        footerTextView?.setOnClickListener {
            val sneaker = getGroup(groupPosition) as String
            onChapterFooterClickListener?.onChapterFooterClicked(sneaker)
        }

        return convertView!!
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val childText = getChild(groupPosition, childPosition) as String
        val childImageUrl = sneakerImageList[sneakerList[groupPosition]]?.get(childPosition)

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.child_item_layout, null)

        // Find views
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val textView = view.findViewById<TextView>(R.id.textView)

        val displayMetrics = context.resources.displayMetrics

        // Calculate desired width and height for each image (one-third of screen width)
        val screenWidth = displayMetrics.widthPixels
        val desiredWidth = screenWidth / 3
        val desiredHeight = desiredWidth

        // Set desired width and height to the ImageView
        imageView.layoutParams.width = desiredWidth
        imageView.layoutParams.height = desiredHeight

        // Set text and image
        textView.text = childText
        if (!childImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(childImageUrl)
                .resize(desiredWidth, desiredHeight)
                .centerInside()
                .into(imageView)
        } else {
            imageView.visibility = View.GONE
        }

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
