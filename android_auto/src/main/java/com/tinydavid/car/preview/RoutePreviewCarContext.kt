package com.tinydavid.car.preview

import com.tinydavid.car.MainCarContext

data class RoutePreviewCarContext internal constructor(
    val mainCarContext: MainCarContext
) {
    /** MainCarContext **/
    val carContext = mainCarContext.carContext
    val mapboxCarMap = mainCarContext.mapboxCarMap
    val distanceFormatter = mainCarContext.distanceFormatter
    val mapboxNavigation = mainCarContext.mapboxNavigation
}
