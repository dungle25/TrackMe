package com.dungle.getlocationsample.ui

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dungle.getlocationsample.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_session.*

class SessionAdapter : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {
    var data = listOf<Location>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        fun bind(location: Location) {
            val text = "${location.latitude} - ${location.longitude}"
            tvLocation.text = text
        }
    }
}