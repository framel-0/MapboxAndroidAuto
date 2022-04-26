package com.tinydavid.car.navigation

import com.tinydavid.android_auto.car.navigation.lanes.CarLanesImageRenderer
import com.tinydavid.android_auto.car.navigation.maneuver.CarManeuverIconOptions
import com.tinydavid.android_auto.car.navigation.maneuver.CarManeuverIconRenderer
import com.tinydavid.android_auto.car.navigation.maneuver.CarManeuverInstructionRenderer
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import com.tinydavid.car.MainCarContext

class CarActiveGuidanceCarContext(
    val mainCarContext: MainCarContext
) {
    /** MapCarContext **/
    val carContext = mainCarContext.carContext
    val mapboxCarMap = mainCarContext.mapboxCarMap
    val mapboxNavigation = mainCarContext.mapboxNavigation
    val distanceFormatter = mainCarContext.distanceFormatter

    /** NavigationCarContext **/
    val carDistanceFormatter = CarDistanceFormatter(
        mapboxNavigation.navigationOptions.distanceFormatterOptions.unitType
    )
    val carLaneImageGenerator = CarLanesImageRenderer(carContext)
    val navigationInfoMapper = CarNavigationInfoMapper(
        carContext.applicationContext,
        CarManeuverInstructionRenderer(),
        CarManeuverIconRenderer(CarManeuverIconOptions.Builder(carContext).build()),
        carLaneImageGenerator,
        carDistanceFormatter
    )
    val maneuverApi = mainCarContext.maneuverApi
    val tripProgressMapper = CarNavigationEtaMapper(
        carDistanceFormatter,
        MapboxTripProgressApi(TripProgressUpdateFormatter.Builder(carContext).build()),
    )
}
