package com.dungle.getlocationsample.model.wrapper

class DataExceptionHandler {
    fun <T : Any> handleException(e: Exception): DataResult<T> {
        return when (e) {
            is NullPointerException -> DataResult.error(getErrorMessage(404), null)
            else -> DataResult.error(getErrorMessage(Int.MAX_VALUE), null)
        }
    }

    private fun getErrorMessage(code: Int): String {
        return when (code) {
            404 -> "Not found"
            else -> "Something went wrong"
        }
    }
}