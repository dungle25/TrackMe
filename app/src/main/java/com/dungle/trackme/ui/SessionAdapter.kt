package com.dungle.trackme.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dungle.trackme.R
import com.dungle.trackme.model.Session
import com.dungle.trackme.util.Util
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_session.*

class SessionAdapter(private val data: List<Session>) :
    RecyclerView.Adapter<SessionAdapter.ViewHolder>() {
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
        fun bind(session: Session) {
            session.mapSnapshot?.let {
                Glide.with(containerView.context)
                    .asBitmap()
                    .load(it)
                    .fitCenter()
                    .into(ivMap)
            }
            tvDistance?.text = containerView.context.getString(
                R.string.txt_distance, Util.toStringAndRounded(
                    session.distance
                )
            )
            tvAvgSpeed?.text = containerView.context.getString(
                R.string.txt_speed, Util.toStringAndRounded(
                    session.speeds.average()
                )
            )
            tvTime?.text = session.displayDuration
        }
    }
}
