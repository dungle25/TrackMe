package com.dungle.getlocationsample.model

data class PermissionResultModel(
    private val isPermissionGranted: Boolean = false,
    private val requestCode: String? = ""
)