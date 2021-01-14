package com.dungle.trackme

class Constant {
    companion object {
        const val CURRENT_SESSION = "CURRENT_SESSION"
        const val FROM_NOTIFICATION = "FROM_NOTIFICATION"
        const val REQUEST_INTERVAL = 500L
        const val FASTEST_REQUEST_INTERVAL = 300L
        /*
         * Minimum distance to update in meters
         */
        const val SMALLEST_DISPLACEMENT = 5.0F

        // Notification channel ID
        const val CHANNEL_ID_FOREGROUND_NOTIFICATION = "while_in_use_channel_01"
        const val NOTIFICATION_ID_FOREGROUND = 1000
        const val LOCATION_REQUEST = 123
    }
}
