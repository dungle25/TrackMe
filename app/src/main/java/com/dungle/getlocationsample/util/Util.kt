package com.dungle.getlocationsample.util

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Build.VERSION
import android.provider.Settings
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
    }
}