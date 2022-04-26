package com.tinydavid.car.navigation

import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.tinydavid.android_auto.ArrivalState
import com.tinydavid.android_auto.MapboxCarApp
import com.tinydavid.android_auto.R
import com.tinydavid.android_auto.car.navigation.roadlabel.RoadLabelSurfaceLayer
import com.tinydavid.android_auto.car.navigation.speedlimit.CarSpeedLimitRenderer
import com.tinydavid.android_auto.logAndroidAuto
import com.tinydavid.car.MainMapActionStrip
import com.tinydavid.car.action.MapboxActionProvider
import com.tinydavid.car.location.CarLocationRenderer
import com.tinydavid.car.preview.CarRouteLine

/**
 * After a route has been selected. This view gives turn-by-turn instructions
 * for completing the route.
 */
class ActiveGuidanceScreen(
    private val carActiveGuidanceContext: CarActiveGuidanceCarContext,
    private val actionProviders: List<MapboxActionProvider>
) : Screen(carActiveGuidanceContext.carContext) {

    val carRouteLine = CarRouteLine(carActiveGuidanceContext.mainCarContext)
    val carLocationRenderer = CarLocationRenderer(carActiveGuidanceContext.mainCarContext)
    val carSpeedLimitRenderer = CarSpeedLimitRenderer(carActiveGuidanceContext.mainCarContext)
    val carNavigationCamera = CarNavigationCamera(
        carActiveGuidanceContext.mapboxNavigation,
        CarCameraMode.FOLLOWING,
        CarCameraMode.OVERVIEW,
    )
    private val roadLabelSurfaceLayer = RoadLabelSurfaceLayer(
        carActiveGuidanceContext.carContext,
        carActiveGuidanceContext.mapboxNavigation,
        carActiveGuidanceContext.mapboxCarMap,
    )

    private val carRouteProgressObserver = CarNavigationInfoObserver(carActiveGuidanceContext)
    private val mapActionStripBuilder = MainMapActionStrip(this, carNavigationCamera)

    private val arrivalObserver = object : ArrivalObserver {

        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            stopNavigation()
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
            // not implemented
        }

        override fun onWaypointArrival(routeProgress: RouteProgress) {
            // not implemented
        }
    }

    init {
        logAndroidAuto("ActiveGuidanceScreen constructor")
        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onCreate(owner: LifecycleOwner) {
                logAndroidAuto("ActiveGuidanceScreen onCreate")
                carActiveGuidanceContext.mapboxNavigation.registerArrivalObserver(arrivalObserver)
            }

            override fun onResume(owner: LifecycleOwner) {
                logAndroidAuto("ActiveGuidanceScreen onResume")
                carActiveGuidanceContext.mapboxCarMap.registerObserver(carLocationRenderer)
                carActiveGuidanceContext.mapboxCarMap.registerObserver(roadLabelSurfaceLayer)
                carActiveGuidanceContext.mapboxCarMap.registerObserver(carSpeedLimitRenderer)
                carActiveGuidanceContext.mapboxCarMap.registerObserver(carNavigationCamera)
                carActiveGuidanceContext.mapboxCarMap.registerObserver(carRouteLine)
                carRouteProgressObserver.start {
                    invalidate()
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                logAndroidAuto("ActiveGuidanceScreen onPause")
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(roadLabelSurfaceLayer)
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(carLocationRenderer)
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(carSpeedLimitRenderer)
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(carNavigationCamera)
                carActiveGuidanceContext.mapboxCarMap.unregisterObserver(carRouteLine)
                carRouteProgressObserver.stop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                logAndroidAuto("ActiveGuidanceScreen onDestroy")
                carActiveGuidanceContext.mapboxNavigation.unregisterArrivalObserver(arrivalObserver)
            }
        })
    }

    override fun onGetTemplate(): Template {
        logAndroidAuto("ActiveGuidanceScreen onGetTemplate")
        val actionStrip = ActionStrip.Builder().apply {
            actionProviders.forEach {
                when (it) {
                    is MapboxActionProvider.ScreenActionProvider -> {
                        this.addAction(it.getAction(this@ActiveGuidanceScreen))
                    }
                    is MapboxActionProvider.ActionProvider -> {
                        this.addAction(it.getAction())
                    }
                }
            }
            this.addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.car_action_navigation_stop_button))
                    .setOnClickListener {
                        stopNavigation()
                    }.build()
            )
        }.build()
        val builder = NavigationTemplate.Builder()
            .setBackgroundColor(CarColor.PRIMARY)
            .setActionStrip(actionStrip)
            .setMapActionStrip(mapActionStripBuilder.build())

        carRouteProgressObserver.navigationInfo?.let {
            builder.setNavigationInfo(it)
        }

        carRouteProgressObserver.travelEstimateInfo?.let {
            builder.setDestinationTravelEstimate(it)
        }

        return builder.build()
    }

    private fun stopNavigation() {
        logAndroidAuto("ActiveGuidanceScreen stopNavigation")
        MapboxCarApp.updateCarAppState(ArrivalState)
    }
}
