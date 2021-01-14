package com.dungle.trackme

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

class TrackingStatus {
    companion object {
        const val TRACKING = "TRACKING"
        const val PAUSED = "PAUSED"
        const val STOPPED = "STOPPED"
        const val RESUME = "RESUME"
    }
}