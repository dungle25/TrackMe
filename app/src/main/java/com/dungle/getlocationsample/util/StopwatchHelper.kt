package com.dungle.getlocationsample.util

class StopwatchHelper {
    companion object {
        var startTime: Long = 0
        var currentTime: Long = 0
        private var running = false

        fun start() {
            startTime = System.currentTimeMillis()
            running = true
        }

        fun stop() {
            running = false
        }

        fun pause() {
            running = false
            currentTime = System.currentTimeMillis() - startTime
        }

        fun resume() {
            running = true
            startTime = System.currentTimeMillis() - currentTime
        }

        //elaspsed time in seconds
        private fun getElapsedTimeSecs(): Long {
            var elapsed: Long = 0
            if (running) {
                elapsed = (System.currentTimeMillis() - startTime) / 1000 % 60
            }
            return elapsed
        }

        //elaspsed time in minutes
        private fun getElapsedTimeMin(): Long {
            var elapsed: Long = 0
            if (running) {
                elapsed = (System.currentTimeMillis() - startTime) / 1000 / 60 % 60
            }
            return elapsed
        }

        //elaspsed time in hours
        private fun getElapsedTimeHour(): Long {
            var elapsed: Long = 0
            if (running) {
                elapsed = (System.currentTimeMillis() - startTime) / 1000 / 60 / 60
            }
            return elapsed
        }

        override fun toString(): String {
            val format = "%1$02d"
            return (String.format(format, getElapsedTimeHour()) + ":" + String.format(format, getElapsedTimeMin()) + ":"
                    + String.format(format, getElapsedTimeSecs()))
        }
    }
}