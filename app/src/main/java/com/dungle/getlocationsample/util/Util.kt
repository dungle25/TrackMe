package com.dungle.getlocationsample.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import android.os.Build.VERSION
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dungle.getlocationsample.R
import pub.devrel.easypermissions.EasyPermissions

class Util {
    companion object {
        fun checkAccessLocationBackgroundGranted(mContext: Context?): Boolean {
            return if (VERSION.SDK_INT >= Build.VERSION_CODES.Q) EasyPermissions.hasPermissions(
                mContext!!, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) else true
        }

        fun checkFineLocationGranted(mContext: Context?): Boolean {
            return if (VERSION.SDK_INT >= Build.VERSION_CODES.M) EasyPermissions.hasPermissions(
                mContext!!, Manifest.permission.ACCESS_FINE_LOCATION
            ) else true
        }

        fun checkMockLocation(context: Context, location: Location): Boolean {
            val isMock: Boolean = if (VERSION.SDK_INT >= 18) {
                location.isFromMockProvider
            } else {
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ALLOW_MOCK_LOCATION
                ) != "0"
            }
            return isMock
        }

        @SuppressLint("InflateParams")
        fun createStartLocationMarker(context: Context): Bitmap {
            val markerView: View =
                (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    R.layout.layout_start_marker,
                    null
                )
            val width: Int = DeviceDimensionsHelper.convertDpToPixel(35f, context)
            val height: Int = DeviceDimensionsHelper.convertDpToPixel(50f, context)
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
    }
}