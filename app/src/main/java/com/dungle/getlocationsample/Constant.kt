package com.dungle.getlocationsample

class Constant {
    companion object {
        const val CURRENT_SESSION_ID = "CURRENT_SESSION_ID"
        const val ZOOM_LEVEL_OVER = 1f
        const val REQUEST_INTERVAL = 10000L
        const val FASTEST_REQUEST_INTERVAL = 5000L        /*
         * Minimum distance to update
         */
        const val SMALLEST_DISPLACEMENT = 5.0F

        // Notification channel ID
        const val CHANNEL_ID_FOREGROUND_NOTIFICATION = "while_in_use_channel_01"
        const val NOTIFICATION_ID_FOREGROUND = 1000
        const val EVENT_FAKE_LOCATION_DETECTED = "EVENT_FAKE_LOCATION_DETECTED"
        const val LOCATION_REQUEST = 123
        const val BACKGROUND_REQUEST = 456
    }
}
