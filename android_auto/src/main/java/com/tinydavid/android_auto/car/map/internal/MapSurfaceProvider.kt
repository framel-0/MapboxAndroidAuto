package com.tinydavid.android_auto.car.map.internal

import android.content.Context
import android.view.Surface
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapSurface

/**
 * This provider is needed for creating tests.
 */
internal object MapSurfaceProvider {
    fun create(
        context: Context,
        surface: Surface,
        mapInitOptions: MapInitOptions
    ) = MapSurface(context, surface, mapInitOptions)
}
