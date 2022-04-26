package com.tinydavid.car

import androidx.car.app.CarContext
import com.mapbox.navigation.base.formatter.DistanceFormatter
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.utils.internal.JobControl
import com.tinydavid.android_auto.car.map.MapboxCarMap
import com.tinydavid.car.settings.CarSettingsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainCarContext(
    val carContext: CarContext,
    val mapboxCarMap: MapboxCarMap
) {
    val carSettingsStorage = CarSettingsStorage(carContext)

    val mapboxNavigation: MapboxNavigation by lazy {
        MapboxNavigationProvider.retrieve()
    }

    val distanceFormatter: DistanceFormatter by lazy {
        MapboxDistanceFormatter(
            mapboxNavigation.navigationOptions.distanceFormatterOptions
        )
    }

    val maneuverApi: MapboxManeuverApi by lazy {
        MapboxManeuverApi(distanceFormatter)
    }

    fun getJobControl(): JobControl {
        val supervisorJob = SupervisorJob()
        val scope = CoroutineScope(supervisorJob + Dispatchers.Main)
        return JobControl(supervisorJob, scope)
    }
}
