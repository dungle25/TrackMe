package com.dungle.getlocationsample

class Constant {
    companion object {
        const val CURRENT_SESSION = "CURRENT_SESSION"
        const val REQUEST_INTERVAL = 1000L
        const val FASTEST_REQUEST_INTERVAL = 500L
        /*
         * Minimum distance to update
         */
        const val SMALLEST_DISPLACEMENT = 5.0F

        // Notification channel ID
        const val CHANNEL_ID_FOREGROUND_NOTIFICATION = "while_in_use_channel_01"
        const val NOTIFICATION_ID_FOREGROUND = 1000
        const val LOCATION_REQUEST = 123

        const val KEY_SESSION_ID = "KEY_SESSION_ID"
        const val KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES"
    }
}
