package com.dungle.getlocationsample.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build.VERSION
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dungle.getlocationsample.Status

class Util {
    companion object {
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

        fun showMessage(context : Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}