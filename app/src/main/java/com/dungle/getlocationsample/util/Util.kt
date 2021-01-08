package com.dungle.getlocationsample.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Build.VERSION
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
    }
}