package com.tinydavid.car.search

import com.mapbox.search.MapboxSearchSdk
import com.tinydavid.android_auto.MapboxCarApp
import com.tinydavid.car.MainCarContext
import com.tinydavid.car.preview.CarRouteRequest

/**
 * Contains the dependencies for the search feature.
 */
class SearchCarContext(
    val mainCarContext: MainCarContext
) {
    /** MainCarContext **/
    val carContext = mainCarContext.carContext
    val distanceFormatter = mainCarContext.distanceFormatter

    /** SearchCarContext **/
    val carSearchEngine = CarSearchEngine(
        MapboxSearchSdk.getSearchEngine(),
        MapboxCarApp.carAppServices.location().navigationLocationProvider
    )
    val carRouteRequest = CarRouteRequest(
        mainCarContext.mapboxNavigation,
        MapboxCarApp.carAppServices.location().navigationLocationProvider
    )
}
