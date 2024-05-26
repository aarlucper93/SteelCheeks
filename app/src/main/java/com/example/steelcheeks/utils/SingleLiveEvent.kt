package com.example.steelcheeks.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * A custom implementation of MutableLiveData that emits only one event to its observers.
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {

    private var pending = false

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { t ->
            if (pending) {
                observer.onChanged(t)
                pending = false
            }
        }
    }

    override fun setValue(t: T?) {
        pending = true
        super.setValue(t)
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    fun call() {
        value = null
    }
}