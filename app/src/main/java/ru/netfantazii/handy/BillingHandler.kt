package ru.netfantazii.handy

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gen.rxbilling.client.RxBilling

class BillingHandler(private val rxBilling: RxBilling) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {

    }
}