package com.example.niolenelson.running

/**
 * Created by niolenelson on 7/22/18.
 */
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.niolenelson.running.utilities.Route
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.route_item.view.*
import com.google.maps.model.LatLng as JavaLatLng

class GeneratedRoutesAdapter(var items : ArrayList<Route>, private val context: MapsActivity) : RecyclerView.Adapter<ViewHolder>() {
    private var selectedPosition: Int = -1

    fun getSelectedItem(): Route? {
       if (selectedPosition > -1) {
           return items[selectedPosition]
       }
       return null
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.route_item, parent, false)
        )
    }

    private fun setSelectedStyle(view: TextView) {
        view.setBackgroundColor(Color.parseColor("#fff00000"))
        view.setTextColor(Color.parseColor("#ffffffff"))
    }

    private fun setNotSelectedStyle(view: TextView) {
        view.setBackgroundColor(Color.parseColor("#ffffffff"))
        view.setTextColor(Color.parseColor("#ff000000"))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder != null && holder.itemView != null) {
            if (position == selectedPosition) {
                setSelectedStyle(holder.tvGeneratedRoutesType!!)
            } else {
                setNotSelectedStyle(holder.tvGeneratedRoutesType!!)
            }
        }

        holder?.tvGeneratedRoutesType?.text = "route ${position.toString()}"
        holder?.tvGeneratedRoutesType?.setOnClickListener {
            _ ->
            if (selectedPosition > -1) {
                context.removeRouteAtIndex(selectedPosition)
                val oldView = context.generated_routes_list.layoutManager.findViewByPosition(selectedPosition)
                if (oldView != null) {
                    val visibleContent = oldView.findViewById<TextView>(R.id.generated_route)
                    setNotSelectedStyle(visibleContent)
                }
            }
            selectedPosition = position
            setSelectedStyle(holder?.tvGeneratedRoutesType!!)
            context.selectRouteAtIndex(position)
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val tvGeneratedRoutesType = view.generated_route
}