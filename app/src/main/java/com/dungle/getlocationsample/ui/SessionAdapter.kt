package com.dungle.getlocationsample.ui

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.model.Session
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.android.extensions.LayoutContainer

class SessionAdapter(private val data: List<Session>) :
    RecyclerView.Adapter<SessionAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_session, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = data[position]
        holder.bind(session)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer, OnMapReadyCallback {
        var currentLocation: Location? = null
        var map: MapView? = null
        fun bind(session: Session) {
            initMap(containerView)
//            currentLocation = session.
//            val text = "${location.latitude} - ${location.longitude}"
//            tvLocation.text = text
        }

        private fun initMap(containerView: View) {
            map = containerView.findViewById(R.id.mapView)
            if (map != null) {
                map!!.onCreate(null)
                map!!.onResume()
                map!!.getMapAsync(this)
            }
        }

        override fun onMapReady(googleMap: GoogleMap?) {
            googleMap?.let {
                settingMap(it)
            }
            MapsInitializer.initialize(containerView.context)
            currentLocation?.let {
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        Constant.ZOOM_LEVEL_OVER
                    )
                )
            }
        }

        private fun settingMap(map: GoogleMap?) {
            map?.uiSettings?.isZoomControlsEnabled = false
            map?.uiSettings?.isRotateGesturesEnabled = false
            map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    containerView.context, R.raw.mapstyle
                )
            )
        }
    }
}