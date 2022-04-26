package com.tinydavid.android_auto.car.map.widgets.logo

import com.mapbox.maps.LayerPosition
import com.tinydavid.android_auto.car.map.MapboxCarMapObserver
import com.tinydavid.android_auto.car.map.MapboxCarMapSurface

class CarLogoSurfaceRenderer(
    private val layerPosition: LayerPosition? = null
) : MapboxCarMapObserver {

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        val logoWidget = LogoWidget(mapboxCarMapSurface.carContext)
        mapboxCarMapSurface.style.addPersistentStyleCustomLayer(
            LogoWidget.LOGO_WIDGET_LAYER_ID,
            logoWidget.host,
            layerPosition
        )
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface) {
        mapboxCarMapSurface.style.removeStyleLayer(LogoWidget.LOGO_WIDGET_LAYER_ID)
    }
}
