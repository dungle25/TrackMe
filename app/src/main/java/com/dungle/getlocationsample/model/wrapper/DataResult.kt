package com.dungle.getlocationsample.model.wrapper

import com.dungle.getlocationsample.Status

data class DataResult<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): DataResult<T> {
            return DataResult(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): DataResult<T> {
            return DataResult(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): DataResult<T> {
            return DataResult(Status.LOADING, data, null)
        }
    }
}