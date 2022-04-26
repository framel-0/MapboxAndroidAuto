package com.tinydavid.car.route

internal interface RoutesProvider {
    fun registerRoutesListener(listener: RoutesListener)
    fun unregisterRoutesListener(listener: RoutesListener)
}
