package com.example.runapp

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter

class CombinedExpandableListAdapter(private val context: Context,
                                    private val routesAdapter: ExpandableListViewAdapterRoutes,
                                    private val sneakersAdapter: ExpandableListAdapterSneakers): BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return sneakersAdapter.groupCount + routesAdapter.groupCount
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        if (isSneakerGroup(groupPosition)) {
            return sneakersAdapter.getChildrenCount(getSneakerGroupPosition(groupPosition))
        } else {
            return routesAdapter.getChildrenCount(getRouteGroupPosition(groupPosition))
        }
    }

    override fun getGroup(groupPosition: Int): Any {
        return if (isSneakerGroup(groupPosition)) {
            sneakersAdapter.getGroup(getSneakerGroupPosition(groupPosition))
        } else {
            routesAdapter.getGroup(getRouteGroupPosition(groupPosition))
        }
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return if (isSneakerGroup(groupPosition)) {
            sneakersAdapter.getChild(getSneakerGroupPosition(groupPosition), childPosition)
        } else {
            routesAdapter.getChild(getRouteGroupPosition(groupPosition), childPosition)
        }
    }

    override fun getGroupId(groupPosition: Int): Long {
        return if (isSneakerGroup(groupPosition)) {
            sneakersAdapter.getGroupId(getSneakerGroupPosition(groupPosition))
        } else {
            routesAdapter.getGroupId(getRouteGroupPosition(groupPosition))
        }
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        return if (isSneakerGroup(groupPosition)) {
            sneakersAdapter.getChildView(getSneakerGroupPosition(groupPosition), childPosition, isLastChild, convertView, parent)
        } else {
            routesAdapter.getChildView(getRouteGroupPosition(groupPosition), childPosition, isLastChild, convertView, parent)
        }
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return if (isSneakerGroup(groupPosition)) {
            sneakersAdapter.getChildId(getSneakerGroupPosition(groupPosition), childPosition)
        } else {
            routesAdapter.getChildId(getRouteGroupPosition(groupPosition), childPosition)
        }
    }

    override fun hasStableIds(): Boolean {
        // Return true if both adapters have stable IDs.
        // Adjust as necessary based on the specifics of your adapters.
        return sneakersAdapter.hasStableIds() && routesAdapter.hasStableIds()
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        // Delegate to the appropriate adapter.
        return if (isSneakerGroup(groupPosition)) {
            sneakersAdapter.isChildSelectable(getSneakerGroupPosition(groupPosition), childPosition)
        } else {
            routesAdapter.isChildSelectable(getRouteGroupPosition(groupPosition), childPosition)
        }
    }

    // Other required methods...

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        if (isSneakerGroup(groupPosition)) {
            return sneakersAdapter.getGroupView(getSneakerGroupPosition(groupPosition), isExpanded, convertView, parent)
        } else {
            return routesAdapter.getGroupView(getRouteGroupPosition(groupPosition), isExpanded, convertView, parent)
        }
    }

    // Child view handling similar to the group view...

    // Helper methods to determine group type and adjust positions
    private fun isSneakerGroup(groupPosition: Int): Boolean {
        return groupPosition < sneakersAdapter.groupCount
    }

    private fun getSneakerGroupPosition(groupPosition: Int): Int {
        return groupPosition
    }

    private fun getRouteGroupPosition(groupPosition: Int): Int {
        return groupPosition - sneakersAdapter.groupCount
    }
}
