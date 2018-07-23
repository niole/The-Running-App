package com.example.niolenelson.running

/**
 * Created by niolenelson on 7/22/18.
 */
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.route_item.view.*
import com.google.maps.model.LatLng as JavaLatLng

class GeneratedRoutesAdapter(private val items : ArrayList<JavaLatLng>, private val context: MapsActivity) : RecyclerView.Adapter<ViewHolder>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.route_item, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.tvGeneratedRoutesType?.text = "route ${position.toString()}"
        holder?.tvGeneratedRoutesType?.setOnClickListener {
            _ ->
            context.drawRouteAtIndex(position)
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val tvGeneratedRoutesType = view.generated_route
}