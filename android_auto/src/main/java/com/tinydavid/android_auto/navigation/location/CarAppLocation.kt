package com.tinydavid.android_auto.navigation.location

import android.location.Location
import com.tinydavid.android_auto.MapboxCarApp
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

/**
 * Provides a way to access the car or app navigation location.
 * Access through [MapboxCarApp.carAppServices].
 */
interface CarAppLocation {
    /**
     * location provider that is attached to [MapboxNavigation].
     * This provider can be used as a relay for the latest map coordinates.
     */
    val navigationLocationProvider: NavigationLocationProvider

    /**
     * Helper function that will suspend until a location is found,
     * or until the coroutine scope is no longer active.
     */
    suspend fun validLocation(): Location?
}
