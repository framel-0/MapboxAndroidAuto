package com.tinydavid.android_auto.deeplink

import android.content.Intent
import androidx.car.app.Screen
import androidx.lifecycle.Lifecycle
import com.mapbox.navigation.core.geodeeplink.GeoDeeplink
import com.mapbox.navigation.core.geodeeplink.GeoDeeplinkParser
import com.tinydavid.android_auto.logAndroidAuto
import com.tinydavid.car.MainCarContext
import com.tinydavid.car.feedback.core.CarFeedbackSender
import com.tinydavid.car.feedback.ui.CarFeedbackAction
import com.tinydavid.car.place_list_on_map.PlaceMarkerRenderer
import com.tinydavid.car.place_list_on_map.PlacesListItemMapper
import com.tinydavid.car.place_list_on_map.PlacesListOnMapScreen

class GeoDeeplinkNavigateAction(
    val mainCarContext: MainCarContext,
    val lifecycle: Lifecycle
) {
    fun onNewIntent(intent: Intent): Screen? {
        val geoDeeplink = GeoDeeplinkParser.parse(intent.dataString)
            ?: return null
        return preparePlacesListOnMapScreen(geoDeeplink)
    }

    private fun preparePlacesListOnMapScreen(geoDeeplink: GeoDeeplink): Screen {
        logAndroidAuto("GeoDeeplinkNavigateAction preparePlacesListOnMapScreen")
        val accessToken = mainCarContext.mapboxNavigation.navigationOptions.accessToken
        checkNotNull(accessToken) {
            "GeoDeeplinkGeocoding requires an access token"
        }
        val placesProvider = GeoDeeplinkPlacesListOnMapProvider(
            mainCarContext.carContext,
            GeoDeeplinkGeocoding(accessToken),
            geoDeeplink
        )

        return PlacesListOnMapScreen(
            mainCarContext,
            placesProvider,
            PlacesListItemMapper(
                PlaceMarkerRenderer(mainCarContext.carContext),
                mainCarContext
                    .mapboxNavigation
                    .navigationOptions
                    .distanceFormatterOptions
                    .unitType
            ),
            listOf(
                CarFeedbackAction(mainCarContext.mapboxCarMap, CarFeedbackSender(), placesProvider)
            )
        )
    }
}
