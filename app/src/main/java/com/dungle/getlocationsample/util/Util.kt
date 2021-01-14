package com.dungle.getlocationsample.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dungle.getlocationsample.Status
import com.dungle.getlocationsample.model.LocationData
import kotlin.math.*

class Util {
    companion object {
        @SuppressLint("InflateParams")
        fun createStartLocationMarker(context: Context, markerLayout: Int): Bitmap {
            val markerView: View =
                (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    markerLayout,
                    null
                )
            val width: Int = DeviceDimensionsHelper.convertDpToPixel(25f, context)
            val height: Int = DeviceDimensionsHelper.convertDpToPixel(40f, context)
            markerView.layoutParams = ViewGroup.LayoutParams(width, height)
            markerView.measure(
                DeviceDimensionsHelper.getDisplayWidth(context),
                DeviceDimensionsHelper.getDisplayWidth(context)
            )
            markerView.layout(
                0,
                0,
                DeviceDimensionsHelper.getDisplayWidth(context),
                DeviceDimensionsHelper.getDisplayHeight(context)
            )
            markerView.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(
                markerView.measuredWidth,
                markerView.measuredHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            markerView.draw(canvas)
            return bitmap
        }

        // High order function
        fun handleDataResult(
            status: Status,
            handleSuccess: () -> Unit,
            handleError: () -> Unit,
            hideLoading: () -> Unit,
            showLoading: () -> Unit
        ) {
            when (status) {
                Status.SUCCESS -> {
                    hideLoading()
                    handleSuccess()
                }

                Status.ERROR -> {
                    hideLoading()
                    handleError()
                }

                Status.LOADING -> {
                    showLoading()
                }
            }
        }

        fun showMessage(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun calculateDistance(
            lat1: Double,
            lng1: Double,
            lat2: Double,
            lng2: Double
        ): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lng2 - lng1)
            val a = (sin(dLat / 2) * sin(dLat / 2)
                    + (cos(Math.toRadians(lat1))
                    * cos(Math.toRadians(lat2)) * sin(dLon / 2)
                    * sin(dLon / 2)))
            return 2 * asin(sqrt(a))
        }

        fun calculateDistanceInKm(
            startLocation: Location,
            currentLocation: Location
        ): Float {
            return startLocation.distanceTo(currentLocation) / 1000
        }

        fun calculateSpeed(
            startLocation: LocationData,
            currentLocation: LocationData
        ): Double {
            return sqrt(
                (currentLocation.long - startLocation.long).pow(2)
                        + (currentLocation.lat - startLocation.lat).pow(2)
            ) / (currentLocation.time - startLocation.time)
        }

        fun round(value: Double): Double {
            val scale = 10.0.pow(1.toDouble()).toInt()
            return (value * scale).roundToInt().toDouble() / scale
        }
    }
}