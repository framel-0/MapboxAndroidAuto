package com.tinydavid.android_auto.deeplink

import androidx.car.app.CarContext
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.Point
import com.mapbox.navigation.core.geodeeplink.GeoDeeplink
import com.tinydavid.android_auto.MapboxCarApp
import com.tinydavid.car.feedback.core.CarFeedbackItemProvider
import com.tinydavid.car.feedback.ui.CarFeedbackItem
import com.tinydavid.car.feedback.ui.buildSearchPlacesCarFeedbackItems
import com.tinydavid.car.place_list_on_map.PlacesListOnMapProvider
import com.tinydavid.car.search.GetPlacesError
import com.tinydavid.car.search.PlaceRecord
import com.tinydavid.car.search.PlaceRecordMapper

class GeoDeeplinkPlacesListOnMapProvider(
    private val carContext: CarContext,
    private val geoDeeplinkGeocoding: GeoDeeplinkGeocoding,
    private val geoDeeplink: GeoDeeplink
) : PlacesListOnMapProvider, CarFeedbackItemProvider {

    private var geocodingResponse: GeocodingResponse? = null

    @Suppress("ReturnCount")
    override suspend fun getPlaces(): Expected<GetPlacesError, List<PlaceRecord>> {
        // Wait for an origin location
        val origin = MapboxCarApp.carAppServices.location().validLocation()
            ?.run { Point.fromLngLat(longitude, latitude) }
            ?: return ExpectedFactory.createError(
                GetPlacesError("Did not find current location.", null)
            )

        // Request places from the origin to the deeplink place
        val result = geoDeeplinkGeocoding.requestPlaces(geoDeeplink, origin)
            ?: return ExpectedFactory.createError(
                GetPlacesError("Error getting geo deeplink places.", null)
            )
        geocodingResponse = result
        return ExpectedFactory.createValue(
            result.features().map(PlaceRecordMapper::fromCarmenFeature)
        )
    }

    override fun cancel() {
        geoDeeplinkGeocoding.cancel()
    }

    override fun feedbackItems(): List<CarFeedbackItem> = buildSearchPlacesCarFeedbackItems(
        carContext = carContext,
        geoDeeplink = geoDeeplink,
        geocodingResponse = geocodingResponse,
    )
}
