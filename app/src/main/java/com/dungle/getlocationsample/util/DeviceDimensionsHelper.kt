package com.dungle.getlocationsample.util

import android.content.Context
import android.util.TypedValue

class DeviceDimensionsHelper {
    companion object {
        // DeviceDimensionsHelper.getDisplayWidth(context) => (display width in pixels)
        fun getDisplayWidth(context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.widthPixels
        }

        // DeviceDimensionsHelper.getDisplayHeight(context) => (display height in pixels)
        fun getDisplayHeight(context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.heightPixels
        }

        // DeviceDimensionsHelper.convertDpToPixel(25f, context) => (25dp converted to pixels)
        fun convertDpToPixel(dp: Float, context: Context): Int {
            val r = context.resources
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics)
                .toInt()
        }

        fun convertDpToPixelFloat(dp: Float, context: Context): Float {
            val r = context.resources
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics)
        }
    }
}