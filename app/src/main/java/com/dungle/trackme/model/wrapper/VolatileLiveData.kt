package com.dungle.trackme.model.wrapper

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * A [MutableLiveData] variant that introduces a _volatile_ behavior. Namely, it has no "memory" of values set
 * prior to the moment of subscription of any observer, with respect to that observer. This allows for the
 * concrete observer to be certain of that all received info is "fresh", published in real time.
 *
 * This comes alongside the default behavior, which typically invokes the subscriber immediately upon
 * subscription with the last value set (if such exists).
 *
 * >Note: this behavior isn't inherent in the [MutableLiveData] itself, but in the complementing
 * [LifecycleRegistry][androidx.lifecycle.LifecycleRegistry], which can invoke observers depending on
 * lifecycle events.
 *
 * ### Implementation details (optional read)
 *
 * The core idea is to subscribe a wrapper around the user's observer, which, by default, initially suppresses the premature,
 * unwanted invocation to the actual observer, if applied.
 *
 * Naively, this could have been achieved by setting aside the current live data value - when registering an observer, and
 * excluding all observer invocations for the same value. But doing that introduces some caveats, such as the risk of indirectly
 * holding references to expired resources (e.g. activities) for an undetermined period of time.
 *
 * To stick with this idea without introducing such risks, we instead associate a unique sequence with each published value,
 * and compare based on the sequence: Each wrapper-observer would hold the initial sequence which was in-effect
 * when it was born; When invoked, it would only pass the call through to the actual observer if the concurrent sequence has
 * changed compared to its initial one.
 */
open class VolatileLiveData<T> : MutableLiveData<T> {
    private val mVersion = AtomicInteger(0)
    private val wrappers = ConcurrentHashMap<Observer<in T>, Observer<T>>()

    constructor() : super()

    constructor(t: T?) : super(t) {
        mVersion.getAndIncrement()
        this.value = t
    }

    override fun setValue(value: T?) {
        mVersion.getAndIncrement()
        super.setValue(value)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val wrapper = ObserverWrapper(mVersion, observer)
        wrappers[observer] = wrapper
        super.observe(owner, wrapper)
    }

    override fun observeForever(observer: Observer<in T>) {
        val wrapper = ObserverWrapper(mVersion, observer)
        wrappers[observer] = wrapper
        super.observeForever(observer)
    }

    override fun removeObserver(observer: Observer<in T>) {
        val wrapper = wrappers[observer]
        wrapper?.let {
            wrappers.remove(observer)
            super.removeObserver(observer)
        }
    }

    private class ObserverWrapper<T>(val currentVersion: AtomicInteger,
                                     val observer: Observer<in T>) : Observer<T> {
        private val initialVersion = currentVersion.get()

        override fun onChanged(t: T) {
            if (initialVersion != currentVersion.get()) {
                observer.onChanged(t)
            }
        }
    }
}