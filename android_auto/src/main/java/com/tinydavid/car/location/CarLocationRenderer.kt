package com.tinydavid.car.location

import com.tinydavid.android_auto.MapboxCarApp
import com.tinydavid.android_auto.car.map.MapboxCarMapSurface
import com.tinydavid.android_auto.car.map.MapboxCarMapObserver
import com.tinydavid.android_auto.logAndroidAuto
import com.mapbox.maps.plugin.locationcomponent.location
import com.tinydavid.car.MainCarContext

/**
 * Create a simple 3d location puck. This class is demonstrating how to
 * create a renderer. To Create a new location experience, try creating a new class.
 */
class CarLocationRenderer(
    private val mainCarContext: MainCarContext
) : MapboxCarMapObserver {

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarLocationRenderer carMapSurface loaded")
        mapboxCarMapSurface.mapSurface.location.apply {
            locationPuck = CarLocationPuck.navigationPuck2D(mainCarContext.carContext)
            enabled = true
            pulsingEnabled = true
            setLocationProvider(MapboxCarApp.carAppServices.location().navigationLocationProvider)
        }
    }
}
